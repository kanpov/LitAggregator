package io.github.kanpov.litaggregator.desktop.browser

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.kanpov.litaggregator.engine.feed.FeedQuery
import io.github.kanpov.litaggregator.engine.feed.FeedSortOrder
import io.github.kanpov.litaggregator.engine.feed.FeedSortParameter
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class BrowserScreen(private val manager: ProfileManager) : Screen {
    @Composable
    override fun Content() {
        resizeAppWindow(LARGE_WINDOW_SIZE)
        val navigator = LocalNavigator.currentOrThrow
        var query by remember { mutableStateOf(FeedQuery()) }

        Column {
            // top row
            Row(modifier = Modifier.fillMaxWidth().height(intrinsicSize = IntrinsicSize.Max)) {
                ActionBar(navigator)
                Spacer(modifier = Modifier.weight(1f))
                SearchBar(query.filterText) { query = query.copy(filterText = it) }
                Spacer(modifier = Modifier.weight(1f))
                SortBar(query) { order, parameter -> query = query.copy(sortOrder = order, sortParameter = parameter) }
            }
        }
    }

    @Composable
    private fun ActionBar(navigator: Navigator) {
        RoundedContainer(RoundedCornerShape(bottomEnd = 10.dp)) {
            // profile settings
            HoverableIconButton(
                tooltip = Locale["browser.action_bar.settings_tooltip"],
                iconPath = "icons/settings_alt.png"
            ) {
                manager.withProfile {
                    ConfigScreen.startConfig(navigator, ConfigIntent.EditExistingProfile, existingProfile = this,
                        existingPassword = manager.password)
                }
            }

            // profile selection
            HoverableIconButton(
                tooltip = Locale["browser.action_bar.profile_select_tooltip"],
                iconPath = "icons/select.png"
            ) {
                navigator.popUntil { it is ProfileSelectScreen }
            }

            // system config
            HoverableIconButton(
                tooltip = Locale["browser.action_bar.system_config_tooltip"],
                iconPath = "icons/system_config.png"
            ) {
                navigator.push(SystemConfigScreen())
            }

            // separator
            Spacer(modifier = Modifier.width(10.dp))
            Spacer(modifier = Modifier.width(2.dp).border(3.dp, Color.Black).fillMaxHeight().align(Alignment.CenterVertically))

            // sync
            ProlongedActionButton(
                tooltip = Locale["browser.action_bar.sync_tooltip"],
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
                tooltip = Locale["browser.action_bar.save_tooltip"],
                iconPath = "icons/save2disk.png"
            ) {
                manager.writeToDisk().isSuccess
            }

            // end spacing
            Spacer(modifier = Modifier.width(5.dp))
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun SearchBar(initialFilterText: String, changeFilterText: (String) -> Unit) {
        var filterText by remember { mutableStateOf(initialFilterText) }

        RoundedContainer(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)) {
            BasicIcon(
                painter = painterResource("icons/search.png"),
                size = 30.dp
            )

            BasicTextField(
                value = filterText,
                onValueChange = {
                    filterText = it
                    changeFilterText(filterText)
                },
                textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Normal),
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }

    @Composable
    private fun SortBar(initialQuery: FeedQuery, changeQuery: (FeedSortOrder, FeedSortParameter) -> Unit) {
        var sortOrder by remember { mutableStateOf(initialQuery.sortOrder) }
        var sortParameter by remember { mutableStateOf(initialQuery.sortParameter) }

        RoundedContainer(RoundedCornerShape(bottomStart = 10.dp)) {
            HoverableIconButton(
                tooltip = Locale["browser.query_bar.${sortOrder.id}_tooltip"],
                iconPath = "icons/${sortOrder.id}.png"
            ) {
                sortOrder = sortOrder.opposite
                changeQuery(sortOrder, sortParameter)
            }

            val parameterIds = FeedSortParameter.entries.map { it.id }
            val parameterTranslations = parameterIds.map { Locale["browser.query_bar.parameter.$it"] }

            FullDropdown(
                options = parameterTranslations,
                defaultOption = parameterTranslations.last(),
                onSelectedOptionChange = {
                    sortParameter = FeedSortParameter.fromId(parameterIds[parameterTranslations.indexOf(it)])
                    changeQuery(sortOrder, sortParameter)
                },
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 5.dp)
            )
        }
    }
}

val Color.Companion.Orange: Color
    get() = Color(red = 255,green = 140,blue = 0)
