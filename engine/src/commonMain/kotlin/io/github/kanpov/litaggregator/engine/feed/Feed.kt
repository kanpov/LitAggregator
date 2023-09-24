package io.github.kanpov.litaggregator.engine.feed

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val homework: MutableList<HomeworkFeedEntry> = mutableListOf(),
    val marks: MutableList<MarkFeedEntry> = mutableListOf(),
    val ratings: MutableList<RatingFeedEntry> = mutableListOf(),
    val visits: MutableList<VisitFeedEntry> = mutableListOf(),
    val banners: MutableList<BannerFeedEntry> = mutableListOf(),
    val announcements: MutableList<AnnouncementFeedEntry> = mutableListOf(),
    val events: MutableList<EventFeedEntry> = mutableListOf(),
    val diagnostics: MutableList<DiagnosticFeedEntry> = mutableListOf()
)
