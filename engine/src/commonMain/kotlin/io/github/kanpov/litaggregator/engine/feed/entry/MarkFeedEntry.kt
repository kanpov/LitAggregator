package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MarkFeedEntry(
    val subject: String,
    val value: Int,
    val weight: Int,
    val isExam: Boolean,
    val comment: String,
    val topic: String?,
    val workForm: String,
    val period: String,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val contentParams: List<*> = listOf(subject, value, weight, isExam, comment, topic, workForm, period)
) : FeedEntry
