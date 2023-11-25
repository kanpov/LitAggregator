package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.MarkFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.RatingFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.VisitFeedEntry
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class BrowserScreen(private val manager: ProfileManager) : Screen {
    @Composable
    override fun Content() {
        resizeAppWindow(LARGE_WINDOW_SIZE, true)
        val navigator = LocalNavigator.currentOrThrow
        var query by remember { mutableStateOf(FeedQuery()) }

        Column {
            // top row
            Row(modifier = Modifier.fillMaxWidth()) {
                ActionBar(navigator)
                Spacer(modifier = Modifier.weight(1f))
                SearchBar(query.filterText) { query = query.copy(filterText = it) }
                Spacer(modifier = Modifier.weight(1f))
                PoolBar(
                    selectPool = { query = query.copy(filterPools = query.filterPools + it) },
                    deselectPool = { query = query.copy(filterPools = query.filterPools - it) }
                )
                Spacer(modifier = Modifier.weight(1f))
                SortBar(query) { order, parameter -> query = query.copy(sortOrder = order, sortParameter = parameter) }
            }
            // feed
            FeedView(manager, query)
        }
    }

    @Composable
    private fun ActionBar(navigator: Navigator) {
        RoundedContainer(roundEnd = true) {
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
                iconPath = "icons/sync.png",
                restartAfterwards = true,
                navigator = navigator
            ) {
                var noErrors = true
                manager.withProfileSuspend {
                    if (refreshFeed().isNotEmpty()) noErrors = false
                }
                manager.writeToDisk()
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

        RoundedContainer(roundStart = true, roundEnd = true) {
            BasicIcon(
                painter = painterResource("icons/search.png"),
                size = 30.dp
            )

            BasicTextField(
                value = filterText,
                onValueChange = {
                    filterText = it.take(25) // cap text length
                    changeFilterText(filterText)
                },
                textStyle = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Normal),
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }

    @Composable
    private fun PoolBar(selectPool: (String) -> Unit, deselectPool: (String) -> Unit) {
        RoundedContainer(roundStart = true, roundEnd = true) {
            Spacer(modifier = Modifier.width(3.dp))
            for (id in setOf(
                "homework",
                "marks",
                "ratings",
                "visits",
                "banners",
                "announcements",
                "events",
                "diagnostics"
            )) {
                val tooltip = Locale["browser.pool_bar.$id"]
                var selected by remember { mutableStateOf(false) }
                BasicHoverable(tooltip, delayMillis = 100, startPadding = 2.dp) {
                    H5Text(
                        text = tooltip.first().toString(),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 1.5.em,
                        modifier = Modifier.clickable {
                            selected = !selected
                            if (selected) selectPool(id) else deselectPool(id)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(3.dp))
        }
    }

    @Composable
    private fun SortBar(initialQuery: FeedQuery, changeQuery: (FeedSortOrder, FeedSortParameter) -> Unit) {
        var sortOrder by remember { mutableStateOf(initialQuery.sortOrder) }
        var sortParameter by remember { mutableStateOf(initialQuery.sortParameter) }

        RoundedContainer(roundStart = true) {
            HoverableIconButton(
                tooltip = Locale["browser.query_bar.${sortOrder.id}_tooltip"],
                iconPath = "icons/${sortOrder.id}.png"
            ) {
                sortOrder = sortOrder.opposite
                changeQuery(sortOrder, sortParameter)
            }

            val parameterIds = FeedSortParameter.entries.map { it.id }
            val parameterTranslations = parameterIds.map { Locale["browser.query_bar.parameter.$it"] }
            val defaultTranslation = parameterTranslations[parameterIds.indexOf(sortParameter.id)]

            FullDropdown(
                options = parameterTranslations,
                defaultTranslation,
                onSelectedOptionChange = {
                    sortParameter = FeedSortParameter.fromId(parameterIds[parameterTranslations.indexOf(it)])
                    changeQuery(sortOrder, sortParameter)
                },
                modifier = Modifier.align(Alignment.CenterVertically),
                fontStyle = FontStyle.Italic
            )
        }
    }

    @Composable
    private fun FeedView(manager: ProfileManager, query: FeedQuery) {
        val entries = manager.getProfile()?.feed?.performQuery(query) ?: throw IllegalArgumentException("Unexpected error")
        if (entries.isEmpty()) return

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
            contentPadding = PaddingValues(10.dp)
        ) {
            items(entries) { entry ->
                when (entry) {
                    is HomeworkFeedEntry -> HomeworkFeedEntryRenderer(entry).Render()
                    is MarkFeedEntry -> MarkFeedEntryRenderer(entry).Render()
                    is RatingFeedEntry -> RatingFeedEntryRenderer(entry).Render()
                    is VisitFeedEntry -> VisitFeedEntryRenderer(entry).Render()
                }
            }
        }
    }
}

val Color.Companion.Orange: Color
    get() = Color(red = 255,green = 140,blue = 0)

val Color.Companion.Gold: Color
    get() = Color(red = 255, green = 215, blue = 0)

val Color.Companion.Silver: Color
    get() = Color(red = 169, green = 169, blue = 169)

val Color.Companion.Bronze: Color
    get() = Color(red = 205, green = 133, blue = 63)
