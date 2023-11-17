package io.github.kanpov.litaggregator.desktop.locale

import co.touchlab.kermit.Logger

interface Locale {
    val localeId: String
    val localeName: String
    val windowName: String
    val systemConfig: SystemConfigLocale
    val profileSelect: ProfileSelectLocale
    val button: ButtonLocale

    companion object {
        lateinit var current: Locale
        val locales = setOf(EnglishLocale, RussianLocale)

        val defaultLocaleId: String = locales.first().localeId

        fun setById(localeId: String) {
            for (locale in locales) {
                if (locale.localeId == localeId) {
                    Logger.i { "Loaded locale \"${locale.localeId}\"" }
                    current = locale
                    return
                }
            }

            current = locales.first()
            Logger.w { "Saved locale \"$localeId\" is non-existent, defaulting to \"${current.localeId}\"" }
        }
    }
}

data class SystemConfigLocale(
    val uiLanguage: String,
    val configureYourSystem: String,
    val supportsWebDriver: String,
    val supportsAwtDesktop: String,
    val supportsShellBrowserInvocation: String,
    val browserBinary: String,
    val shellBinary: String
)

data class ProfileSelectLocale(
    val selectYourProfile: String,
    val loadProfile: String,
    val createProfile: String,
    val recentProfiles: String
)

data class ButtonLocale(
    val continueButton: String
)
