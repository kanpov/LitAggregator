package io.github.kanpov.litaggregator.engine.feed

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
    fun <E : FeedEntry> insert(entry: E) {
        when (entry) {
            is HomeworkFeedEntry -> homework += entry
            is MarkFeedEntry -> marks += entry
            is RatingFeedEntry -> ratings += entry
            is VisitFeedEntry -> visits += entry
            is BannerFeedEntry -> banners += entry
            is AnnouncementFeedEntry -> announcements += entry
            is EventFeedEntry -> events += entry
            is DiagnosticFeedEntry -> diagnostics += entry
        }
    }
}
