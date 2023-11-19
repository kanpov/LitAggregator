package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

@Serializable
data class FeedSettings(
    var maxPoolSize: Int = 30,
    var maxAgeOfArchiveEntries: Int = 14,
    var maxAgeOfNewEntries: Int = 7,
    var lookAheadDays: Int = 2
)
