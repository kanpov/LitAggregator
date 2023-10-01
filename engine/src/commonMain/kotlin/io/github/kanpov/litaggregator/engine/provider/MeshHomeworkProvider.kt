package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.*

class MeshHomeworkProvider(authorizer: MosAuthorizer) : MeshProvider<HomeworkFeedEntry>(authorizer) {
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
            val attachments = mutableListOf<String>()

            for (materialObj in homeworkObj.jArray("materials")) {
                for (materialItemObj in materialObj.jArray("items")) {
                    if (materialItemObj["link"] != null && materialItemObj.jString("link") != "null") {
                        attachments += materialItemObj.jString("link")
                    }
                }
            }

            val creationTime = TimeFormatters.longMeshDateTime.parseInstant(homeworkObj.jString("homework_created_at"))
            val assignedTime = TimeFormatters.iso.parseInstant(homeworkObj.jString("date_prepared_for"))
            val objectId = homeworkObj.jInt("homework_id")
            val entryId = homeworkObj.jInt("homework_entry_id")

            insert(profile.feed, HomeworkFeedEntry(
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

    object Definition : AuthorizedProviderDefinition<MosAuthorizer, HomeworkFeedEntry> {
        override val name: String = "Домашние задания из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshHomework != null }
        override val isAuthorized: (Authorization) -> Boolean = { it.mos != null }
        override val factory: (Profile) -> AuthorizedProvider<MosAuthorizer, HomeworkFeedEntry> = { MeshHomeworkProvider(it.authorization.mos!!) }
        override val networkUsage: ProviderNetworkUsage = ProviderNetworkUsage.High
    }
}