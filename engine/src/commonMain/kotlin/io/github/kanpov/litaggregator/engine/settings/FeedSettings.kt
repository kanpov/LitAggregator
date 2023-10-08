package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

@Serializable
data class FeedSettings(
    val maxPoolSize: Int = 30,
    val maxAgeOfArchiveEntries: Int = 14,
    val maxAgeOfNewEntries: Int = 7,
    val lookAheadDays: Int = 2
)
