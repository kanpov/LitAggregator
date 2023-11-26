package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.engine.feed.entry.EventFeedEntry
import io.github.kanpov.litaggregator.engine.util.TimeFormatters

class EventFeedEntryRenderer(entry: EventFeedEntry) : FeedEntryRenderer<EventFeedEntry>(entry) {
    @Composable
    private fun EventProperties() {
        if (entry.organizer.isNotBlank() && entry.organizer.trim() != "null") {
            TextProperty(DesktopLocale["browser.events.organizer"], entry.organizer)
        }
        if (entry.subject != null) {
            TextProperty(DesktopLocale["browser.events.subject"], entry.subject!!)
        }
        if (entry.startTime != null) {
            TextProperty(DesktopLocale["browser.events.start_time"], TimeFormatters.dottedMeshDate.format(entry.startTime!!))
        }
        if (entry.endTime != null) {
            TextProperty(DesktopLocale["browser.events.end_time"], TimeFormatters.dottedMeshDate.format(entry.endTime!!))
        }
    }

    @Composable
    override fun ColumnScope.PreviewContent() {
        PreviewHeading(entry.name.split(" ").take(4).joinToString(separator = " "))
        Spacer(modifier = Modifier.height(5.dp))

        Row {
            Surface(
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.dp, Color.Gold),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Box(modifier = Modifier.padding(5.dp)) {
                    Text(
                        entry.reward,
                        fontSize = 1.02.em,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column {
                EventProperties()
            }
        }
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        TextProperty(DesktopLocale["browser.events.full_name"], entry.name)
        TextProperty(DesktopLocale["browser.events.reward"], entry.reward)
        EventProperties()
        if (entry.participantCategory != null && entry.participantCategory!!.trim() != "null") {
            TextProperty(DesktopLocale["browser.events.participant_category"], entry.participantCategory!!)
        }
        if (entry.profession != null && entry.profession!!.trim() != "null") {
            TextProperty(DesktopLocale["browser.events.profession"], entry.profession!!)
        }
    }
}