package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.data.*
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val identity: Identity,
    val providers: ProviderSettings,
    val authorization: Authorization,
    val feedSettings: FeedSettings,
    val feed: Feed
)
