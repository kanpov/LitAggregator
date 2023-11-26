package io.github.kanpov.litaggregator.engine.feed.entry

import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DiagnosticFeedEntry(
    val years: String,
    val subject: String,
    val place: Int,
    val maxResult: Int,
    val yourResult: Int,
    val yourLevel: String,
    val comparisonToRegion: DiagnosticResultComparison?,
    val comparisonToSchool: DiagnosticResultComparison?,
    val comparisonToGroup: DiagnosticResultComparison?,
    override val sourceFingerprint: String,
    override val metadata: FeedEntryMetadata,
    @Transient override val contentParams: List<*> = listOf(subject, place, maxResult, yourResult, comparisonToRegion, comparisonToSchool, comparisonToGroup)
) : FeedEntry

@Serializable
data class DiagnosticResultComparison(
    val sameResultAmount: Int,
    val firstPlaceAmount: Int,
    val secondPlaceAmount: Int,
    val thirdPlaceAmount: Int,
    val percentile: Float
)
