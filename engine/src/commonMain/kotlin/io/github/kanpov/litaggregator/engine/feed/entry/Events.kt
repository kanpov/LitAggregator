package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.util.JsonInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class EventFeedEntry(
    val name: String,
    val organizer: String,
    val award: String?,
    val startTime: JsonInstant?,
    val endTime: JsonInstant?,
    val subject: String?,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata = FeedEntryMetadata(),
    @Transient override val contentParams: List<*> = listOf(name, organizer, award, startTime, endTime, subject)
) : FeedEntry
