package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

@Serializable
data class FeedSettings(
    val maxEntryAmount: Int = 1000,
    val maxEntryDayAge: Int = 21,
    val globalExclusions: List<String> = emptyList()
)
