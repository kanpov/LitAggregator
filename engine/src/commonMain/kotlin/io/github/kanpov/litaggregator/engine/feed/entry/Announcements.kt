package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AnnouncementFeedEntry(
    val title: String,
    val content: String,
    val categories: List<String>,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata = FeedEntryMetadata(),
    @Transient override val contentParams: List<*> = listOf(title, content, categories),
) : FeedEntry
