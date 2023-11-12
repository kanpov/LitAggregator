package io.github.kanpov.litaggregator.engine.authorizer

import co.touchlab.kermit.Logger
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.buildUrl
import io.github.kanpov.litaggregator.engine.util.io.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

abstract class GoogleAuthorizer(internal val session: GoogleClientSession = GoogleClientSession())
    : Authorizer(){
    private var bufferedCodeVerifier: String? = null

    override val name: String = "Google"
    override val validationUrl: String? = null
    override val authorizers: Set<suspend () -> Unit> = setOf(
        ::authorizeWithBrowser
    )
    protected abstract var redirectUri: String

    private suspend fun authorizeWithBrowser() {
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val oauthUrl = buildUrl {
            set(OAUTH_CODE_ENDPOINT)
            parameter("client_id", EnginePlatform.current.googleClientId)
            parameter("redirect_uri", redirectUri)
            parameter("scope", delimitScopes())
            parameter("code_challenge", codeChallenge)
            parameter("code_challenge_method", "S256")
            parameter("response_type", "code")
        }
        println(oauthUrl)

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
            parameter("client_id", EnginePlatform.current.googleClientId)
            parameter("refresh_token", session.refreshToken)
            parameter("grant_type", "refresh_token")
            addClientSecret()
        }

        val json = jsonInstance.decodeFromString<JsonObject>(request.bodyAsText())

        session.accessToken = json.jString("access_token")
        session.accessExpiry = Instant.now()
            .plusSeconds(json.jInt("expires_in").toLong()).toString()

        Logger.i { "Refreshed Google OAuth access token, new one will be valid until ${session.accessExpiry}" }
    }

    // Platform-specific part of the implementation:
    // On Android, the code is received from a native Intent and consent happens through a native browser tab
    // On Desktop, the code is received from a loopback server IP and consent happens through an external browser
    protected abstract fun authorizeImpl(oauthUrl: String)

    protected suspend fun obtainTokens(code: String) {
        val response = ktorClient.post(OAUTH_TOKEN_MANAGE_ENDPOINT) {
            parameter("client_id", EnginePlatform.current.googleClientId)
            parameter("code", code)
            parameter("code_verifier", bufferedCodeVerifier!!)
            parameter("grant_type", "authorization_code")
            parameter("redirect_uri", redirectUri)
            addClientSecret()
        }

        if (response.error()) return

        val json = jsonInstance.decodeFromString<JsonObject>(response.bodyAsText())

        session.accessToken = json.jString("access_token")
        session.refreshToken = json.jString("refresh_token")
        session.accessExpiry = Instant.now().plusSeconds(json.jInt("expires_in").toLong()).toString()
    }

    companion object {
        private val secureRandom = SecureRandom()
        private val sha256Digest = MessageDigest.getInstance("SHA-256")
        private const val CODE_VERIFIER_LENGTH = 32
        private const val OAUTH_CODE_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        const val OAUTH_TOKEN_MANAGE_ENDPOINT = "https://oauth2.googleapis.com/token"
        const val SCHEME = "io.github.kanpov.litaggregator"

        private fun delimitScopes() = buildString {
            for (scope in GoogleScope.entries) {
                for (scopeUrl in scope.scopeUrls) {
                    append(scopeUrl)
                    append("%20")
                }
            }
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

enum class GoogleScope(val scopeUrls: Set<String>) {
    Classroom(setOf(
        "https://www.googleapis.com/auth/classroom.courses.readonly",
        "https://www.googleapis.com/auth/classroom.student-submissions.me.readonly"
    )),
    Drive(setOf(
        "https://www.googleapis.com/auth/drive.appdata"
    ));

    companion object {
        fun parse(literal: String) = buildSet {
            val literalParts = literal.split(' ')

            for (scope in GoogleScope.entries) {
                if (scope.scopeUrls.all { literalParts.contains(it) }) {
                    this += scope
                }
            }
        }
    }
}

@Serializable
class GoogleClientSession {
    lateinit var accessToken: String
    lateinit var refreshToken: String
    lateinit var accessExpiry: String
}

private fun HttpRequestBuilder.addClientSecret() {
    if (EnginePlatform.current.googleClientSecret != null) { // client secret is required on this platform
        parameter("client_secret", EnginePlatform.current.googleClientSecret)
    }
}
