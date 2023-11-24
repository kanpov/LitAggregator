package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import io.github.kanpov.litaggregator.desktop.browser.Orange
import io.github.kanpov.litaggregator.desktop.screen.restartScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProlongedActionButton(tooltip: String, iconPath: String, restartAfterwards: Boolean = false, navigator: Navigator? = null,
                          action: suspend () -> Boolean) {
    var state by remember { mutableStateOf(ProlongedActionState.Off) }
    val coroutineScope = rememberCoroutineScope()
    var shouldRestart by remember { mutableStateOf(false) }

    if (shouldRestart) {
        restartScreen(navigator!!)
        shouldRestart = false
    }

    HoverableIconButton(
        tooltip = tooltip,
        iconPath = iconPath,
        tint = state.color
    ) {
        if (state == ProlongedActionState.Working) return@HoverableIconButton
        coroutineScope.launch {
            state = ProlongedActionState.Working
            state = if (action()) ProlongedActionState.RecentlyFinished else ProlongedActionState.RecentlyErrored
            if (!restartAfterwards || navigator == null) {
                delay(10000L) // cool-off period
                state = ProlongedActionState.Off
            } else {
                shouldRestart = true
            }
        }
    }
}

enum class ProlongedActionState(val color: Color) {
    Off(Color.Black),
    Working(Color.Orange),
    RecentlyFinished(Color.Green),
    RecentlyErrored(Color.Red)
}
