package io.github.kanpov.litaggregator.engine.authorizer

import io.github.kanpov.litaggregator.engine.util.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

abstract class Authorizer {
    private var authorized: Boolean = false
    protected abstract val authorizers: Set<suspend () -> Unit>
    protected abstract val validationUrl: String?

    suspend fun authorize(): Boolean {
        for (authorizer in authorizers) {
            authorizer.invoke()

            if (validateAuthorization()) {
                authorized = true
                return true
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
            println(e)
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
        } != null
    }

    private suspend fun makeRequest(retryAmount: Int = 0, block: HttpRequestBuilder.() -> Unit): HttpResponse? {
        if (retryAmount >= RETRY_LIMIT) return null

        if (validationUrl != null && !validateAuthorization()) {
            if (!authorize()) {
                return makeRequest(retryAmount + 1, block)
            }
        }

        val response = makeUnvalidatedRequest(block)
        return if (response.error()) null else response
    }

    private suspend fun validateAuthorization(): Boolean {
        return makeUnvalidatedRequest {
            setUrl(validationUrl!!)
        }.strictlySuccessful()
    }

    protected abstract suspend fun makeUnvalidatedRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse

    companion object {
        private const val RETRY_LIMIT = 3
    }
}

@Serializable
data class StandardAuthorizerCredentials(
    val username: String,
    val password: String
)
