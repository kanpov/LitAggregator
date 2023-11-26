package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.feed.entry.*
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Feed(
    val homework: MutableSet<HomeworkFeedEntry> = mutableSetOf(),
    val marks: MutableSet<MarkFeedEntry> = mutableSetOf(),
    val ratings: MutableSet<RatingFeedEntry> = mutableSetOf(),
    val visits: MutableSet<VisitFeedEntry> = mutableSetOf(),
    val announcements: MutableSet<AnnouncementFeedEntry> = mutableSetOf(),
    val events: MutableSet<EventFeedEntry> = mutableSetOf(),
    val diagnostics: MutableSet<DiagnosticFeedEntry> = mutableSetOf()
) {
    val allPools: Map<String, MutableSet<out FeedEntry>>
        get() = mapOf("homework" to homework, "marks" to marks, "ratings" to ratings, "visits" to visits,
            "announcements" to announcements, "events" to events, "diagnostics" to diagnostics)

    inline fun <reified E : FeedEntry> withPool(action: (MutableSet<E>) -> Unit) {
        val subPool = when (E::class) {
            HomeworkFeedEntry::class -> homework
            MarkFeedEntry::class -> marks
            RatingFeedEntry::class -> ratings
            VisitFeedEntry::class -> visits
            AnnouncementFeedEntry::class -> announcements
            EventFeedEntry::class -> events
            DiagnosticFeedEntry::class -> diagnostics
            else -> throw IllegalArgumentException()
        } as MutableSet<E>

        action(subPool)
    }

    fun countEntries(): Int {
        var result = 0

        for ((_, set) in allPools) {
            result += set.size
        }

        return result
    }

    fun performQuery(query: FeedQuery): List<FeedEntry> {
        // filter out pools that aren't mentioned in the query
        val combinedPool = buildSet {
            for ((id, pool) in allPools) {
                if (query.filterPools.contains(id)) {
                    addAll(pool)
                }
            }
        }

        // filter out entries that don't contain the filter text
        val filteredPool = combinedPool.filter { entry ->
            entry.contentParams.any { param -> param.toString().contains(query.filterText) }
        }

        // order the matching entries using the query's sort options
        val sortedPool = when (query.sortOrder) {
            FeedSortOrder.Ascending -> filteredPool.sortedBy { query.sortParameter.element(it) as Comparable<Any> }
            FeedSortOrder.Descending -> filteredPool.sortedByDescending { query.sortParameter.element(it) as Comparable<Any> }
        }.toMutableList()

        return sortedPool
    }

    companion object {
        val poolIds = setOf("homework", "marks", "ratings", "visits", "announcements", "events", "diagnostics")
    }
}

fun <T : FeedEntry> MutableSet<T>.sortedByRelevancy(): List<T> {
    return sortedBy { it.metadata.creationTime?.epochSecond?.minus(Instant.MIN.epochSecond) }
}
