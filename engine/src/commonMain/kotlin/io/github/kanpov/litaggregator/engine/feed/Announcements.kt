package io.github.kanpov.litaggregator.engine.feed

import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementFeedEntry(
    override val metadata: FeedEntryMetadata,
    val title: String,
    val content: String,
    val categories: List<String>
) : FeedEntry
