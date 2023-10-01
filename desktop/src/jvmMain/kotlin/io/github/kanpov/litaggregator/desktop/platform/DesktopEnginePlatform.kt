package io.github.kanpov.litaggregator.desktop.platform

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.BrowserEmulator
import org.apache.commons.lang3.SystemUtils
import java.io.File

object DesktopEnginePlatform : EnginePlatform {
    override val name: String
        get() = "Desktop, OS: ${SystemUtils.OS_NAME} ${SystemUtils.OS_VERSION}, architecture: ${SystemUtils.OS_ARCH}"
    override val googleClientId: String = "627773039515-rd5jl5lfgk0it63j9a09aqpd2og2vi2o.apps.googleusercontent.com"
    override val googleClientSecret: String = "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"
    override val googleAuthorizerFactory = ::DesktopGoogleAuthorizer
    override val browserEmulator: BrowserEmulator = DesktopBrowserEmulator

    override fun getCachePath(relativePath: String): String {
        val dir = File("${SystemUtils.USER_HOME}/.litaggregator/cache/")
        dir.mkdirs()
        return dir.absolutePath + "/" + relativePath
    }

    override fun getPersistentPath(relativePath: String): String {
        val dir = File("${SystemUtils.USER_HOME}/.litaggregator/")
        dir.mkdirs()
        return dir.absolutePath + "/" + relativePath
    }
}
