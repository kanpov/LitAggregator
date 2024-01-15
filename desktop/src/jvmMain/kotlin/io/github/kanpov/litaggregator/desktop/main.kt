package io.github.kanpov.litaggregator.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.desktop.screen.ProfileSelectScreen
import io.github.kanpov.litaggregator.desktop.screen.SystemConfigScreen
import io.github.kanpov.litaggregator.engine.EnginePlatform
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

lateinit var resizeAppWindow: (size: DpSize, resizable: Boolean) -> Unit

val LARGE_WINDOW_SIZE = DpSize(1400.dp, 900.dp)
val MEDIUM_WINDOW_SIZE = DpSize(700.dp, 600.dp)
val SMALL_WINDOW_SIZE = DpSize(450.dp, 450.dp)

@OptIn(ExperimentalResourceApi::class)
fun main() {
    EnginePlatform.current = DesktopEnginePlatform
    DesktopEnginePlatform.initialize()

    val bootScreen: Screen = if (DesktopEnginePlatform.firstBoot) SystemConfigScreen() else ProfileSelectScreen()

    application {
        val windowState = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = if (bootScreen is SystemConfigScreen) MEDIUM_WINDOW_SIZE else SMALL_WINDOW_SIZE
        )
        var resizable by remember { mutableStateOf(false) }
        resizeAppWindow = { newSize, newResizable ->
            windowState.size = newSize
            resizable = newResizable
        }

        Window(
            title = DesktopLocale["window_name"],
            onCloseRequest = ::exitApplication,
            icon = painterResource("logos/ulysses.png"),
            state = windowState,
            resizable = resizable
        ) {
            Navigator(bootScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
