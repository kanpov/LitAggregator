package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.*
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.jFloat
import io.github.kanpov.litaggregator.engine.util.jInt
import io.github.kanpov.litaggregator.engine.util.jObj
import io.github.kanpov.litaggregator.engine.util.jString
import kotlinx.serialization.json.JsonObject

class DnevnikRatingProvider(authorizer: MosAuthorizer) : AbstractDnevnikProvider<RatingFeedEntry>(authorizer) {
    override suspend fun dnevnikProvide(
        profile: Profile,
        studentInfo: DnevnikStudentInfo
    ): Set<RatingFeedEntry> {
        val newEntries = mutableSetOf<RatingFeedEntry>()

        // Find profiles of all classmates
        val classProfiles = authorizer.getJsonArray<JsonObject>("https://dnevnik.mos.ru/core/api/profiles?class_unit_id=${studentInfo.classUnitId}") {
        }!!

        // Map their person IDs (contingent GUIDs) to their full names in order to identify them in the rating list
        val personIdToName = buildMap {
            classProfiles.forEach { obj ->
                if (obj.jString("type") == "student" && obj["person_id"] != null) {
                    val id = obj.jString("person_id")
                    val user = obj.jObj("user")
                    val name =
                        "${user.jString("last_name")} ${user.jString("first_name")} ${user.jString("middle_name")}"

                    if (id != studentInfo.personId) this[id] = name
                }
            }
        }

        // Add rating entry for each relevant day
        getRelevantDays(profile).forEach { (time, day) ->
            var personRating: Rating? = null
            val classmateRatings: MutableMap<String, Rating> = mutableMapOf()
            val subjectRatings: MutableMap<String, Rating> = mutableMapOf()
            val args = "?personId=${studentInfo.personId}&date=$day"

            // Add self and classmates' ratings
            authorizer.getJsonArray<JsonObject>("https://school.mos.ru/api/ej/rating/v1/rank/class$args")!!.forEach { obj ->
                if (obj.jString("personId") == studentInfo.personId) {
                    personRating = parseRating(obj.jObj("rank"))
                } else if (profile.providers.dnevnikRatings!!.includeClassmateRatings) {
                    // some classmates may not have fully been registered yet
                    if (personIdToName.containsKey(obj.jString("personId"))) {
                        val classmateName = personIdToName[obj.jString("personId")]!!
                        classmateRatings[classmateName] = parseRating(obj.jObj("rank"))
                    }
                }
            }

            // Add own per subject ratings
            authorizer.getJsonArray<JsonObject>("https://school.mos.ru/api/ej/rating/v1/rank/subjects$args")!!.forEach { obj ->
                subjectRatings[obj.jString("subjectName")] = parseRating(obj.jObj("rank"))
            }

            if (personRating != null) {
                newEntries += RatingFeedEntry(
                    metadata = FeedEntryMetadata(creationTime = time),
                    overallRating = personRating!!,
                    perSubjectRatings = subjectRatings,
                    classmateRatings = classmateRatings.ifEmpty { null }
                )
            }
        }

        return newEntries
    }

    private fun parseRating(obj: JsonObject) = Rating(
        averageMark = obj.jFloat("averageMarkFive"),
        rankPlace = obj.jInt("rankPlace"),
        trend = RatingTrend.parse(obj.jString("trend"))
    )

    object Definition : AuthorizedProviderDefinition<MosAuthorizer, RatingFeedEntry> {
        override val name: String = "Рейтинги МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.dnevnikRatings != null }
        override val isAuthorized: (Authorization) -> Boolean = { it.mos != null }
        override val factory: (Authorization) -> AuthorizedProvider<MosAuthorizer, RatingFeedEntry> = { DnevnikRatingProvider(it.mos!!) }
    }
}