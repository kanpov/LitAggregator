package io.github.kanpov.litaggregator.desktop.browser

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.github.kanpov.litaggregator.desktop.components.*
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
            // top row
            Row(modifier = Modifier.fillMaxWidth().height(intrinsicSize = IntrinsicSize.Max)) {
                ActionBar(navigator)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    @Composable
    private fun ActionBar(navigator: Navigator) {
        RoundedContainer(RoundedCornerShape(bottomEnd = 15.dp)) {
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
    private fun SortBar() {
        RoundedContainer(RoundedCornerShape(bottomStart = 15.dp)) {

        }
    }
}

val Color.Companion.Orange: Color
    get() = Color(red = 255,green = 140,blue = 0)
