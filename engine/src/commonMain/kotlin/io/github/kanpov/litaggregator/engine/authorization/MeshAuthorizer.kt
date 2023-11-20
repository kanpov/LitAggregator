package io.github.kanpov.litaggregator.engine.authorization

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.io.ktorClient
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
data class MeshAuthorizer(private val credentials: CredentialPair,
                          private var authToken: String = "")
    : Authorizer() {

    override val name: String = "МЭШ"
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
            delay(Random.nextInt(100..200).toLong()) // avoid api rate limit
        }
    }

    private suspend fun authorizeThroughBrowserEmulator() {
        EnginePlatform.current.browserEmulator.use {
            loadUrl(MOS_LOGIN_URL)
            awaitUrl { it.contains("https://login.mos.ru/sps/login/methods/password") }
            awaitElement(""".//input[@id="login"]""").inputText(credentials.username)
            awaitElement(""".//input[@id="password"]""").inputText(credentials.password)
            awaitElement(""".//button[@id="bind"]""").click()
            val tokenUrl = awaitUrl { it.contains("?token=") }
            authToken = tokenUrl
                .split('?')[1] // after query begin
                .split('=')[1] // after assignation of parameter
                .split('&')[0] // before other parameter
        }
    }
}
