package io.github.kanpov.litaggregator.engine.util

import io.github.kanpov.litaggregator.engine.util.io.BasicCookie

abstract class BrowserEmulator {
    abstract val loadedUrl: String
    abstract val cookies: Set<BasicCookie>

    fun use(headless: Boolean = true, block: BrowserEmulator.() -> Unit): Boolean {
        return try {
            launch(headless)
            block(this)
            shutdown()
            true
        } catch (_: Exception) {
            false
        }
    }
    protected abstract fun launch(headless: Boolean)
    protected abstract fun shutdown()
    abstract fun loadUrl(url: String)
    abstract fun findElement(xpath: String): BrowserElement?
}

interface BrowserElement {
    fun click()
    fun inputText(value: String)
}
