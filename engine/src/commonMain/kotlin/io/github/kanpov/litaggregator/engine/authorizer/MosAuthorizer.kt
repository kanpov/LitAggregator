package io.github.kanpov.litaggregator.engine.authorizer

import io.github.kanpov.litaggregator.engine.EngineRuntime
import io.github.kanpov.litaggregator.engine.util.ktorClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.cookie
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

private const val MOS_LOGIN_URL = "https://school.mos.ru/v3/auth/sudir/login"
private const val MOS_VALIDATION_URL = "https://dnevnik.mos.ru/core/api/student_profiles"

@Serializable
data class MosAuthorizer(private val credentials: StandardClientCredentials,
                         private var authToken: String = "")
    : Authorizer() {

    @Transient override val authorizers: Set<suspend () -> Unit> = setOf(::authorizeThroughBrowserEmulator)
    @Transient override val validationUrl: String = MOS_VALIDATION_URL

    override suspend fun makeUnvalidatedRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return ktorClient.request {
            block()
            cookie("auth_token", authToken)
            header("Auth-Token", authToken)
            bearerAuth(authToken)
        }
    }

    private suspend fun authorizeThroughBrowserEmulator() {
        val cookies = EngineRuntime.current.useBrowserEmulator(
            loginUrl = MOS_LOGIN_URL,
            usernameInputXpath = """.//input[@id="login"]""",
            usernameValue = credentials.username,
            passwordInputXpath = """.//input[@id="password"]""",
            passwordValue = credentials.password,
            buttonXpath = """.//button[@id="bind"]""",
            delayAfterPageLoad = 6000L,
            delayAfterClick = 15000L
        )

        authToken = cookies.first { it.name == "auth_token" }.value
    }
}
