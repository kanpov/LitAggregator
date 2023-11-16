package io.github.kanpov.litaggregator.desktop.platform

import kotlinx.serialization.Serializable

@Serializable
data class DesktopSystemConfig(
    val supportsWebDriver: Boolean,
    val supportsAwtDesktop: Boolean,
    val supportsShellBrowserInvocation: Boolean,
    var browserBinary: String,
    var shellBinary: String,
    var localeId: String
)
