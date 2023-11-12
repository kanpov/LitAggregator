package io.github.kanpov.litaggregator.desktop.platform

import co.touchlab.kermit.Logger
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.BrowserEmulator
import io.github.kanpov.litaggregator.engine.util.io.asFile
import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import io.github.kanpov.litaggregator.engine.util.io.readFile
import io.github.kanpov.litaggregator.engine.util.io.writeFile
import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.io.File
import java.net.URI

private const val SYSTEM_CONFIG_FILE_PATH = "system_config.json"
private val standardShellBrowsers = setOf("chrome", "chromium", "firefox", "firefox-esr", "librewolf")

object DesktopEnginePlatform : EnginePlatform {
    lateinit var systemConfig: DesktopSystemConfig
    private val systemConfigFile by lazy { getPersistentPath(SYSTEM_CONFIG_FILE_PATH).asFile() }

    override val name: String
        get() = "Desktop, OS: ${SystemUtils.OS_NAME} ${SystemUtils.OS_VERSION}, architecture: ${SystemUtils.OS_ARCH}"
    override val googleClientId: String = "627773039515-rd5jl5lfgk0it63j9a09aqpd2og2vi2o.apps.googleusercontent.com"
    override val googleClientSecret: String = "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"
    override val googleAuthorizerFactory = ::DesktopGoogleAuthorizer
    override val browserEmulator: BrowserEmulator = DesktopBrowserEmulator

    init {
        // Load or scan new system config
        if (systemConfigFile.exists()) {
            try {
                systemConfig = jsonInstance.decodeFromString(DesktopSystemConfig.serializer(), readFile(systemConfigFile)!!)
                Logger.i { "Loaded system config." }
            } catch (_: Exception) {
                createSystemConfig()
            }
        } else {
            createSystemConfig()
        }
    }

    private fun createSystemConfig() {
        var browseActionSupported = Desktop.isDesktopSupported()
        if (browseActionSupported) {
            browseActionSupported = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        }

        systemConfig = DesktopSystemConfig(
            supportsWebDriver = DesktopBrowserEmulator.tryLoadDriver() != null,
            supportsAwtDesktop = browseActionSupported,
            supportsShellBrowserInvocation = SystemUtils.IS_OS_LINUX,
            shellBrowser = "chromium",
            shell = "/bin/bash"
        )

        Logger.i { "Scanned a new system config: $systemConfig" }
        writeFile(systemConfigFile, jsonInstance.encodeToString(DesktopSystemConfig.serializer(), systemConfig))
    }

    fun openBrowser(uri: URI) {
        if (systemConfig.supportsAwtDesktop) {
            Desktop.getDesktop().browse(uri)
            return
        }

        Runtime.getRuntime().exec(arrayOf(systemConfig.shell, "-c", "${systemConfig.shellBrowser} \"$uri\""))
    }

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
