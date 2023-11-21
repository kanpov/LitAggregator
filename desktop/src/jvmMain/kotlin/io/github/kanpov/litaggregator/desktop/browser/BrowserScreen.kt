package io.github.kanpov.litaggregator.desktop.browser

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.github.kanpov.litaggregator.desktop.LARGE_WINDOW_SIZE
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.desktop.resizeAppWindow
import io.github.kanpov.litaggregator.engine.profile.ProfileManager

class BrowserScreen(private val manager: ProfileManager) : Screen {
    @Composable
    override fun Content() {
        resizeAppWindow(LARGE_WINDOW_SIZE)
        H6Text("WIP")
    }
}