package io.github.kanpov.litaggregator.engine.feed

import kotlinx.serialization.Serializable

@Serializable
data class BannerFeedEntry(
    override val metadata: FeedEntryMetadata,
    val leftImageUrl: String,
    val rightImageUrl: String,
    val targetAudience: String,
    val text: String,
    val textColor: String,
    val backgroundColor: String,
    val outgoingUrl: String?
) : FeedEntry
