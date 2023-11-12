package io.github.kanpov.litaggregator.engine.util

import io.github.kanpov.litaggregator.engine.util.io.BasicCookie
import kotlinx.coroutines.delay
import java.time.Instant

abstract class BrowserEmulator {
    abstract val loadedUrl: String
    abstract val cookies: Set<BasicCookie>

    suspend fun use(block: suspend BrowserEmulator.() -> Unit): Boolean {
        if (!tryLaunch()) return false
        block.invoke(this)
        shutdown()
        return true
    }

    protected abstract fun tryLaunch(): Boolean

    protected abstract fun shutdown()

    abstract fun loadUrl(url: String)

    suspend fun awaitElement(xpath: String, timeoutSeconds: Long = 5L, checkIntensityMillis: Long = 250L): BrowserElement {
        awaitInternal("element at XPath $xpath still not present",
            timeoutSeconds, checkIntensityMillis) { hasElement(xpath) && findElementOrThrow(xpath).isUsable }
        return findElementOrThrow(xpath)
    }

    suspend fun awaitUrl(timeoutSeconds: Long = 15L, checkIntensityMillis: Long = 250L, matcher: (String) -> Boolean): String {
        awaitInternal("browser is still not at the URL matching given expression",
            timeoutSeconds, checkIntensityMillis) { matcher(loadedUrl) }
        return loadedUrl
    }

    private suspend fun awaitInternal(message: String, timeoutSeconds: Long, checkIntensityMillis: Long, condition: () -> Boolean) {
        val startInstant = Instant.now()

        while (true) {
            val currentInstant = Instant.now()

            if (currentInstant.epochSecond - startInstant.epochSecond >= timeoutSeconds) {
                break
            }

            delay(checkIntensityMillis)
            if (condition()) {
                return
            }
        }

        throw IllegalArgumentException("Await failed with message: $message")
    }

    fun hasElement(xpath: String) = findElementOrNull(xpath) != null

    fun findElementOrThrow(xpath: String): BrowserElement {
        return findElementOrNull(xpath) ?: throw IllegalArgumentException("Could not find element at XPath: $xpath")
    }

    abstract fun findElementOrNull(xpath: String): BrowserElement?
}

interface BrowserElement {
    val isUsable: Boolean
    fun click()
    fun inputText(value: String)
}
