package io.github.kanpov.litaggregator.engine.util

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

private const val MAX_RETRY_AMOUNT = 3

val ktorClient = HttpClient(OkHttp) {
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = MAX_RETRY_AMOUNT)
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Napier.i { message }
            }
        }

        level = LogLevel.INFO
    }
}

suspend fun downloadFile(url: String, path: String) {
    writeFile(path, ktorClient.get(url).bodyAsText())
}

fun HttpResponse.strictlySuccessful() = status.value == 200

fun HttpResponse.error() = status.value >= 400

fun HttpResponse.missingCookie(name: String) = setCookie().none { cookie -> cookie.name == name }

fun HttpResponse.findCookie(name: String) = setCookie().first { cookie -> cookie.name == name }

fun HttpRequestBuilder.cookiesFrom(map: Map<String, String>) {
    map.forEach { (name, value) ->
        cookie(name, value)
    }
}

fun HttpRequestBuilder.setUrl(url: String) {
    url(Url(url))
}
