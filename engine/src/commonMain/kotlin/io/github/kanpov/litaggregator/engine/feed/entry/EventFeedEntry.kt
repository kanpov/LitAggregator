package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.util.io.JsonInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class EventFeedEntry(
    val name: String,
    val organizer: String,
    val reward: String,
    val startTime: JsonInstant?,
    val endTime: JsonInstant?,
    val subject: String?,
    val participantCategory: String?,
    val profession: String?,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val contentParams: List<*> = listOf(name, organizer, reward, startTime, endTime, subject)
) : FeedEntry
