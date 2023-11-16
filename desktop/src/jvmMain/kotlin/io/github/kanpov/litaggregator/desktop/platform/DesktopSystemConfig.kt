package io.github.kanpov.litaggregator.desktop.platform

import kotlinx.serialization.Serializable

@Serializable
data class DesktopSystemConfig(
    val supportsWebDriver: Boolean,
    val supportsAwtDesktop: Boolean,
    val supportsShellBrowserInvocation: Boolean,
    val shellBrowser: String,
    val shell: String,
    val localeId: String
)
