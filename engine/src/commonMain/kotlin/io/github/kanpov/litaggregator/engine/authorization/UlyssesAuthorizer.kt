package io.github.kanpov.litaggregator.engine.authorization

import io.github.kanpov.litaggregator.engine.util.TimeFormatters
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
import java.time.LocalDateTime

private const val ULYSSES_VALIDATION_URL = "https://in.lit.msu.ru/Ulysses/2023-2024/"
private const val TOKEN_CSS_QUERY = "input[name=csrfmiddlewaretoken]"

@Serializable
class UlyssesAuthorizer(private val credentials: CredentialPair,
                        private val session: UlyssesSession = UlyssesSession()
) : Authorizer() {

    override val name: String = "УЛИСС"
    @Transient override val authorizers: Set<suspend () -> Unit> = setOf(
        ::authorizeThroughHttp
    )
    @Transient override val validationUrl: String = ULYSSES_VALIDATION_URL

    override suspend fun makeUnvalidatedRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return ktorClient.request {
            block()
            cookie("csrftoken", session.csrfToken)
            cookie("sessionid", session.id)
        }
    }

    private suspend fun authorizeThroughHttp() {
        val loginUrl = determineLoginUrl()
        val acquireResponse = ktorClient.get(loginUrl)

        if (acquireResponse.error()) return

        val csrfMiddlewareToken = Jsoup.parse(acquireResponse.bodyAsText()).select(TOKEN_CSS_QUERY).attr("value")
        val csrfLoginCookie = acquireResponse.setCookie().first().value

        val authResponse = ktorClient.post(loginUrl) {
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
        session.expiry = authResponse.findCookie("sessionid").expires!!.toJvmDate().toInstant().toString()
    }

    private fun determineLoginUrl(): String {
        val dateTime = LocalDateTime.now(TimeFormatters.zid)
        val year: String = if (dateTime.monthValue < 9) {
            "${dateTime.year - 1}-${dateTime.year}"
        } else {
            "${dateTime.year}-${dateTime.year + 1}"
        }

        return if (credentials.username.isBlank()) {
            "https://in.lit.msu.ru/Ulysses/login/keyword/?next=/Ulysses/$year/"
        } else {
            "https://in.lit.msu.ru/Ulysses/login/?next=/Ulysses/$year/"
        }
    }
}

@Serializable
class UlyssesSession {
    lateinit var csrfToken: String
    lateinit var id: String
    lateinit var expiry: String
}
