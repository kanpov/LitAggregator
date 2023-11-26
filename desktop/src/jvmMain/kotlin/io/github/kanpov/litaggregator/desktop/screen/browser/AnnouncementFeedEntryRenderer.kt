package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.engine.feed.entry.AnnouncementFeedEntry

class AnnouncementFeedEntryRenderer(entry: AnnouncementFeedEntry) : FeedEntryRenderer<AnnouncementFeedEntry>(entry) {
    @Composable
    override fun ColumnScope.PreviewContent() {
        PreviewHeading(entry.title)

        val contentText = if (entry.content.length <= 100) entry.content else entry.content.take(100) + "..."
        Text(contentText, modifier = Modifier.padding(top = 5.dp))

        if (entry.categories.isEmpty()) return

        for (category in entry.categories) {
            Row(modifier = Modifier.padding(top = 5.dp)) {
                Text("#$category", color = Color.Blue)
            }
        }
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        DetailedSubHeading(entry.title)
        Text(entry.content, modifier = Modifier.padding(top = 5.dp))
        if (entry.categories.isEmpty()) {
            Text(DesktopLocale["browser.announcements.no_categories"], modifier = Modifier.padding(top = 3.dp),
                fontWeight = FontWeight.SemiBold)
        } else {
            val categoryText = if (entry.categories.size == 1) {
                entry.categories.first()
            } else {
                entry.categories.joinToString(", ")
            }
            TextProperty(DesktopLocale["browser.announcements.categories"], categoryText)
        }
    }
}