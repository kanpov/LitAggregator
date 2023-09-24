package io.github.kanpov.litaggregator.engine.feed

import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticFeedEntry(
    override val metadata: FeedEntryMetadata,
    val subject: String,
    val place: Int,
    val maxResult: Int,
    val yourResult: Int,
    val comparisonToRegion: DiagnosticResultComparison?,
    val comparisonToSchool: DiagnosticResultComparison?,
    val comparisonToGroup: DiagnosticResultComparison?
) : FeedEntry

@Serializable
data class DiagnosticResultComparison(
    val sameResultAmount: Int,
    val bestResultAmount: Int,
    val percentile: Float
)
