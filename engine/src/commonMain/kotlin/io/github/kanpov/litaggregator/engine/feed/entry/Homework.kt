package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.util.JsonInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class HomeworkFeedEntry(
    val plain: String,
    val html: String?,
    val subject: String,
    val teacher: String,
    val assignedTime: JsonInstant,
    val attachments: List<String>,
    val allowsSubmissions: Boolean,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val contentParams: List<*> = listOf(plain, html, subject, teacher, assignedTime, attachments, allowsSubmissions),
) : FeedEntry
