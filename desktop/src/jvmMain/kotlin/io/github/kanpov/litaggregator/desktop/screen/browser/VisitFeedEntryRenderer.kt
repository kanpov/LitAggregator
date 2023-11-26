package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.engine.feed.entry.VisitFeedEntry
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import java.time.Instant

class VisitFeedEntryRenderer(entry: VisitFeedEntry) : FeedEntryRenderer<VisitFeedEntry>(entry) {
    @Composable
    private fun ColumnScope.EntryExitTimes(modifier: Modifier = Modifier) {
        Row(modifier = modifier.align(Alignment.CenterHorizontally)) {
            SingleTime(entry.entryTime, Color.Green, DesktopLocale["browser.visits.entry_time"])
            if (entry.exitTime != null) {
                Spacer(modifier = Modifier.width(20.dp))
                SingleTime(entry.exitTime!!, Color.Red, DesktopLocale["browser.visits.exit_time"])
            }
        }
    }

    @Composable
    private fun SingleTime(instant: Instant, color: Color, tooltip: String) {
        Column {
            Text(
                TimeFormatters.shortMeshTime.format(instant),
                color = color,
                fontSize = 2.em
            )

            Text(
                tooltip,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 5.dp)
            )
        }
    }

    @Composable
    override fun ColumnScope.PreviewContent() {
        PreviewHeading(
            DesktopLocale["browser.visits.title_formatting", TimeFormatters.dottedMeshDate.format(entry.metadata.creationTime)]
        )
        EntryExitTimes(Modifier.padding(top = 5.dp))
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        EntryExitTimes()
        TextProperty(DesktopLocale["browser.visits.short_address"], entry.shortAddress)
        TextProperty(DesktopLocale["browser.visits.full_address"], entry.fullAddress)
        TextProperty(DesktopLocale["browser.visits.irregular_pattern"], if (entry.irregularPattern) DesktopLocale["yes"] else DesktopLocale["no"])
    }
}