package io.github.kanpov.litaggregator.desktop.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.LARGE_WINDOW_SIZE
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.desktop.components.H5Text
import io.github.kanpov.litaggregator.desktop.components.HoverableIconButton
import io.github.kanpov.litaggregator.desktop.resizeAppWindow
import io.github.kanpov.litaggregator.desktop.screen.ProfileSelectScreen
import io.github.kanpov.litaggregator.desktop.screen.SystemConfigScreen
import io.github.kanpov.litaggregator.desktop.screen.config.ConfigIntent
import io.github.kanpov.litaggregator.desktop.screen.config.ConfigScreen
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import io.github.kanpov.litaggregator.engine.util.padLeft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BrowserScreen(private val manager: ProfileManager) : Screen {
    @Composable
    override fun Content() {
        resizeAppWindow(LARGE_WINDOW_SIZE)
        val navigator = LocalNavigator.currentOrThrow

        Column {
            Row(modifier = Modifier.fillMaxWidth().height(intrinsicSize = IntrinsicSize.Max)) {
                LeftTaskbarSide(navigator)
                Spacer(modifier = Modifier.weight(1f))
                RightTaskbarSide()
            }
        }
    }

// --- TASKBAR ---

    @Composable
    private fun RightTaskbarSide() {
        val coroutineScope = rememberCoroutineScope()
        val initialTime = LocalDateTime.now(TimeFormatters.zid)
        val initialSeconds = initialTime.hour * 3600L + initialTime.minute * 60L + initialTime.second
        var currentSeconds by remember { mutableStateOf(initialSeconds) }
        coroutineScope.launch { countTime(initialSeconds) { currentSeconds = it } }

        TaskbarSide(RoundedCornerShape(bottomStart = 15.dp)) {
            // profile info
            val profile = manager.getProfile() ?: return@TaskbarSide
            H5Text(
                profile.identity.profileName,
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 5.dp),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic
            )

            // separator
            Spacer(modifier = Modifier.width(5.dp))
            Spacer(modifier = Modifier.width(2.dp).border(2.dp, Color.Black).fillMaxHeight().align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.width(5.dp))

            // current time
            val hours = (currentSeconds.floorDiv(3600)).toString().padLeft(until = 2, with = '0')
            val minutes = (currentSeconds % 3600).floorDiv(60).toString().padLeft(until = 2, with = '0')
            val seconds = (currentSeconds % 60).toString().padLeft(until = 2, with = '0')

            H5Text(
                "$hours:$minutes:$seconds",
                modifier = Modifier.align(Alignment.CenterVertically),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    @Composable
    private fun LeftTaskbarSide(navigator: Navigator) {
        TaskbarSide(RoundedCornerShape(bottomEnd = 15.dp)) {
            // profile settings
            HoverableIconButton(
                tooltip = Locale["browser.toolbar.settings_tooltip"],
                iconPath = "icons/settings_alt.png"
            ) {
                manager.withProfile {
                    ConfigScreen.startConfig(navigator, ConfigIntent.EditExistingProfile, existingProfile = this,
                        existingPassword = manager.password)
                }
            }

            // profile selection
            HoverableIconButton(
                tooltip = Locale["browser.toolbar.profile_select_tooltip"],
                iconPath = "icons/select.png"
            ) {
                navigator.popUntil { it is ProfileSelectScreen }
            }

            // system config
            HoverableIconButton(
                tooltip = Locale["browser.toolbar.system_config_tooltip"],
                iconPath = "icons/system_config.png"
            ) {
                navigator.push(SystemConfigScreen())
            }

            // separator
            Spacer(modifier = Modifier.width(10.dp))
            Spacer(modifier = Modifier.width(2.dp).border(3.dp, Color.Black).fillMaxHeight().align(Alignment.CenterVertically))

            // sync
            ProlongedActionButton(
                tooltip = Locale["browser.toolbar.sync_tooltip"],
                iconPath = "icons/sync.png"
            ) {
                var noErrors = true
                manager.withProfileSuspend {
                    if (refreshFeed().isNotEmpty()) noErrors = false
                }
                noErrors
            }

            // save to disk
            ProlongedActionButton(
                tooltip = Locale["browser.toolbar.save_tooltip"],
                iconPath = "icons/save2disk.png"
            ) {
                manager.writeToDisk().isSuccess
            }

            // end spacing
            Spacer(modifier = Modifier.width(5.dp))
        }
    }

    @Composable
    private fun ProlongedActionButton(tooltip: String, iconPath: String, action: suspend () -> Boolean) {
        var state by remember { mutableStateOf(ProlongedActionState.Off) }
        val coroutineScope = rememberCoroutineScope()

        HoverableIconButton(
            tooltip = tooltip,
            iconPath = iconPath,
            tint = state.color
        ) {
            if (state == ProlongedActionState.Working) return@HoverableIconButton
            coroutineScope.launch {
                state = ProlongedActionState.Working
                state = if (action()) ProlongedActionState.RecentlyFinished else ProlongedActionState.RecentlyErrored
                delay(10000L) // cool-off period
                state = ProlongedActionState.Off
            }
        }
    }

    private enum class ProlongedActionState(val color: Color) {
        Off(Color.Black),
        Working(Color.Orange),
        RecentlyFinished(Color.Green),
        RecentlyErrored(Color.Red)
    }

    private suspend fun countTime(initial: Long, onChange: (Long) -> Unit) {
        var delta = 0L
        while (true) {
            delay(1000L)
            delta += 1L
            onChange(initial + delta)
        }
    }

    @Composable
    private fun TaskbarSide(shape: RoundedCornerShape, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
        Surface(
            shape = shape,
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Row(modifier = modifier.padding(5.dp).height(38.dp)) {
                content()
            }
        }
    }
}

val Color.Companion.Orange: Color
    get() = Color(red = 255,green = 140,blue = 0)
