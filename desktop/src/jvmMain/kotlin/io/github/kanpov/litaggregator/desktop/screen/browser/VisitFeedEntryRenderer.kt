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
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.engine.feed.entry.VisitFeedEntry
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import java.time.Instant

class VisitFeedEntryRenderer(entry: VisitFeedEntry) : FeedEntryRenderer<VisitFeedEntry>(entry) {
    @Composable
    private fun ColumnScope.EntryExitTimes(modifier: Modifier = Modifier) {
        Row(modifier = modifier.align(Alignment.CenterHorizontally)) {
            SingleTime(entry.entryTime, Color.Green, Locale["browser.visits.entry_time"])
            if (entry.exitTime != null) {
                Spacer(modifier = Modifier.width(20.dp))
                SingleTime(entry.exitTime!!, Color.Red, Locale["browser.visits.exit_time"])
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
        Text(
            Locale["browser.visits.title_formatting", TimeFormatters.dottedMeshDate.format(entry.metadata.creationTime)],
            fontWeight = FontWeight.Medium,
            fontSize = 1.1.em,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        EntryExitTimes(Modifier.padding(top = 5.dp))
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        EntryExitTimes()
        TextProperty(Locale["browser.visits.short_address"], entry.shortAddress)
        TextProperty(Locale["browser.visits.full_address"], entry.fullAddress)
        TextProperty(Locale["browser.visits.irregular_pattern"], if (entry.irregularPattern) Locale["yes"] else Locale["no"])
    }
}