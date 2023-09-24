package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.util.JsonInstant
import kotlinx.serialization.Serializable

@Serializable
data class EventFeedEntry(
    override val metadata: FeedEntryMetadata,
    val name: String,
    val organizer: String,
    val award: String?,
    val startTime: JsonInstant?,
    val endTime: JsonInstant?,
    val subject: String?
) : FeedEntry
