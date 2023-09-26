package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.util.JsonInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class VisitFeedEntry(
    val entryTime: JsonInstant,
    val exitTime: JsonInstant,
    val stayDuration: JsonInstant,
    val irregularPattern: Boolean,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val contentParams: List<*> = listOf(entryTime, exitTime, stayDuration, irregularPattern)
) : FeedEntry
