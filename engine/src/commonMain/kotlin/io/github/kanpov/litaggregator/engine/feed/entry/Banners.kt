package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class BannerFeedEntry(
    val leftImageUrl: String,
    val rightImageUrl: String,
    val targetAudience: String,
    val text: String,
    val textColor: String,
    val backgroundColor: String,
    val outgoingUrl: String?,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val signParams: List<*> = listOf(leftImageUrl, rightImageUrl, targetAudience, text, textColor, backgroundColor, outgoingUrl)
) : FeedEntry
