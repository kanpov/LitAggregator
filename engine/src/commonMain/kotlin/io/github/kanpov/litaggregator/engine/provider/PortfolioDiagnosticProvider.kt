package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorization.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.DiagnosticFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.DiagnosticResultComparison
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorization.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.io.*
import kotlinx.serialization.json.JsonObject

class PortfolioDiagnosticProvider(authorizer: MeshAuthorizer) : MeshProvider<DiagnosticFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        val diagnosticYears = authorizer.getJsonArrayFromPayload(
            "https://school.mos.ru/portfolio/app/persons/${studentInfo.personId}/diagnostic/independent-rating",
            payloadName = "data"
        )!!

        for (yearObj in diagnosticYears) {
            for (ratingObj in yearObj.jArray("independentDiagnosticRating")) {
                if (insert(profile.feed, DiagnosticFeedEntry(
                    year = yearObj.jString("learningYear"),
                    subject = ratingObj.jString("subject"),
                    place = ratingObj.jObject("ratingClass").jInt("studentPlace"),
                    maxResult = ratingObj.jInt("maxResult"),
                    yourResult = ratingObj.jObject("ratingClass").jInt("studentResultMarkValue"),
                    yourLevel = ratingObj.jObject("ratingClass").jString("studentLevel"),
                    comparisonToRegion = parseResultComparison(profile, ratingObj.jObject("ratingRegion")),
                    comparisonToSchool = parseResultComparison(profile, ratingObj.jObject("ratingSchool")),
                    comparisonToGroup = parseResultComparison(profile, ratingObj.jObject("ratingClass")),
                    sourceFingerprint = FeedEntry.fingerprintFrom(ratingObj.jInt("workId")),
                    metadata = FeedEntryMetadata(creationTime = null)
                ))) return
            }
        }
    }

    private fun parseResultComparison(profile: Profile, obj: JsonObject): DiagnosticResultComparison? {
        return if (profile.providers.portfolioDiagnostics!!.includeComparisons) {
            DiagnosticResultComparison(
                sameResultAmount = obj.jInt("studentPlaceStudentsCount"),
                bestResultAmount = obj.jInt("firstPlaceStudentCount"),
                percentile = obj.jFloat("diagnosticPercentLowerOthers")
            )
        } else {
            null
        }
    }

    object Definition : AuthorizedProviderDefinition<MeshAuthorizer, DiagnosticFeedEntry> {
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.mesh != null }
        override val name: String = "Диагностики из Портфолио МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.portfolioDiagnostics != null }
        override val factory: (Profile) -> SimpleProvider<DiagnosticFeedEntry> = { PortfolioDiagnosticProvider(it.authorization.mesh!!) }
    }
}