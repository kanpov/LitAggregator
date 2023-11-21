package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorization.UlyssesAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntryAttachment
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorization.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import io.github.kanpov.litaggregator.engine.util.io.jFlagBoolean
import io.github.kanpov.litaggregator.engine.util.io.jInt
import io.github.kanpov.litaggregator.engine.util.io.jString
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import java.time.Instant
import java.time.LocalDateTime

class UlyssesProvider(authorizer: UlyssesAuthorizer) : AuthorizedProvider<UlyssesAuthorizer, HomeworkFeedEntry>(authorizer) {
    override suspend fun provide(profile: Profile) {
        val schoolYear = getCurrentSchoolYear()
        val subjects = authorizer.getJsonArray<JsonObject>("https://in.lit.msu.ru/api/v1/Ulysses/$schoolYear/")!!
        val (earliestTime, _) = getRelevantPastDays(profile).entries.last()

        for (subjectObj in subjects) {
            val studyGrade = subjectObj.jInt("study_grade")
            val subjectId = subjectObj.jInt("subject_id")
            val teacherId = subjectObj.jInt("teacher_id")

            if (studyGrade != profile.identity.parallel) continue

            val homeworks = authorizer.getJsonArray<JsonObject>(
                "https://in.lit.msu.ru/api/v1/Ulysses/$schoolYear/$studyGrade/$subjectId/$teacherId")!!

            for (homeworkObj in homeworks) {
                addHomework(profile, homeworkObj, earliestTime)
            }
        }
    }

    private fun addHomework(profile: Profile, homeworkObj: JsonObject, earliestTime: Instant) {
        val title = homeworkObj.jString("title")
        val cleanContent = homeworkObj.jString("body_clean")
        val htmlContent = homeworkObj.jString("body")
        val creationTime = TimeFormatters.isoGlobalDateTime.parse(homeworkObj.jString("Post date"), Instant::from)
        val attachments = buildList {
            val attachmentUrls = if (homeworkObj["attachments"] is JsonNull) {
                emptyList()
            } else {
                homeworkObj.jString("attachments").split(";")
            }

            for (attachmentUrl in attachmentUrls) {
                this += FeedEntryAttachment(
                    downloadUrl = attachmentUrl,
                    title = null,
                    thumbnailUrl = null
                )
            }
        }

        if (creationTime.isBefore(earliestTime)) return

        profile.providers.ulysses!!.apply {
            if (!inclusions.studyMaterials && homeworkObj.jFlagBoolean("прикреплено")) return
            if (!inclusions.hidden && !homeworkObj.jFlagBoolean("опубликовано")) return
            val solelyForOtherGroups = profile.identity.classNames.none { title.contains(it) }
                    && profile.identity.otherClassNames.any { title.contains(it)  }
            if (!inclusions.solelyForOtherGroups && solelyForOtherGroups) return

            if (!filters.titleFilter.match(title) || !filters.cleanContentFilter.match(cleanContent)
                || !filters.htmlContentFilter.match(htmlContent)) return
        }

        insert(profile.feed, HomeworkFeedEntry(
            title = title,
            plain = cleanContent,
            html = htmlContent,
            subject = homeworkObj.jString("subject"),
            teacher = homeworkObj.jString("teacher_fullname"),
            assignedTime = null,
            attachments = attachments,
            allowsSubmissions = false,
            sourceFingerprint = homeworkObj.jString("Post date"),
            metadata = FeedEntryMetadata(creationTime = creationTime)
        ))
    }

    private fun getCurrentSchoolYear(): String {
        val time = LocalDateTime.now(TimeFormatters.zid)
        val offset = if (time.monthValue < 9) 0 else 1
        return "${time.year - 1 + offset}-${time.year + offset}"
    }

    object Definition : AuthorizedProviderDefinition<UlyssesAuthorizer, HomeworkFeedEntry> {
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.ulysses != null }
        override val name: String = "Учебные материалы из УЛИСС"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.ulysses != null }
        override val factory: (Profile) -> SimpleProvider<HomeworkFeedEntry> = { UlyssesProvider(it.authorization.ulysses!!) }
    }
}
