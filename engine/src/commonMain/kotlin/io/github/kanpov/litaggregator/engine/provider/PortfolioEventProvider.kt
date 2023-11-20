package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.EventFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorizer.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import io.github.kanpov.litaggregator.engine.util.io.*
import io.github.kanpov.litaggregator.engine.util.parseInstant
import java.time.Instant

class PortfolioEventProvider(authorizer: MeshAuthorizer) : MeshProvider<EventFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        val rewards = authorizer.getJsonArrayFromPayload(
            "https://school.mos.ru/portfolio/app/persons/${studentInfo.personId}/rewards/list?size=100",
            payloadName = "data"
        )!!

        val events = authorizer.getJsonArrayFromPayload(
            "https://school.mos.ru/portfolio/app/persons/${studentInfo.personId}/events/list?size=100",
            payloadName = "data"
        )!!

        for (rewardObj in rewards) {
            val rewardId = rewardObj.jLong("id")
            val entityId = rewardObj.jString("entityId").toLong()
            val eventObj = events.firstOrNull { it.jLong("id") == entityId } ?: continue

            val creationTime = TimeFormatters.isoLocalDateTime.parse(rewardObj.jString("creationDate"), Instant::from)
            val reward = rewardObj.jObject("rewardType").jString("value")
            val startTime = if (eventObj.containsKey("startDate")) TimeFormatters.slashedMeshDate.parseInstant(eventObj.jString("startDate")) else null
            val endTime = if (eventObj.containsKey("endDate")) TimeFormatters.slashedMeshDate.parseInstant(eventObj.jString("endDate")) else null
            val subject = if (eventObj.containsKey("subject")) eventObj.jArray("subject").first().jString("value") else null

            if (subject == null && profile.providers.portfolioEvents!!.onlyVos) continue

            insert(profile.feed, EventFeedEntry(
                name = eventObj.jString("name"),
                organizer = eventObj.jString("organizators"),
                reward = reward,
                startTime = startTime,
                endTime = endTime,
                subject = subject,
                participantCategory = eventObj.jOptionalString("participantCategory"),
                profession = eventObj.jOptionalString("profession"),
                sourceFingerprint = FeedEntry.fingerprintFrom(entityId, rewardId),
                metadata = FeedEntryMetadata(creationTime = creationTime)
            ))
        }
    }

    object Definition : AuthorizedProviderDefinition<MeshAuthorizer, EventFeedEntry> {
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.mos != null }
        override val name: String = "Соревнования из Портфолио МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.portfolioEvents != null }
        override val factory: (Profile) -> SimpleProvider<EventFeedEntry> = { PortfolioEventProvider(it.authorization.mos!!) }
    }
}