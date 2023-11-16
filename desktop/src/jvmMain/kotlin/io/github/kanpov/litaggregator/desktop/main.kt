package io.github.kanpov.litaggregator.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.kanpov.litaggregator.desktop.locale.Locale
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

var currentProfileManager: ProfileManager? = null

@OptIn(ExperimentalResourceApi::class)
fun main() {
    EnginePlatform.current = DesktopEnginePlatform
    DesktopEnginePlatform.initialize() // boot platform

    application {
        Window(
            title = Locale.current.windowName,
            onCloseRequest = ::exitApplication,
            icon = painterResource("ulysses_logo.png")
        ) {

        }
    }
}
