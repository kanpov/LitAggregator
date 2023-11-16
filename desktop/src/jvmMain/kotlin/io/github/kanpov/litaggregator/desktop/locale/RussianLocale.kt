package io.github.kanpov.litaggregator.desktop.locale

object RussianLocale : Locale {
    override val localeId: String = "ru"
    override val localeName: String = "Русский"
    override val windowName: String = "ЛИТ Агрегатор"
    override val systemConfig: SystemConfigLocale = SystemConfigLocale(
        uiLanguage = "Язык интерфейса (требует перезапуска приложения):",
        configureYourSystem = "Настройте вашу систему",
        supportsWebDriver = "Поддерживает необходимые браузерные драйвера для авторизации в МЭШ",
        supportsAwtDesktop = "Поддерживает запуск браузера приложением автоматически",
        supportsShellBrowserInvocation = "Поддерживает запуск браузера приложением через консоль",
        shellBinary = "Используемая консоль:",
        browserBinary = "Используемый браузер:"
    )
    override val button: ButtonLocale = ButtonLocale(
        continueButton = "Продолжить"
    )
}
