package io.github.kanpov.litaggregator.engine.authorizer

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.io.error
import io.github.kanpov.litaggregator.engine.util.io.findCookie
import io.github.kanpov.litaggregator.engine.util.io.ktorClient
import io.github.kanpov.litaggregator.engine.util.io.missingCookie
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import io.ktor.http.setCookie
import io.ktor.util.date.toJvmDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jsoup.Jsoup

private const val ULYSS_LOGIN_URL = "https://in.lit.msu.ru/Ulysses/login/?next=/Ulysses/2023-2024/"
private const val ULYSS_VALIDATION_URL = "https://in.lit.msu.ru/Ulysses/2023-2024/"
private const val TOKEN_CSS_QUERY = "input[name=csrfmiddlewaretoken]"

@Serializable
class UlyssAuthorizer(private val credentials: StandardAuthorizerCredentials,
                      private val session: UlyssClientSession = UlyssClientSession()
)
    : Authorizer() {

    @Transient override val authorizers: Set<suspend () -> Unit> = setOf(
        ::authorizeThroughHttp,
        ::authorizeThroughBrowserEmulator
    )

    @Transient override val validationUrl: String = ULYSS_VALIDATION_URL

    override suspend fun makeUnvalidatedRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return ktorClient.request {
            block()
            cookie("csrftoken", session.csrfToken)
            cookie("sessionid", session.id)
        }
    }

    private suspend fun authorizeThroughHttp() {
        val acquireResponse = ktorClient.get(ULYSS_LOGIN_URL)

        if (acquireResponse.error()) return

        val csrfMiddlewareToken = Jsoup.parse(acquireResponse.bodyAsText()).select(TOKEN_CSS_QUERY).attr("value")
        val csrfLoginCookie = acquireResponse.setCookie().first().value

        val authResponse = ktorClient.post(ULYSS_LOGIN_URL) {
            cookie("csrftoken", csrfLoginCookie)
            setBody(FormDataContent(parameters {
                append("username", credentials.username)
                append("password", credentials.password)
                append("csrfmiddlewaretoken", csrfMiddlewareToken)
            }))
        }

        if (authResponse.error() || authResponse.missingCookie("csrftoken")
            || authResponse.missingCookie("sessionid")) return

        session.csrfToken = authResponse.findCookie("csrftoken").value
        session.id = authResponse.findCookie("sessionid").value
        session.expiry = authResponse.findCookie("sessionid").expires!!
            .toJvmDate().toInstant().toString()
    }

    private suspend fun authorizeThroughBrowserEmulator() {
        val cookies = EnginePlatform.current.useBrowserEmulator(
            loginUrl = ULYSS_LOGIN_URL,
            usernameInputXpath = """.//input[@name="username"]""",
            usernameValue = credentials.username,
            passwordInputXpath = """.//input[@name="password"]""",
            passwordValue = credentials.password,
            buttonXpath = """.//button[@type="submit"]""",
            delayAfterPageLoad = 1000L,
            delayAfterClick = 1000L
        )

        cookies.first { it.name == "csrftoken" }.let {
            session.csrfToken = it.value
        }

        cookies.first { it.name == "sessionid" }.let {
            session.id = it.value
            session.expiry = it.expiry!!.toString()
        }
    }
}

@Serializable
class UlyssClientSession {
    lateinit var csrfToken: String
    lateinit var id: String
    lateinit var expiry: String
}
