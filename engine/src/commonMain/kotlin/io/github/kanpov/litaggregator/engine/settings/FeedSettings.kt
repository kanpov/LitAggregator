package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

@Serializable
data class FeedSettings(
    var maxPoolSize: Int = 30,
    var lookBehindDays: Int = 5,
    var lookAheadDays: Int = 7
)
