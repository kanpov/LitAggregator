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

    fun initialize()
    fun getCachePath(relativePath: String): String
    fun getPersistentPath(relativePath: String): String

    companion object {
        lateinit var current: EnginePlatform
    }
}
