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
private const val DEFAULT_SHELL_BROWSER = "chromium"
private const val DEFAULT_SHELL = "/bin/bash"

object DesktopEnginePlatform : EnginePlatform {
    var firstBoot = false
    var systemConfig: DesktopSystemConfig? = null
        set(value) {
            if (value == null) {
                Logger.e { "Tried to set system config to null. Aborted operation." }
                return
            }

            field = value
            DesktopLocale.loadById(value.localeId)
        }

    private val systemConfigFile by lazy { getPersistentPath(SYSTEM_CONFIG_FILE_PATH).asFile() }

    override val name: String
        get() = "Desktop, OS: ${SystemUtils.OS_NAME} ${SystemUtils.OS_VERSION}, architecture: ${SystemUtils.OS_ARCH}"
    override val googleClientId: String = "627773039515-rd5jl5lfgk0it63j9a09aqpd2og2vi2o.apps.googleusercontent.com"
    override val googleClientSecret: String = "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"
    override val googleAuthorizerFactory = ::DesktopGoogleAuthorizer
    override val browserEmulator: BrowserEmulator = DesktopBrowserEmulator

    override fun initialize() {
        super.initialize()
        // Load or scan new system config
        if (systemConfigFile.exists()) {
            try {
                systemConfig = jsonInstance.decodeFromString(DesktopSystemConfig.serializer(), readFile(systemConfigFile)!!)
                Logger.i { "Loaded an existing system config" }
            } catch (_: Exception) {
                createSystemConfig()
            }
        } else {
            createSystemConfig()
        }
    }

    private fun createSystemConfig() {
        firstBoot = true

        var browseActionSupported = Desktop.isDesktopSupported()
        if (browseActionSupported) {
            browseActionSupported = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        }

        systemConfig = DesktopSystemConfig(
            supportsWebDriver = DesktopBrowserEmulator.tryLoadDriver(probe = true) != null,
            supportsAwtDesktop = browseActionSupported,
            supportsShellBrowserInvocation = SystemUtils.IS_OS_LINUX,
            browserBinary = DEFAULT_SHELL_BROWSER,
            shellBinary = DEFAULT_SHELL,
            localeId = DesktopLocale.nameToId(DesktopLocale.localeNames.first())
        )

        Logger.i { "Scanned a new system config: $systemConfig" }
        writeFile(systemConfigFile, jsonInstance.encodeToString(DesktopSystemConfig.serializer(), systemConfig!!))
    }

    fun updateSystemConfig(scope: DesktopSystemConfig.() -> Unit) {
        if (systemConfig == null) {
            Logger.e { "Tried to update system config when it was not yet loaded" }
            return
        }

        systemConfig!!.scope()
        DesktopLocale.loadById(systemConfig!!.localeId)
        writeFile(systemConfigFile, jsonInstance.encodeToString(DesktopSystemConfig.serializer(), systemConfig!!))
        Logger.i { "Updated system config" }
    }

    fun openBrowser(uri: URI) {
        if (systemConfig!!.supportsAwtDesktop) {
            Desktop.getDesktop().browse(uri)
            return
        }

        Runtime.getRuntime().exec(arrayOf(systemConfig!!.shellBinary, "-c", "${systemConfig!!.browserBinary} \"$uri\""))
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
