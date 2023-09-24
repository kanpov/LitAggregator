package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.settings.*
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val identity: IdentitySettings,
    val providers: ProviderSettings,
    val authorization: Authorization,
    val feedSettings: FeedSettings,
    val feed: Feed
)
