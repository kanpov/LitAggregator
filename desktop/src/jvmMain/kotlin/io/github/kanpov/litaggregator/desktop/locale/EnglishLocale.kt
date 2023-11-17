package io.github.kanpov.litaggregator.desktop.locale

object EnglishLocale : Locale {
    override val localeId: String = "en"
    override val localeName: String = "English"
    override val windowName: String = "LIT Aggregator"
    override val systemConfig: SystemConfigLocale = SystemConfigLocale(
        uiLanguage = "Interface language (requires restart):",
        configureYourSystem = "Configure your system",
        supportsWebDriver = "Supports necessary browser drivers to authorize to MESH",
        supportsAwtDesktop = "Supports launching the browser automatically",
        supportsShellBrowserInvocation = "Supports launching the browser through the console",
        shellBinary = "Shell binary being used:",
        browserBinary = "Browser binary being used:"
    )
    override val profileSelect: ProfileSelectLocale = ProfileSelectLocale(
        selectYourProfile = "Select a profile",
        loadProfile = "Load profile",
        createProfile = "Create profile",
        recentProfiles = "Recent profiles:"
    )
    override val button: ButtonLocale = ButtonLocale(
        continueButton = "Continue"
    )
}