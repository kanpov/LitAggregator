package io.github.kanpov.litaggregator.engine.feed

import kotlinx.serialization.Serializable

@Serializable
data class RatingFeedEntry(
    override val metadata: FeedEntryMetadata,
    val overallRating: Rating,
    val perSubjectRatings: Map<String, Rating>,
    val classmateRatings: Map<String, Rating>?
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
    Decreasing
}
