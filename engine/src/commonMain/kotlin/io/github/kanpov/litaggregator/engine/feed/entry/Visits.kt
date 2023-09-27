package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.util.JsonInstant
import io.github.kanpov.litaggregator.engine.util.differenceFrom
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class VisitFeedEntry(
    val entryTime: JsonInstant,
    val exitTime: JsonInstant,
    val stayDuration: JsonInstant = entryTime.differenceFrom(exitTime),
    val irregularPattern: Boolean,
    val fullAddress: String,
    val shortAddress: String,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val contentParams: List<*> = listOf(entryTime, exitTime, stayDuration, irregularPattern, fullAddress, shortAddress)
) : FeedEntry
