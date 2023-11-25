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
import io.github.kanpov.litaggregator.desktop.screen.SystemConfigScreen
import io.github.kanpov.litaggregator.engine.EnginePlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

lateinit var resizeAppWindow: (size: DpSize, resizable: Boolean) -> Unit

val LARGE_WINDOW_SIZE = DpSize(1000.dp, 800.dp)
val MEDIUM_WINDOW_SIZE = DpSize(700.dp, 600.dp)
val SMALL_WINDOW_SIZE = DpSize(400.dp, 400.dp)

@OptIn(ExperimentalResourceApi::class)
fun main() {
    EnginePlatform.current = DesktopEnginePlatform
    DesktopEnginePlatform.initialize()

    val bootScreen: Screen = SystemConfigScreen()

    application {
        val windowState = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = MEDIUM_WINDOW_SIZE
        )
        var resizable by remember { mutableStateOf(false) }
        resizeAppWindow = { newSize, newResizable ->
            windowState.size = newSize
            windowState.position = WindowPosition.Aligned(Alignment.Center) // realign
            resizable = newResizable
        }

        Window(
            title = Locale["window_name"],
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
