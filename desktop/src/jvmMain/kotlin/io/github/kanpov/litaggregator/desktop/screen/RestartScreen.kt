package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay

private const val RESTART_DELAY = 500L
private const val INDICATOR_FREQUENCY = 10L

class RestartScreen(private val newScreen: Screen? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var progress by remember { mutableStateOf(0f) }

        LaunchedEffect(null) {
            val cycles = RESTART_DELAY / INDICATOR_FREQUENCY
            for (i in 1..cycles) {
                progress = i.toFloat() / cycles
                delay(INDICATOR_FREQUENCY)
            }

            if (newScreen == null) {
                navigator.pop()
            } else {
                navigator.push(newScreen)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(70.dp).align(Alignment.Center),
                progress = progress
            )
        }
    }
}

fun restartScreen(navigator: Navigator) {
    navigator.push(RestartScreen())
}
