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
    val banners: MutableSet<BannerFeedEntry> = mutableSetOf(),
    val announcements: MutableSet<AnnouncementFeedEntry> = mutableSetOf(),
    val events: MutableSet<EventFeedEntry> = mutableSetOf(),
    val diagnostics: MutableSet<DiagnosticFeedEntry> = mutableSetOf()
) {
    val allPools: Map<String, MutableSet<out FeedEntry>>
        get() = mapOf("homework" to homework, "marks" to marks, "ratings" to ratings, "visits" to visits,
            "banners" to banners, "announcements" to announcements, "events" to events, "diagnostics" to diagnostics)

    inline fun <reified E : FeedEntry> withPool(action: (MutableSet<E>) -> Unit) {
        val subPool = when (E::class) {
            HomeworkFeedEntry::class -> homework
            MarkFeedEntry::class -> marks
            RatingFeedEntry::class -> ratings
            VisitFeedEntry::class -> visits
            BannerFeedEntry::class -> banners
            AnnouncementFeedEntry::class -> announcements
            EventFeedEntry::class -> events
            DiagnosticFeedEntry::class -> diagnostics
            else -> throw IllegalArgumentException()
        } as MutableSet<E>

        action(subPool)
    }
}

fun <T : FeedEntry> MutableSet<T>.sortedByRelevancy(): List<T> {
    return sortedBy { it.metadata.creationTime?.toEpochMilli() ?: Instant.MIN.toEpochMilli() }
}
