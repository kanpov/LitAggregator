package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.feed.entry.*
import kotlinx.serialization.Serializable

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
