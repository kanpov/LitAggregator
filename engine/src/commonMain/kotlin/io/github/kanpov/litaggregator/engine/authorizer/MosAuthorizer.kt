package io.github.kanpov.litaggregator.engine.authorizer

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.ktorClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.cookie
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random
import kotlin.random.nextInt

private const val MOS_LOGIN_URL = "https://school.mos.ru/v3/auth/sudir/login"
private const val MOS_VALIDATION_URL = "https://dnevnik.mos.ru/core/api/student_profiles"

@Serializable
data class MosAuthorizer(private val credentials: StandardAuthorizerCredentials,
                         private var authToken: String = "")
    : Authorizer() {

    @Transient override val authorizers: Set<suspend () -> Unit> = setOf(::authorizeThroughBrowserEmulator)
    @Transient override val validationUrl: String = MOS_VALIDATION_URL

    override suspend fun makeUnvalidatedRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return ktorClient.request {
            block()
            cookie("auth_token", authToken)
            cookie("aupd_current_role", "student")
            header("Auth-Token", authToken)
            header("x-mes-role", "student")
            header("x-mes-subsystem", "familyweb")
            bearerAuth(authToken)
        }.also {
//            delay(Random.nextInt(400..700).toLong())
        }
    }

    private suspend fun authorizeThroughBrowserEmulator() {
        val cookies = EnginePlatform.current.useBrowserEmulator(
            loginUrl = MOS_LOGIN_URL,
            usernameInputXpath = """.//input[@id="login"]""",
            usernameValue = credentials.username,
            passwordInputXpath = """.//input[@id="password"]""",
            passwordValue = credentials.password,
            buttonXpath = """.//button[@id="bind"]""",
            delayAfterPageLoad = 6000L,
            delayAfterClick = 20000L
        )

        authToken = cookies.first { it.name == "auth_token" }.value
    }
}
