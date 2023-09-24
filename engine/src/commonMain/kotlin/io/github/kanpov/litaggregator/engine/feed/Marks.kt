package io.github.kanpov.litaggregator.engine.feed

import kotlinx.serialization.Serializable

@Serializable
data class MarkFeedEntry(
    override val metadata: FeedEntryMetadata,
    val subject: String,
    val value: Int,
    val weight: Int,
    val isExam: Boolean,
    val comment: String,
    val topic: String,
    val task: String,
    val period: String
) : FeedEntry
