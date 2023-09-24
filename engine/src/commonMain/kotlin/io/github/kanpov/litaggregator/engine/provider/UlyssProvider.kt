package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.UlyssAuthorizer
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.response.ulyss.UlyssSubject
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class UlyssProvider(authorizer: UlyssAuthorizer) : AuthorizedProvider<UlyssAuthorizer>(authorizer) {
    override suspend fun provide(profile: Profile, entries: MutableList<out FeedEntry>) {
        val studyYear = determineStudyYear()
        val subjects = authorizer.getJsonArray<UlyssSubject>("https://in.lit.msu.ru/api/v1/Ulysses/$studyYear/")

        for (subject in subjects!!) {
            println(subject)
        }
    }

    private fun determineStudyYear(): String {
        // current time on GMT+3 (Moscow)
        val time = LocalDateTime.now(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(3)))
        val offset = if (time.monthValue < 9) 0 else 1
        return "${time.year - 1 + offset}-${time.year + offset}"
    }

    object Definition : AuthorizedProviderDefinition<HomeworkFeedEntry, UlyssAuthorizer> {
        override val name: String = "УЛИСС (ДЗ)"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.ulyss != null }
        override val isAuthorized: (Authorization) -> Boolean = { it.ulyss != null && it.mos != null }
        override val entries: (Feed) -> MutableList<HomeworkFeedEntry> = Feed::homework
        override val factory: (Authorization) -> AuthorizedProvider<UlyssAuthorizer> = { UlyssProvider(it.ulyss!!) }
    }
}
