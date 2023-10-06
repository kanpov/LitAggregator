package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.Authorizer
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.io.asInstant
import java.time.*
import java.time.format.DateTimeFormatter

abstract class AuthorizedProvider<A : Authorizer, E : FeedEntry>(protected val authorizer: A)
    : SimpleProvider<E>()

abstract class SimpleProvider<E : FeedEntry> {
    suspend fun run(profile: Profile): Boolean {
        return try {
            provide(profile)
            true
        } catch (e: Exception) {
            false
        }
    }

    protected abstract suspend fun provide(profile: Profile)

    protected fun getRelevantPastDays(profile: Profile): Map<Instant, String> {
        return getRelevantDays(profile, 0..profile.feedSettings.maxAgeOfNewEntries, plus = false)
    }

    protected fun getRelevantFutureDays(profile: Profile): Map<Instant, String> {
        return getRelevantDays(profile, 0..profile.feedSettings.lookAheadDays, plus = true)
    }

    protected inline fun <reified E : FeedEntry> insert(feed: Feed, entry: E): Boolean {
        // Check if there are any entries with the same source fingerprint as the given entry.
        // Because of the way this algorithm works, it is guaranteed that only 0 or 1 matches will ever
        // be present in the pool
        var matchingEntry: E? = null
        feed.withPool<E> { pool ->
            matchingEntry = pool.firstOrNull { it.sourceFingerprint == entry.sourceFingerprint }
        }

        // If the source fingerprint is unique (new), simply insert the entry into the pool
        if (matchingEntry == null) {
            feed.withPool<E> { pool ->
                pool.add(entry)
            }
            return false
        }

        // If the metadata fingerprints of the matching and new entry are different, a merge is needed
        if (matchingEntry!!.metadataFingerprint != entry.metadataFingerprint) {
            matchingEntry!!.metadata.merge(entry.metadata)
            return false
        }

        // If the content fingerprints of the matching and new entry are different, the matching entry needs to be
        // swapped out for the new entry
        if (matchingEntry!!.contentFingerprint != entry.contentFingerprint) {
            feed.withPool<E> { pool ->
                pool.remove(matchingEntry)
                pool.add(entry)
            }
            return false
        }

        // If all fingerprints are equal, both entries can only be equal, so the matching entry should be kept
        println("dupe ${entry.contentFingerprint}")
        return true
    }

    private fun getRelevantDays(profile: Profile, range: IntRange, plus: Boolean): Map<Instant, String> {
        val currentTime = LocalDateTime.now(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(3))) // moscow time

        return buildMap {
            for (offset in range) {
                val time = if (plus) currentTime.plusDays(offset.toLong()) else currentTime.minusDays(offset.toLong())

                if (!profile.identity.studiesOnSaturdays && time.dayOfWeek == DayOfWeek.SATURDAY) continue // people in heaven
                if (time.monthValue in 6..8) continue // summer holidays

                if (time.dayOfWeek != DayOfWeek.SUNDAY) { // regular weekday
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
    val factory: (Profile) -> SimpleProvider<E>
    val networkUsage: ProviderNetworkUsage

    companion object {
        val all = setOf<SimpleProviderDefinition<*>>(AnnouncementProvider.Definition)
    }
}

interface AuthorizedProviderDefinition<A : Authorizer, E : FeedEntry> : SimpleProviderDefinition<E> {
    val isAuthorized: (Authorization) -> Boolean

    companion object {
        val all = setOf<AuthorizedProviderDefinition<*, *>>(
        )
    }
}

enum class ProviderNetworkUsage {
    Low,
    Medium,
    High
}
