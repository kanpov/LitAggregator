package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorization.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.VisitFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorization.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import io.github.kanpov.litaggregator.engine.util.io.jArray
import io.github.kanpov.litaggregator.engine.util.io.jBoolean
import io.github.kanpov.litaggregator.engine.util.io.jString

class MeshVisitProvider(authorizer: MeshAuthorizer) : MeshProvider<VisitFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        val relevantPastDays = getRelevantPastDays(profile)
        val (_, beginDay) = relevantPastDays.entries.last()
        val (_, endDay) = relevantPastDays.entries.first()
        val rootObj = authorizer.getJson("https://school.mos.ru/api/family/web/v1/visits?from=$beginDay&to=$endDay&contract_id=${studentInfo.contractId}")!!

        for (dayObj in rootObj.jArray("payload")) {
            val day = dayObj.jString("date")

            for (visitObj in dayObj.jArray("visits")) {
                val irregularPattern = visitObj.jBoolean("is_warning")

                if (visitObj.jString("in").contains("-")) continue // malformed entry because ???

                val entryTime = TimeFormatters.parseMeshDateTime(day, visitObj.jString("in"))
                val isOut = visitObj.jString("out")
                val exitTime = if (isOut == "-") null else TimeFormatters.parseMeshDateTime(day, visitObj.jString("out"))
                val fingerprintTime = exitTime ?: entryTime

                if (!profile.providers.meshVisits!!.includeIrregularPatterns && irregularPattern) continue

                insert(profile.feed, VisitFeedEntry(
                    entryTime = entryTime,
                    exitTime = exitTime,
                    irregularPattern = irregularPattern,
                    fullAddress = visitObj.jString("address"),
                    shortAddress = visitObj.jString("short_name"),
                    metadata = FeedEntryMetadata(creationTime = fingerprintTime),
                    sourceFingerprint = FeedEntry.fingerprintFrom(fingerprintTime)
                ))
            }
        }
    }

    object Definition : AuthorizedProviderDefinition<MeshAuthorizer, VisitFeedEntry> {
        override val name: String = "Посещаемость из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshVisits != null }
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.mesh != null }
        override val factory: (Profile) -> AuthorizedProvider<MeshAuthorizer, VisitFeedEntry> = { MeshVisitProvider(it.authorization.mesh!!) }
    }
}