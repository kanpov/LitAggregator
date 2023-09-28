package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.VisitFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import io.github.kanpov.litaggregator.engine.util.jArray
import io.github.kanpov.litaggregator.engine.util.jBoolean
import io.github.kanpov.litaggregator.engine.util.jString
import kotlinx.serialization.json.JsonObject

class MeshVisitProvider(authorizer: MosAuthorizer) : MeshProvider<VisitFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        val relevantPastDays = getRelevantPastDays(profile)
        val (_, beginDay) = relevantPastDays.entries.last()
        val (_, endDay) = relevantPastDays.entries.first()
        val rootObj = authorizer.getJson("https://school.mos.ru/api/family/web/v1/visits?from=$beginDay&to=$endDay&contract_id=${studentInfo.contractId}")!!

        for (dayObj in rootObj.jArray<JsonObject>("payload")) {
            val day = dayObj.jString("date")

            for (visitObj in dayObj.jArray<JsonObject>("visits")) {
                val irregularPattern = visitObj.jBoolean("is_warning")
                val entryTime = TimeFormatters.parseMeshDateTime(day, visitObj.jString("in"))
                val exitTime = TimeFormatters.parseMeshDateTime(day, visitObj.jString("out"))

                if (!profile.providers.meshVisits!!.includeIrregularPatterns && irregularPattern) continue

                insert(profile.feed, VisitFeedEntry(
                    entryTime = entryTime,
                    exitTime = exitTime,
                    irregularPattern = irregularPattern,
                    fullAddress = visitObj.jString("address"),
                    shortAddress = visitObj.jString("short_name"),
                    metadata = FeedEntryMetadata(creationTime = exitTime),
                    sourceFingerprint = FeedEntry.fingerprintFrom(exitTime)
                ))
            }
        }
    }

    object Definition : AuthorizedProviderDefinition<MosAuthorizer, VisitFeedEntry> {
        override val name: String = "Посещаемость из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshVisits != null }
        override val isAuthorized: (Authorization) -> Boolean = { it.mos != null }
        override val factory: (Authorization) -> AuthorizedProvider<MosAuthorizer, VisitFeedEntry> = { MeshVisitProvider(it.mos!!) }
    }
}