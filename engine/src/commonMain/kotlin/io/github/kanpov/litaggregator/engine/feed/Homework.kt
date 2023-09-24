package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.util.JsonInstant
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkFeedEntry(
    override val metadata: FeedEntryMetadata,
    val plain: String,
    val html: String?,
    val subject: String,
    val teacher: String,
    val assignedTime: JsonInstant,
    val attachments: List<String>,
    val allowsSubmissions: Boolean
) : FeedEntry
