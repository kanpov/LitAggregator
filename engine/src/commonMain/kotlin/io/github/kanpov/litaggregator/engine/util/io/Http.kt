package io.github.kanpov.litaggregator.engine.util.io

import co.touchlab.kermit.Logger
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
//    install(HttpRequestRetry) {
//        retryOnServerErrors(maxRetries = MAX_RETRY_AMOUNT)
//    }

    install(Logging) {
        logger = object : io.ktor.client.plugins.logging.Logger {
            override fun log(message: String) {
                // Custom string manipulator to make the horrendous default formatting look cleaner
                val lines = message.lowercase().lines()
                if (lines.size != 3) return
                val httpCode = lines[0].trim().removePrefix("response: ")
                val httpMethod = lines[1].trim()
                    .removePrefix("method: httpmethod(value=").removeSuffix(")").uppercase()
                val httpUrl = lines[2].trim().removePrefix("from: ")

                Logger.i { "Received $httpCode by $httpMethod: $httpUrl" }
            }
        }

        level = LogLevel.INFO
    }
}

suspend fun downloadFile(url: String, path: String) {
    writeFile(path, ktorClient.get(url).bodyAsText())
    Logger.i { "Downloaded file into $path from: $url" }
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
