package io.github.kanpov.litaggregator.engine.authorization

import co.touchlab.kermit.Logger
import io.github.kanpov.litaggregator.engine.util.io.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject

abstract class Authorizer {
    @Transient abstract val name: String
    protected abstract val authorizers: Set<suspend () -> Unit>
    protected abstract val validationUrl: String?

    suspend fun authorize(): Boolean {
        for ((attempts, authorizer) in authorizers.withIndex()) {
            try {
                authorizer.invoke()
            } catch (_: Exception) {
                continue
            }

            try {
                if (validateAuthorization()) {
                    Logger.i { "Authorization to $name was successful after $attempts attempt(s)" }
                    return true
                }
            } catch (_: Exception) {
                continue
            }
        }

        return false
    }

    suspend fun getJson(endpoint: String, block: HttpRequestBuilder.() -> Unit = {}): JsonObject? {
        return try {
            jsonInstance.decodeFromString<JsonObject>(getJsonInternal(endpoint, block)?.bodyAsText() ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun <T> getJsonArray(endpoint: String, block: HttpRequestBuilder.() -> Unit = {}): List<T>? {
        return try {
            decodeJsonRootList(getJsonInternal(endpoint, block)?.bodyAsText() ?: return null)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getJsonArrayFromPayload(endpoint: String, payloadName: String = "payload", block: HttpRequestBuilder.() -> Unit = {}): List<JsonObject>? {
        return try {
            getJson(endpoint, block)?.jArray(payloadName)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getJsonInternal(endpoint: String, block: HttpRequestBuilder.() -> Unit): HttpResponse? {
        return makeRequest {
            block()
            method = HttpMethod.Get
            setUrl(endpoint)
            accept(ContentType.Application.Json)
        }
    }

    suspend fun postJson(endpoint: String, jsonData: String, block: HttpRequestBuilder.() -> Unit): Boolean {
        return makeRequest {
            block()
            method = HttpMethod.Post
            setUrl(endpoint)
            contentType(ContentType.Application.Json)
            setBody(jsonData)
        } != null
    }

    private suspend fun makeRequest(retryAmount: Int = 0, block: HttpRequestBuilder.() -> Unit): HttpResponse? {
        if (retryAmount >= RETRY_LIMIT) return null // avoid recursion hell

        val response = makeUnvalidatedRequest(block)

        if (response.error()) {
            // If authorization needs to be renewed
            if (validationUrl != null && !validateAuthorization()) {
                authorize()
                return makeRequest(retryAmount + 1, block)
            }
            // If this is a normal HTTP issue
            return makeRequest(retryAmount + 1, block)
        }

        return if (response.error()) null else response
    }

    private suspend fun validateAuthorization(): Boolean {
        if (validationUrl == null) return true
        return makeUnvalidatedRequest {
            setUrl(validationUrl!!)
        }.strictlySuccessful()
    }

    protected abstract suspend fun makeUnvalidatedRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse

    companion object {
        private const val RETRY_LIMIT = 2
    }
}

@Serializable
data class CredentialPair(
    val username: String,
    val password: String
)
