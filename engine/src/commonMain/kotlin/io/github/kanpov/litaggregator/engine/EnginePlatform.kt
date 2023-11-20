package io.github.kanpov.litaggregator.engine

import co.touchlab.kermit.Logger
import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.GoogleSession
import io.github.kanpov.litaggregator.engine.profile.ProfileCache
import io.github.kanpov.litaggregator.engine.util.BrowserEmulator

interface EnginePlatform {
    val name: String
    val googleClientId: String
    val googleClientSecret: String?
    val googleAuthorizerFactory: (GoogleSession) -> GoogleAuthorizer
    val browserEmulator: BrowserEmulator

    fun initialize() {
        if (!ProfileCache.exists()) {
            Logger.i { "Profile cache does not exist, attempting to create a new one" }
            ProfileCache.write()
        } else {
            ProfileCache.read()
        }

        Logger.i { "Launched engine platform on $name" }
    }
    fun getCachePath(relativePath: String): String
    fun getPersistentPath(relativePath: String): String

    companion object {
        lateinit var current: EnginePlatform
    }
}
