package io.github.kanpov.litaggregator.desktop.browser

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.LARGE_WINDOW_SIZE
import io.github.kanpov.litaggregator.desktop.resizeAppWindow
import io.github.kanpov.litaggregator.engine.profile.ProfileManager

class BrowserScreen(private val manager: ProfileManager) : Screen {
    @Composable
    override fun Content() {
        resizeAppWindow(LARGE_WINDOW_SIZE)
        val navigator = LocalNavigator.currentOrThrow

        Column {

        }
    }
}