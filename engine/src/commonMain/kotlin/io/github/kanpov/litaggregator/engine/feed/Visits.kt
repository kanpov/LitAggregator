package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.util.JsonInstant
import kotlinx.serialization.Serializable

@Serializable
data class VisitFeedEntry(
    override val metadata: FeedEntryMetadata,
    val entryTime: JsonInstant,
    val exitTime: JsonInstant,
    val stayDuration: JsonInstant,
    val irregularPattern: Boolean
) : FeedEntry
