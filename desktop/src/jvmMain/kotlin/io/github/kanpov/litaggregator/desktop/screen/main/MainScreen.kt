package io.github.kanpov.litaggregator.desktop.screen.main

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.engine.profile.ProfileManager

class MainScreen(private val manager: ProfileManager) : Screen {
    @Composable
    override fun Content() {
        H6Text("Work-in-progress")

        // TODO implement
    }
}
