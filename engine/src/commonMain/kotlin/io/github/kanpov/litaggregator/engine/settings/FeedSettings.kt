package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

@Serializable
data class FeedSettings(
    val maxTotalEntryAmount: Int = 200,
    val maxPoolEntryAmount: Int = 30,
    val maxAgeOfArchiveEntries: Int = 14,
    val maxAgeOfNewEntries: Int = 7,
    val globalExclusions: List<String> = emptyList()
)
