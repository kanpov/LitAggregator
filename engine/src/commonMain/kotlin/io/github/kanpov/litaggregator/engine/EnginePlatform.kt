package io.github.kanpov.litaggregator.engine

import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.GoogleClientSession
import io.github.kanpov.litaggregator.engine.util.BrowserEmulator
import io.github.kanpov.litaggregator.engine.util.io.BasicCookie

interface EnginePlatform {
    val name: String
    val googleClientId: String
    val googleClientSecret: String?
    val googleAuthorizerFactory: (GoogleClientSession) -> GoogleAuthorizer
    val browserEmulator: BrowserEmulator

    fun getCachePath(relativePath: String): String
    fun getPersistentPath(relativePath: String): String
    // generic browser emulator impl limited to 1 url and 1 form with 2 inputs and a button
    // fills in the form and submits, returning all cookies currently stored in the emulator
    @Deprecated("Being replaced with a more flexible BrowserEmulator API")
    suspend fun useBrowserEmulator(
        loginUrl: String,
        usernameInputXpath: String,
        usernameValue: String,
        passwordInputXpath: String,
        passwordValue: String,
        buttonXpath: String,
        delayAfterPageLoad: Long,
        delayAfterClick: Long
    ): Set<BasicCookie>

    companion object {
        lateinit var current: EnginePlatform
    }
}
