package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.Authorizer
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings

abstract class AuthorizedProvider<A : Authorizer>(protected val authorizer: A) : SimpleProvider()

abstract class SimpleProvider {
    suspend fun run(profile: Profile, entries: MutableList<out FeedEntry>): Boolean {
        return try {
            provide(profile, entries)
            true
        } catch (_: Exception) {
            false
        }
    }

    protected abstract suspend fun provide(profile: Profile, entries: MutableList<out FeedEntry>)
}

// definitions are essentially glue code needed in order to determine whether certain providers are applicable
interface SimpleProviderDefinition<E : FeedEntry> {
    val name: String
    val isEnabled: (ProviderSettings) -> Boolean
    val entries: (Feed) -> MutableList<E>
    val factory: () -> SimpleProvider

    companion object {
        val all = setOf<SimpleProviderDefinition<out FeedEntry>>()
    }
}

interface AuthorizedProviderDefinition<E : FeedEntry, A : Authorizer> {
    val name: String
    val isEnabled: (ProviderSettings) -> Boolean
    val isAuthorized: (Authorization) -> Boolean
    val entries: (Feed) -> MutableList<E>
    val factory: (Authorization) -> AuthorizedProvider<A>

    companion object {
        val all = setOf<AuthorizedProviderDefinition<out FeedEntry, out Authorizer>>(
            UlyssProvider.Definition
        )
    }
}
