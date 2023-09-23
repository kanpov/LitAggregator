package io.github.kanpov.litaggregator.engine.authorizer

import io.github.aakira.napier.Napier
import io.github.kanpov.litaggregator.engine.EngineRuntime
import io.github.kanpov.litaggregator.engine.util.error
import io.github.kanpov.litaggregator.engine.util.ktorClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

abstract class GoogleAuthorizer(private val session: GoogleClientSession = GoogleClientSession())
    : Authorizer(){
    private var bufferedCodeVerifier: String? = null

    override val validationUrl: String? = null
    override val authorizers: Set<suspend () -> Unit> = setOf(::authorizeWithBrowser)
    protected abstract var redirectUri: String

    private suspend fun authorizeWithBrowser() {
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val oauthUrl = OAUTH_CODE_ENDPOINT +
                "?client_id=${EngineRuntime.current.googleClientId}" +
                "&redirect_uri=$redirectUri" +
                "&scope=${delimitScopes()}" +
                "&code_challenge=$codeChallenge" +
                "&code_challenge_method=S256" +
                "&response_type=code"

        bufferedCodeVerifier = codeVerifier
        authorizeImpl(oauthUrl)
    }

    override suspend fun makeUnvalidatedRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        if (Instant.parse(session.accessExpiry).isBefore(Instant.now())) {
            refreshAccessToken()
        }

        return ktorClient.request {
            block()
            bearerAuth(session.accessToken)
        }
    }

    private suspend fun refreshAccessToken() {
        val request = ktorClient.post(OAUTH_TOKEN_MANAGE_ENDPOINT) {
            parameter("client_id", EngineRuntime.current.googleClientId)
            parameter("refresh_token", session.refreshToken)
            parameter("grant_type", "refresh_token")
        }

        val json = Json.decodeFromString<JsonObject>(request.bodyAsText())

        session.accessToken = json["access_token"]!!.jsonPrimitive.content
        session.accessExpiry = Instant.now()
            .plusSeconds(json["expires_in"]!!.jsonPrimitive.int.toLong()).toString()

        Napier.i { "Refreshed Google OAuth access token, new one will be valid until ${session.accessExpiry}" }
    }

    // Platform-specific part of the implementation:
    // On Android, the code is received from a native Intent and consent happens through a native browser tab
    // On Desktop, the code is received from a loopback server IP and consent happens through an external browser
    protected abstract fun authorizeImpl(oauthUrl: String)

    protected suspend fun obtainTokens(code: String) {
        val response = ktorClient.post(OAUTH_TOKEN_MANAGE_ENDPOINT) {
            parameter("client_id", EngineRuntime.current.googleClientId)
            parameter("code", code)
            parameter("code_verifier", bufferedCodeVerifier!!)
            parameter("grant_type", "authorization_code")
            parameter("redirect_uri", redirectUri)

            if (EngineRuntime.current.googleClientSecret != null) { // client secret is required on this platform
                parameter("client_secret", EngineRuntime.current.googleClientSecret)
            }
        }

        if (response.error()) return

        val json: JsonObject?

        try {
            json = Json.decodeFromString<JsonObject>(response.bodyAsText())
        } catch (exception: Exception) {
            return
        }

        session.accessToken = json["access_token"]!!.jsonPrimitive.content
        session.refreshToken = json["refresh_token"]!!.jsonPrimitive.content
        session.accessExpiry = Instant.now()
            .plusSeconds(json["expires_in"]!!.jsonPrimitive.int.toLong()).toString()

        println(session.accessToken)
    }

    companion object {
        private val secureRandom = SecureRandom()
        private val sha256Digest = MessageDigest.getInstance("SHA-256")
        private val scopes = listOf(
            ".../auth/userinfo.email",
            ".../auth/classroom.coursework.me",
            ".../auth/gmail.addons.current.action.compose",
            ".../auth/gmail.addons.current.message.action",
            ".../auth/drive.appdata"
        )
        private const val SCOPE_BASE_URL = "https://www.googleapis.com"
        private const val CODE_VERIFIER_LENGTH = 32
        private const val OAUTH_CODE_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        const val OAUTH_TOKEN_MANAGE_ENDPOINT = "https://oauth2.googleapis.com/token"
        const val SCHEME = "io.github.kanpov.litaggregator"

        private fun delimitScopes(): String {
            val builder = StringBuilder()

            scopes.forEach { scope ->
                builder.append(scope.replace("...", SCOPE_BASE_URL))
                builder.append("%20")
            }

            return builder.toString()
        }

        private fun generateCodeVerifier(): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val rawString = (1..CODE_VERIFIER_LENGTH).map {
                secureRandom.nextInt(charPool.size).let { charPool[it] }
            }.joinToString("")

            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawString.toByteArray())
        }

        private fun generateCodeChallenge(codeVerifier: String): String {
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sha256Digest.digest(codeVerifier.toByteArray()))
        }
    }
}

@Serializable
class GoogleClientSession {
    lateinit var accessToken: String
    lateinit var refreshToken: String
    lateinit var accessExpiry: String
}
