package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorization.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryAttachment
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorization.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.*
import io.github.kanpov.litaggregator.engine.util.io.*
import java.time.Instant

class MeshHomeworkProvider(authorizer: MeshAuthorizer) : MeshProvider<HomeworkFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        val days = getRelevantPastDays(profile).values.joinToString(separator = "%2C") // url encode days
        val daySchedules = authorizer.getJsonArrayFromPayload(
            "https://school.mos.ru/api/family/web/v1/schedule/short?student_id=${studentInfo.profileId}&dates=$days"
        )!!

        for (scheduleObj in daySchedules) {
            for (lessonObj in scheduleObj.jArray("lessons")) {
                val educationType = lessonObj.jString("lesson_education_type")

                if (educationType != "OO" && profile.providers.meshHomework!!.onlyIncludeOO) continue

                val itemId = lessonObj.jInt("schedule_item_id")
                provideItem(educationType, itemId, profile, studentInfo)
            }
        }
    }

    private suspend fun provideItem(educationType: String, itemId: Int, profile: Profile, studentInfo: MeshStudentInfo) {
        val itemObj = authorizer.getJson(
            "https://school.mos.ru/api/family/web/v1/lesson_schedule_items/$itemId?student_id=${studentInfo.profileId}&type=$educationType"
        )!!

        val teacherName = itemObj.jObject("teacher").asFullName
        val subjectName = itemObj.jString("subject_name")

        for (homeworkObj in itemObj.jArray("lesson_homeworks")) {
            val attachments = buildList {
                for (materialObj in homeworkObj.jArray("materials")) {
                    for (materialItemObj in materialObj.jArray("items")) {
                        if (materialItemObj.containsKey("link") && materialItemObj.jString("link") != "null") {
                            this += FeedEntryAttachment(
                                downloadUrl = materialItemObj.jString("link"),
                                title = materialItemObj.jString("title"),
                                thumbnailUrl = null
                            )
                        }
                    }
                }
            }

            val creationTime = TimeFormatters.longMeshDateTime.parseInstant(homeworkObj.jString("homework_created_at"))
            val assignedTime = TimeFormatters.isoLocalDateTime.parseInstant(homeworkObj.jString("date_prepared_for"))
            val objectId = homeworkObj.jInt("homework_id")
            val entryId = homeworkObj.jInt("homework_entry_id")
            val content = homeworkObj.jString("homework")
            if (!profile.providers.meshHomework!!.contentFilter.match(content)) continue

            insert(profile.feed, HomeworkFeedEntry(
                title = formatHomeworkTitle(subjectName, teacherName, assignedTime, profile),
                plain = homeworkObj.jString("homework"),
                html = null,
                subject = subjectName,
                teacher = teacherName,
                assignedTime = assignedTime,
                attachments = attachments,
                allowsSubmissions = false,
                sourceFingerprint = FeedEntry.fingerprintFrom(objectId, entryId),
                metadata = FeedEntryMetadata(creationTime = creationTime)
            ))
        }
    }

    private fun formatHomeworkTitle(subject: String, teacher: String, assignedTime: Instant, profile: Profile): String {
        val source = profile.providers.meshHomework!!.titleFormatter
        return source
            .replace("!{subject}", subject)
            .replace("!{teacher}", teacher)
            .replace("!{assigned_time}", TimeFormatters.dottedMeshDate.format(assignedTime))
    }

    object Definition : AuthorizedProviderDefinition<MeshAuthorizer, HomeworkFeedEntry> {
        override val name: String = "Домашние задания из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshHomework != null }
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.mesh != null }
        override val factory: (Profile) -> AuthorizedProvider<MeshAuthorizer, HomeworkFeedEntry> = { MeshHomeworkProvider(it.authorization.mesh!!) }
    }
}