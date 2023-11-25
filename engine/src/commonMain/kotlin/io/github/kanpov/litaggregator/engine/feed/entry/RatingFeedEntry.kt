package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class RatingFeedEntry(
    val overallRating: Rating,
    val perSubjectRatings: Map<String, Rating>,
    val classmateRatings: Map<String, Rating>?,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val contentParams: List<*> = listOf(overallRating, perSubjectRatings, classmateRatings)
) : FeedEntry

@Serializable
data class Rating(
    val averageMark: Float,
    val rankPlace: Int,
    val trend: RatingTrend
)

@Serializable
enum class RatingTrend {
    Stable,
    Increasing,
    Decreasing;

    companion object {
        fun parse(value: String) = when (value) {
            "up" -> Increasing
            "down" -> Decreasing
            "stable" -> Stable
            else -> throw IllegalArgumentException()
        }
    }
}
