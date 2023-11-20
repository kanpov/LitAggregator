package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.feed.*
import io.github.kanpov.litaggregator.engine.feed.entry.Rating
import io.github.kanpov.litaggregator.engine.feed.entry.RatingFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.RatingTrend
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorizer.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.io.*
import kotlinx.serialization.json.JsonObject

class MeshRatingProvider(authorizer: MeshAuthorizer) : MeshProvider<RatingFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        // Find profiles of all classmates
        val classProfiles = authorizer.getJsonArray<JsonObject>("https://dnevnik.mos.ru/core/api/profiles?class_unit_id=${studentInfo.classUnitId}") {
        }!!

        // Map their person IDs (contingent GUIDs) to their full names in order to identify them in the rating list
        val personIdToName = buildMap {
            classProfiles.forEach { obj ->
                if (obj.jString("type") == "student" && obj.containsKey("person_id")) {
                    val id = obj.jString("person_id")
                    val user = obj.jObject("user")
                    val name = user.asFullName
                    if (id != studentInfo.personId) this[id] = name
                }
            }
        }

        // Add rating entry for each relevant day
        for ((time, day) in getRelevantPastDays(profile)) {
            var personRating: Rating? = null
            val classmateRatings: MutableMap<String, Rating> = mutableMapOf()
            val subjectRatings: MutableMap<String, Rating> = mutableMapOf()
            val args = "?personId=${studentInfo.personId}&date=$day"

            // Add self and classmates' ratings
            authorizer.getJsonArray<JsonObject>("https://school.mos.ru/api/ej/rating/v1/rank/class$args")!!.forEach { obj ->
                if (obj.jString("personId") == studentInfo.personId) {
                    personRating = parseRating(obj.jObject("rank"))
                } else if (profile.providers.meshRatings!!.includeClassmateRatings) {
                    // some classmates may not have fully been registered yet
                    if (personIdToName.containsKey(obj.jString("personId"))) {
                        val classmateName = personIdToName[obj.jString("personId")]!!
                        classmateRatings[classmateName] = parseRating(obj.jObject("rank"))
                    }
                }
            }

            // Add own per subject ratings
            authorizer.getJsonArray<JsonObject>("https://school.mos.ru/api/ej/rating/v1/rank/subjects$args")!!.forEach { obj ->
                subjectRatings[obj.jString("subjectName")] = parseRating(obj.jObject("rank"))
            }

            if (personRating != null) {
                println()
                if (insert(profile.feed, RatingFeedEntry(
                    sourceFingerprint = FeedEntry.fingerprintFrom(day),
                    metadata = FeedEntryMetadata(creationTime = time),
                    overallRating = personRating!!,
                    perSubjectRatings = subjectRatings,
                    classmateRatings = classmateRatings.ifEmpty { null }
                ))) return
            } else {
                return // invalid ratings begin (sometimes happens at the beginning of the school year0
            }
        }
    }

    private fun parseRating(obj: JsonObject) = Rating(
        averageMark = obj.jFloat("averageMarkFive"),
        rankPlace = obj.jInt("rankPlace"),
        trend = RatingTrend.parse(obj.jString("trend"))
    )

    object Definition : AuthorizedProviderDefinition<MeshAuthorizer, RatingFeedEntry> {
        override val name: String = "Рейтинги из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshRatings != null }
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.mos != null }
        override val factory: (Profile) -> AuthorizedProvider<MeshAuthorizer, RatingFeedEntry> = { MeshRatingProvider(it.authorization.mos!!) }
    }
}