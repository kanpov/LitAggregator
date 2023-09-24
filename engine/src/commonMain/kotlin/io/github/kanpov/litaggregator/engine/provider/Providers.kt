package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.Authorizer
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.asInstant
import java.time.*
import java.time.format.DateTimeFormatter

abstract class AuthorizedProvider<A : Authorizer, E : FeedEntry>(protected val authorizer: A) : SimpleProvider<E>()

abstract class SimpleProvider<E : FeedEntry> {
    suspend fun run(profile: Profile): Set<E>? {
        return try {
            provide(profile)
        } catch (e: Exception) {
            println(e.stackTraceToString())
            null
        }
    }

    protected abstract suspend fun provide(profile: Profile): Set<E>

    protected fun getRelevantDays(profile: Profile): Map<Instant, String> {
        val currentTime = LocalDateTime.now(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(3))) // moscow time

        return buildMap {
            for (offset in 0..profile.feedSettings.maxEntryDayAge) {
                val time = currentTime.minusDays(offset.toLong())

                if (!profile.identity.studiesOnSaturdays && time.dayOfWeek == DayOfWeek.SATURDAY) continue // people in heaven
                if (time.monthValue in 6..8) continue // summer holidays

                if (time.dayOfWeek != DayOfWeek.SUNDAY) { // regular weekend day
                    this += time.asInstant to DateTimeFormatter.ISO_LOCAL_DATE.format(time)
                }
            }
        }
    }
}

// definitions are essentially glue code needed in order to determine whether certain providers are applicable
interface SimpleProviderDefinition<E : FeedEntry> {
    val name: String
    val isEnabled: (ProviderSettings) -> Boolean
    val factory: () -> SimpleProvider<E>

    companion object {
        val all = setOf<SimpleProviderDefinition<out FeedEntry>>()
    }
}

interface AuthorizedProviderDefinition<A : Authorizer, E : FeedEntry> {
    val name: String
    val isEnabled: (ProviderSettings) -> Boolean
    val isAuthorized: (Authorization) -> Boolean
    val factory: (Authorization) -> AuthorizedProvider<A, E>

    companion object {
        val all = setOf<AuthorizedProviderDefinition<*, *>>(
            DnevnikRatingProvider.Definition
        )
    }
}
