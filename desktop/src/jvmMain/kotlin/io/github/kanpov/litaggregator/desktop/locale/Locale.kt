package io.github.kanpov.litaggregator.desktop.locale

import co.touchlab.kermit.Logger

interface Locale {
    val id: String
    val windowName: String

    companion object {
        lateinit var current: Locale
        private val locales = setOf(RussianLocale, EnglishLocale)

        val defaultLocaleId: String
            get() = locales.first().id

        fun setById(localeId: String) {
            for (locale in locales) {
                if (locale.id == localeId) {
                    Logger.i { "Loaded locale \"${locale.id}\"" }
                    current = locale
                    return
                }
            }

            current = locales.first()
            Logger.w { "Saved locale \"$localeId\" is non-existent, defaulting to \"${current.id}\"" }
        }
    }
}
