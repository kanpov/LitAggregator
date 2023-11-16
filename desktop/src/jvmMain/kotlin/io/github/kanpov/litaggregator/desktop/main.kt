package io.github.kanpov.litaggregator.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import io.github.kanpov.litaggregator.desktop.locale.Locale
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.desktop.screen.SystemConfigScreen
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

var currentProfileManager: ProfileManager? = null

@OptIn(ExperimentalResourceApi::class)
fun main() {
    EnginePlatform.current = DesktopEnginePlatform
    DesktopEnginePlatform.initialize()

//    val bootScreen: Screen = if (DesktopEnginePlatform.firstBoot) {
//        SystemConfigScreen()
//    } else {
//        val cachedProfileFile = ProfileManager.tryLocateCachedProfile()
//
//        if (cachedProfileFile == null) {
//            ProfileSelectScreen()
//        } else {
//            AuthScreen(cachedProfileFile)
//        }
//    }
    val bootScreen: Screen = SystemConfigScreen()

    application {
        Window(
            title = Locale.current.windowName,
            onCloseRequest = ::exitApplication,
            icon = painterResource("ulysses_logo.png")
        ) {
            Navigator(bootScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
