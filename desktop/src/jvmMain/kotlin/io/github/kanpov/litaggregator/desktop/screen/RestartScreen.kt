package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.Locale
import kotlinx.coroutines.delay

private const val RESTART_DELAY = 1000L

class RestartScreen(private val newScreen: Screen? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = Locale["restart.please_wait"],
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LaunchedEffect(null) {
            delay(RESTART_DELAY)
            if (newScreen == null) {
                navigator.pop()
            } else {
                navigator.push(newScreen)
            }
        }
    }
}

fun restartScreen(navigator: Navigator) {
    navigator.push(RestartScreen())
}
