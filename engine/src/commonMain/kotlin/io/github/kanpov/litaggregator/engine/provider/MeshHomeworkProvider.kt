package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.*
import kotlinx.serialization.json.JsonObject

class MeshHomeworkProvider(authorizer: MosAuthorizer) : MeshProvider<HomeworkFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        // Fetch teacher profiles to determine teacher names for each subject's id
        val teacherProfiles = authorizer.getJsonArray<JsonObject>("https://dnevnik.mos.ru/core/api/teacher_profiles")!!
        val subjectIdToTeacherName = buildMap {
            for (teacherProfileObj in teacherProfiles) {
                val teacherName = teacherProfileObj.jString("name")

                for (subjectObj in teacherProfileObj.jArray<JsonObject>("subjects")) {
                    val subjectId = subjectObj.jInt("id")

                    this[subjectId] = teacherName
                }
            }
        }

        println(subjectIdToTeacherName)

        val (_, beginDate) = getRelevantPastDays(profile).entries.last()
        val (_, endDate) = getRelevantPastDays(profile).entries.first()
        val rootObj = authorizer.getJson(
            "https://school.mos.ru/api/family/web/v1/homeworks?from=$beginDate&to=$endDate&student_id=${studentInfo.profileId}")!!

        for (homeworkObj in rootObj.jArray<JsonObject>("payload")) {
            val subjectId = homeworkObj.jInt("subject_id")
            val homeworkId = homeworkObj.jString("homework_entry_student_id")
            val creationTime = TimeFormatters.slashedMeshDate.parseInstant(homeworkObj.jString("date_assigned_on"))
            val assignedTime = TimeFormatters.slashedMeshDate.parseInstant(homeworkObj.jString("date"))

            insert(profile.feed, HomeworkFeedEntry(
                plain = homeworkObj.jString("description"),
                html = null,
                subject = homeworkObj.jString("subject_name"),
                teacher = subjectIdToTeacherName[subjectId]!!,
                assignedTime = assignedTime,
                attachments = emptyList(),
                allowsSubmissions = false,
                metadata = FeedEntryMetadata(creationTime = creationTime),
                sourceFingerprint = FeedEntry.fingerprintFrom(subjectId, homeworkId)
            ))
        }
    }

    object Definition : AuthorizedProviderDefinition<MosAuthorizer, HomeworkFeedEntry> {
        override val name: String = "Домашние задания из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshHomework != null }
        override val isAuthorized: (Authorization) -> Boolean = { it.mos != null }
        override val factory: (Authorization) -> AuthorizedProvider<MosAuthorizer, HomeworkFeedEntry> = { MeshHomeworkProvider(it.mos!!) }
    }
}