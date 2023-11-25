package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.util.TimeFormatters

class HomeworkFeedEntryRenderer(entry: HomeworkFeedEntry) : FeedEntryRenderer<HomeworkFeedEntry>(entry) {
    @Composable
    override fun ColumnScope.PreviewContent() {
        val heading = if (entry.assignedTime == null) {
            Locale["browser.homework.short_title_formatting", entry.subject,
                TimeFormatters.dottedMeshDate.format(entry.metadata.creationTime)]
        } else {
            Locale["browser.homework.full_title_formatting", entry.subject,
                TimeFormatters.dottedMeshDate.format(entry.assignedTime)]
        }

        Text(
            text = heading,
            fontWeight = FontWeight.Medium,
            fontSize = 1.1.em,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        val previewTextNormalized = entry.plain.normalizePlainTextFromHtml(stripNewLines = true)
        val previewText = if (previewTextNormalized.length <= 200) {
            previewTextNormalized
        } else {
            previewTextNormalized.take(200) + "..."
        }

        Text(
            text = previewText,
            modifier = Modifier.padding(top = 5.dp)
        )
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        TextProperty(Locale["browser.homework.subject"], entry.subject)
        if (entry.teacher != null) {
            TextProperty(Locale["browser.homework.teacher"], entry.teacher!!)
        }
        if (entry.assignedTime != null) {
            TextProperty(Locale["browser.homework.assigned_time"], TimeFormatters.dottedMeshDate.format(entry.assignedTime!!))
        }
        if (entry.attachments.isNotEmpty()) {
            AttachmentList(entry.attachments)
        }
        if (entry.allowsSubmissions && entry.submissionUrl != null) {
            LinkProperty(Locale["browser.homework.submission_url"], entry.submissionUrl!!)
        }
        Text(entry.plain.normalizePlainTextFromHtml(), modifier = Modifier.padding(top = 5.dp))
    }
}
