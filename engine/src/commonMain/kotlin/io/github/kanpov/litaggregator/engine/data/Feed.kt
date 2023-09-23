package io.github.kanpov.litaggregator.engine.data

import io.github.kanpov.litaggregator.engine.util.JsonInstant
import io.github.kanpov.litaggregator.engine.util.JsonUuid
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Feed(
    val homework: MutableSet<HomeworkFeedEntry> = mutableSetOf(),
    val marks: MutableSet<MarkFeedEntry> = mutableSetOf(),
    val ratings: MutableSet<RatingFeedEntry> = mutableSetOf(),
    val visits: MutableSet<VisitFeedEntry> = mutableSetOf(),
    val banners: MutableSet<BannerFeedEntry> = mutableSetOf(),
    val announcements: MutableSet<AnnouncementFeedEntry> = mutableSetOf(),
    val timetables: MutableSet<TimetableFeedEntry> = mutableSetOf(),
    val events: MutableSet<EventFeedEntry> = mutableSetOf(),
    val diagnostics: MutableSet<DiagnosticFeedEntry> = mutableSetOf()
)

interface FeedEntry {
    val metadata: FeedEntryMetadata
}

@Serializable
data class FeedEntryMetadata(
    val uuid: JsonUuid = UUID.randomUUID(),
    val creationTime: JsonInstant,
    val updateTime: JsonInstant? = null,
    val comments: List<String> = emptyList(),
    val markers: List<String> = emptyList(),
    val attachments: List<FeedEntryAttachment> = emptyList(),
    val taskLists: List<FeedEntryTaskList> = emptyList(),
    val starred: Boolean = false,
    val pinned: Boolean = false
)

@Serializable
data class FeedEntryTaskList(
    val name: String,
    val tasks: Map<String, Boolean>,
    val definesOverallCompletion: Boolean
)

@Serializable
data class FeedEntryAttachment(
    val downloadUrl: String,
    val name: String,
    val thumbnailUrl: String?
)

@Serializable
data class HomeworkFeedEntry(
    override val metadata: FeedEntryMetadata,
    val plain: String,
    val html: String?,
    val subject: String,
    val teacher: String,
    val assignedTime: JsonInstant,
    val attachments: List<String>,
    val allowsSubmissions: Boolean
) : FeedEntry

@Serializable
data class MarkFeedEntry(
    override val metadata: FeedEntryMetadata,
    val subject: String,
    val value: Int,
    val weight: Int,
    val isExam: Boolean,
    val comment: String,
    val topic: String,
    val task: String,
    val period: String
) : FeedEntry

@Serializable
data class RatingFeedEntry(
    override val metadata: FeedEntryMetadata,
    val overallRating: Rating,
    val perSubjectRatings: Map<String, Rating>,
    val classmateRatings: Map<String, Rating>?
) : FeedEntry

@Serializable
data class Rating(
    val averageMark: Float,
    val rankPlace: Int,
    val trend: RatingTrend
)

@Serializable
enum class RatingTrend {
    Stable,
    Increasing,
    Decreasing
}

@Serializable
data class VisitFeedEntry(
    override val metadata: FeedEntryMetadata,
    val entryTime: JsonInstant,
    val exitTime: JsonInstant,
    val stayDuration: JsonInstant,
    val irregularPattern: Boolean
) : FeedEntry

@Serializable
data class BannerFeedEntry(
    override val metadata: FeedEntryMetadata,
    val leftImageUrl: String,
    val rightImageUrl: String,
    val targetAudience: String,
    val text: String,
    val textColor: String,
    val backgroundColor: String,
    val outgoingUrl: String?
) : FeedEntry

@Serializable
data class AnnouncementFeedEntry(
    override val metadata: FeedEntryMetadata,
    val title: String,
    val content: String,
    val categories: List<String>
) : FeedEntry

@Serializable
data class TimetableFeedEntry(
    override val metadata: FeedEntryMetadata,
    val temporary: Boolean,
    val days: List<TimetableDay>
) : FeedEntry

@Serializable
data class TimetableDay(
    val day: Int,
    val lessons: List<TimetableLesson>
)

@Serializable
data class TimetableLesson(
    val name: String,
    val beginTime: TimetableTime,
    val endTime: TimetableTime,
    val subject: String
)

@Serializable
data class TimetableTime(
    val hour: Int,
    val minute: Int
)

@Serializable
data class EventFeedEntry(
    override val metadata: FeedEntryMetadata,
    val name: String,
    val organizer: String,
    val award: String?,
    val startTime: JsonInstant?,
    val endTime: JsonInstant?,
    val subject: String?
) : FeedEntry

@Serializable
data class DiagnosticFeedEntry(
    override val metadata: FeedEntryMetadata,
    val subject: String,
    val place: Int,
    val maxResult: Int,
    val yourResult: Int,
    val comparisonToRegion: DiagnosticResultComparison?,
    val comparisonToSchool: DiagnosticResultComparison?,
    val comparisonToGroup: DiagnosticResultComparison?
) : FeedEntry

@Serializable
data class DiagnosticResultComparison(
    val sameResultAmount: Int,
    val bestResultAmount: Int,
    val percentile: Float
)
