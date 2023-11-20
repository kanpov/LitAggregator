package io.github.kanpov.litaggregator.engine.profile

import co.touchlab.kermit.Logger
import io.github.kanpov.litaggregator.engine.authorizer.*
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.feed.sortedByRelevancy
import io.github.kanpov.litaggregator.engine.provider.AuthorizedProviderDefinition
import io.github.kanpov.litaggregator.engine.provider.SimpleProviderDefinition
import io.github.kanpov.litaggregator.engine.settings.*
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    var identity: IdentitySettings,
    var providers: ProviderSettings,
    var authorization: AuthorizationState,
    var feedSettings: FeedSettings,
    val feed: Feed
) {
    suspend fun setupAuthorizer(authorizer: Authorizer): Boolean {
        if (!authorizer.authorize()) return false

        when (authorizer) {
            is UlyssesAuthorizer -> this.authorization.ulyss = authorizer
            is MeshAuthorizer -> this.authorization.mos = authorizer
            is GoogleAuthorizer -> this.authorization.google = authorizer
        }

        return true
    }

    suspend fun refreshFeed(): Set<String> {
        val errors = mutableSetOf<String>()
        var runProviders = 0
        Logger.i { "Feed refresh has been started" }

        SimpleProviderDefinition.all.forEach { definition ->
            if (definition.isEnabled(this.providers)) {
                if (!definition.factory(this).run(this)) {
                    Logger.i { "Simple provider ${definition.name} has failed" }
                    errors += definition.name
                } else {
                    Logger.i { "Simple provider ${definition.name} was successful" }
                }
                runProviders++
            }
        }

        AuthorizedProviderDefinition.all.forEach { definition ->
            if (definition.isEnabled(this.providers) && definition.isAuthorized(this.authorization)) {
                if (!definition.factory(this).run(this)) {
                    Logger.i { "Authorized provider ${definition.name} has failed" }
                    errors += definition.name
                } else {
                    Logger.i { "Authorized provider ${definition.name} was successful" }
                }
                runProviders++
            }
        }

        if (errors.isEmpty()) {
            Logger.i { "Feed refresh has completed without any errors in $runProviders configured provider(s)" }
        } else {
            Logger.i { "Feed refresh has completed with errors in the following of $runProviders configured provider(s): ${errors.joinToString()}" }
        }

        shrinkFeed()
        return errors
    }

    private fun shrinkFeed() {
        for ((poolName, pool) in this.feed.allPools) {
            val entries = pool.sortedByRelevancy()

            if (entries.size <= this.feedSettings.maxPoolSize) continue

            val diff = entries.size - this.feedSettings.maxPoolSize
            for (i in this.feedSettings.maxPoolSize..<this.feedSettings.maxPoolSize + diff) {
                pool.remove(entries[i])
            }

            Logger.i { "Shrunk pool of $poolName by $diff entries" }
        }
    }
}
