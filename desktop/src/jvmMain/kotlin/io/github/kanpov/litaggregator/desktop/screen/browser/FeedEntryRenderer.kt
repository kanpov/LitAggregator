package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.desktop.screen.restartScreen
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryAttachment
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.net.URI

abstract class FeedEntryRenderer<T : FeedEntry>(protected val entry: T) {
    @Composable
    fun Render(manager: ProfileManager) {
        var showDetails by remember { mutableStateOf(false) }
        var borderColor by remember { mutableStateOf(if (entry.metadata.seenBefore) Color.Gray else Color.Black) }

        Surface(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.clickable {
                if (!entry.metadata.seenBefore) {
                    entry.metadata.seenBefore = true
                    borderColor = Color.LightGray
                }
                showDetails = !showDetails
            }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                PreviewMetadata(manager) { showDetails = true }
                PreviewContent()
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
                                DetailedContent()
                                DetailedMetadata()
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun PreviewMetadata(manager: ProfileManager, toggleShowDetails: () -> Unit) {
        val navigator = LocalNavigator.currentOrThrow

        Row(modifier = Modifier.fillMaxWidth()) {
            // source
            Text(
                entry.metadata.sourceName,
                fontSize = 1.02.em,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            // whether is starred
            var starred by remember { mutableStateOf(entry.metadata.starred) }
            BasicIcon(
                painter = if (starred) painterResource("icons/star_filled.png") else painterResource("icons/star.png"),
                size = 25.dp,
                modifier = Modifier.clickable {
                    starred = !starred
                    entry.metadata.starred = starred
                }
            )
            // whether is commented
            if (entry.metadata.comment.isNotBlank()) {
                BasicIcon(
                    painter = painterResource("icons/comment.png"),
                    size = 25.dp,
                    modifier = Modifier.padding(start = 5.dp).clickable {
                        toggleShowDetails()
                    }
                )
            }
            // delete entry
            BasicIcon(
                painter = painterResource("icons/delete.png"),
                size = 25.dp,
                modifier = Modifier.padding(start = 5.dp).clickable {
                    manager.withProfile {
                        feed.removeEntry(entry)
                    }
                    restartScreen(navigator)
                }
            )
        }

        Divider(modifier = Modifier.fillMaxWidth().width(2.dp).padding(top = 2.dp), color = Color.Black)
        Spacer(modifier = Modifier.height(3.dp))
    }

    @Composable
    private fun ColumnScope.DetailedMetadata() {
        // heading
        Text(
            DesktopLocale["browser.misc.about_entry"],
            fontWeight = FontWeight.Medium,
            fontSize = 1.05.em,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // creation time
        if (entry.metadata.creationTime != null) {
            TextProperty(
                DesktopLocale["browser.misc.creation_time"],
                TimeFormatters.longMeshDateTime.format(entry.metadata.creationTime!!)
            )
        }
        // source
        TextProperty(DesktopLocale["browser.misc.source_name"], entry.metadata.sourceName)
        // comment editor
        Text(
            DesktopLocale["browser.misc.comment"],
            fontWeight = FontWeight.Medium
        )
        var comment by remember { mutableStateOf(entry.metadata.comment) }
        BasicTextField(
            value = comment,
            onValueChange = {
                comment = it
                entry.metadata.comment = comment
            },
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
        )
    }


    @Composable
    abstract fun ColumnScope.PreviewContent()

    @Composable
    abstract fun ColumnScope.DetailedContent()

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
    protected fun TextProperty(title: String, value: String) {
        BaseProperty(title) { Text(value) }
    }

    @Composable
    protected fun LinkProperty(title: String, url: String, text: String? = null) {
        BaseProperty(title) { Link(text, url) }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    protected fun Link(text: String? = null, url: String, modifier: Modifier = Modifier) {
        val linkText = if (text == null || text == url) {
            URI.create(url).path
        } else {
            text
        }
        Text(linkText, textDecoration = TextDecoration.Underline, color = Color.Blue, modifier = modifier.onPointerEvent(
            PointerEventType.Press) {
            DesktopEnginePlatform.openBrowser(URI.create(url))
        })
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    protected fun AttachmentList(attachments: List<FeedEntryAttachment>) {
        Text(DesktopLocale["browser.misc.attachments"], fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(5.dp))
        for (attachment in attachments) {
            Row {
                BasicIcon(
                    painter = painterResource("icons/dot.png"),
                    size = 10.dp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                val text = if (attachment.title != null) attachment.title!! else null
                Link(text = text, url = attachment.downloadUrl, modifier = Modifier.padding(start = 5.dp))
            }
        }
    }

    @Composable
    protected fun ColoredFrame(color: Color, modifier: Modifier = Modifier, width: Dp = 50.dp, height: Dp = 50.dp,
                               content: @Composable () -> Unit) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            border = BorderStroke(1.dp, color),
            modifier = modifier.size(width, height)
        ) {
            content()
        }
    }

    @Composable
    protected fun DetailedSubHeading(text: String) {
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 1.05.em,
            modifier = Modifier.padding(start = 100.dp)
        )
    }

    @Composable
    protected fun ColumnScope.PreviewHeading(text: String) {
        Text(
            text,
            fontSize = 1.1.em,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    protected fun String.normalizePlainTextFromHtml(stripNewLines: Boolean = false): String {
        return StringEscapeUtils.unescapeHtml4(this).replace("\n", if (stripNewLines) " " else "\n")
    }

    protected fun getRewardColor(place: Int): Color = when (place) {
        1 -> Color.Gold
        2 -> Color.Silver
        3 -> Color.Bronze
        else -> Color.Black
    }
}
