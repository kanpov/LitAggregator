package io.github.kanpov.litaggregator.desktop.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryAttachment
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.MarkFeedEntry
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.net.URI
import java.net.URLDecoder

@Composable
fun ColumnScope.BaseEntry(entry: FeedEntry, detailedContent: @Composable ColumnScope.() -> Unit, content: @Composable ColumnScope.() -> Unit) {
    var showDetails by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.clickable {
            showDetails = !showDetails
        }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            content()
        }
    }

    if (!showDetails) return

    Dialog(
        onDismissRequest = { showDetails = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.width(700.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                item(null) {
                    SelectionContainer {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Content
                            detailedContent()

                            // Metadata
                            Text(
                                Locale["browser.misc.about_entry"],
                                fontWeight = FontWeight.Medium,
                                fontSize = 1.05.em,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            TextProperty(Locale["browser.misc.creation_time"],
                                TimeFormatters.isoLocalDateTime.format(entry.metadata.creationTime))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.HomeworkEntry(entry: HomeworkFeedEntry) {
    BaseEntry(entry, detailedContent = {
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
    }) {
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
}

@Composable
fun ColumnScope.MarkEntry(entry: MarkFeedEntry) {
    BaseEntry(entry, detailedContent = {

    }) {
        Row {
            // mark surface
            Surface(
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.size(50.dp)
            ) {
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
    }
}

// --- UTILS ---

fun String.normalizePlainTextFromHtml(stripNewLines: Boolean = false): String {
    return StringEscapeUtils.unescapeHtml4(this).replace("\n", if (stripNewLines) " " else "\n")
}

@Composable
private fun BaseProperty(title: String, value: @Composable () -> Unit) {
    Row {
        Text(title, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(5.dp))
        value()
    }
    Spacer(modifier = Modifier.height(5.dp))
}

@Composable
private fun TextProperty(title: String, value: String) {
    BaseProperty(title) { Text(value) }
}

@Composable
private fun LinkProperty(title: String, url: String, text: String? = null) {
    BaseProperty(title) { Link(text, url) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Link(text: String? = null, url: String, modifier: Modifier = Modifier) {
    val linkText = text ?: URLDecoder.decode(url)
    Text(linkText, textDecoration = TextDecoration.Underline, color = Color.Blue, modifier = modifier.onPointerEvent(
        PointerEventType.Press) {
        DesktopEnginePlatform.openBrowser(URI.create(url))
    })
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AttachmentList(attachments: List<FeedEntryAttachment>) {
    Text(Locale["browser.misc.attachments"], fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.width(5.dp))
    for (attachment in attachments) {
        Row {
            BasicIcon(
                painter = painterResource("icons/dot.png"),
                size = 10.dp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            val text = if (attachment.title != null) attachment.title!! else attachment.downloadUrl
            Link(text = text, url = attachment.downloadUrl, modifier = Modifier.padding(start = 5.dp))
        }
    }
}
