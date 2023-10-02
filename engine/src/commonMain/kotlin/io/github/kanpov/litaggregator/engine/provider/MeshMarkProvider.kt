package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.MarkFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.*
import io.github.kanpov.litaggregator.engine.util.io.*
import kotlinx.serialization.json.JsonObject

class MeshMarkProvider(authorizer: MosAuthorizer) : MeshProvider<MarkFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        val academicYearObj = authorizer.getJsonArray<JsonObject>("https://school.mos.ru/api/ej/core/family/v1/academic_years")!!
        val academicYearId = academicYearObj.first { it.jBoolean("current_year") }.jInt("id")

        val progressObj = authorizer.getJsonArray<JsonObject>(
            "https://school.mos.ru/api/ej/report/family/v1/progress/json?academic_year_id=$academicYearId&student_profile_id=${studentInfo.profileId}")!!
        val (relevancyLimit, _) = getRelevantPastDays(profile).entries.last()

        for (subjectObj in progressObj) {
            val subjectName = subjectObj.jString("subject_name")
            val subjectId = subjectObj.jInt("subject_id")

            for (periodObj in subjectObj.jArray("periods")) {
                val periodName = periodObj.jString("name")

                for (markObj in periodObj.jArray("marks")) {
                    val valueObj = markObj.jArray("values").first()
                    val creationTime = TimeFormatters.dottedMeshDate.parseInstant(markObj.jString("date"))
                    val isExam = markObj.jBoolean("is_exam")
                    val weight = markObj.jInt("weight")

                    if (creationTime.isBefore(relevancyLimit)) continue
                    if (!isExam && profile.providers.meshMarks!!.onlyIncludeExams) continue
                    if (!profile.providers.meshMarks!!.weightFilter.match(weight)) continue

                    insert(profile.feed, MarkFeedEntry(
                        subject = subjectName,
                        value = valueObj.jFloat("five").toInt(),
                        weight = weight,
                        isExam = isExam,
                        comment = markObj.jString("comment"),
                        topic = markObj.jString("topic_name"),
                        workForm = markObj.jString("control_form_name"),
                        period = periodName,
                        metadata = FeedEntryMetadata(creationTime = creationTime),
                        sourceFingerprint = FeedEntry.fingerprintFrom(subjectId, markObj.jInt("id"))
                    ))
                }
            }
        }
    }

    object Definition : AuthorizedProviderDefinition<MosAuthorizer, MarkFeedEntry> {
        override val name: String = "Оценки из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshMarks != null }
        override val isAuthorized: (Authorization) -> Boolean = { it.mos != null }
        override val factory: (Profile) -> AuthorizedProvider<MosAuthorizer, MarkFeedEntry> = { MeshMarkProvider(it.authorization.mos!!) }
        override val networkUsage: ProviderNetworkUsage = ProviderNetworkUsage.Low
    }
}