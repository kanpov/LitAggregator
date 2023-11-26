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
import io.github.kanpov.litaggregator.engine.feed.entry.MarkFeedEntry
import io.github.kanpov.litaggregator.engine.util.TimeFormatters

class MarkFeedEntryRenderer(entry: MarkFeedEntry) : FeedEntryRenderer<MarkFeedEntry>(entry) {
    @Composable
    private fun MarkBox(modifier: Modifier = Modifier) {
        ColoredFrame(if (entry.isExam) Color.Red else Color.Black, modifier) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    entry.value.toString(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 2.5.em,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    entry.weight.toString(),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp, bottom = 5.dp)
                )
            }
        }
    }

    @Composable
    override fun ColumnScope.PreviewContent() {
        Row {
            MarkBox(modifier = Modifier.align(Alignment.CenterVertically).padding(start = 5.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(start = 15.dp)) {
                PreviewHeading(DesktopLocale["browser.marks.title_formatting", entry.subject,
                    TimeFormatters.dottedMeshDate.format(entry.metadata.creationTime)])

                Text(
                    entry.workForm,
                    fontSize = 1.05.em,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 10.dp)
                )

                if (entry.comment.isNotBlank()) {
                    Text(
                        entry.comment,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        MarkBox()
        Spacer(modifier = Modifier.height(5.dp))
        TextProperty(DesktopLocale["browser.marks.subject"], entry.subject)
        TextProperty(DesktopLocale["browser.marks.work_form"], entry.workForm)
        TextProperty(DesktopLocale["browser.marks.period"], entry.period)
        if (entry.comment.isNotBlank()) {
            TextProperty(DesktopLocale["browser.marks.comment"], entry.comment)
        }
        if (entry.topic != null) {
            TextProperty(DesktopLocale["browser.marks.topic"], entry.topic!!)
        }
    }
}