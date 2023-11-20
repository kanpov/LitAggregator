package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.AnnouncementFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import io.github.kanpov.litaggregator.engine.util.io.ktorClient
import io.github.kanpov.litaggregator.engine.util.padLeft
import io.github.kanpov.litaggregator.engine.util.parseInstant
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup
import java.security.MessageDigest

class AnnouncementProvider : SimpleProvider<AnnouncementFeedEntry>() {
    override suspend fun provide(profile: Profile) {
        val doc = Jsoup.parse(ktorClient.get("https://lit.msu.ru/news").bodyAsText())
        val contentDiv = doc.getElementsByClass("view-content").first() ?: return
        val year = doc.getElementById("main")?.getElementsByTag("h1")?.first()?.text() ?: return
        val (earliestTime, _) = getRelevantPastDays(profile).entries.last()

        for (groupingDiv in contentDiv.getElementsByClass("view-grouping")) {
            val monthLiteral = groupingDiv.getElementsByClass("view-grouping-header").first()?.text() ?: continue
            val groupingContentDiv = groupingDiv.getElementsByClass("view-grouping-content").first() ?: continue
            val days = groupingContentDiv.getElementsByTag("h3")
            val postDivs = groupingContentDiv.getElementsByClass("views-row")

            if (days.size != postDivs.size) continue

            for (i in days.indices) {
                val title = postDivs[i].getElementsByTag("h4").first()?.text() ?: continue
                val content = postDivs[i].getElementsByTag("p").first()?.html() ?: continue

                val day = days[i].text().padLeft(until = 2, with = '0')
                val monthInt = listOf("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август",
                    "сентябрь", "октябрь", "ноябрь", "декабрь").indexOf(monthLiteral.lowercase()) + 1
                val monthValue = monthInt.toString().padLeft(until = 2, with = '0')
                val creationTime = TimeFormatters.dottedMeshDate.parseInstant("$day.$monthValue.$year")

                if (creationTime.isBefore(earliestTime)) continue

                val categories = buildList {
                    postDivs[i].getElementsByClass("category-list").let { matches ->
                        if (matches.isEmpty()) return@let

                        for (attachment in matches.first()!!.getElementsByTag("a")) {
                            this += attachment.text()
                        }
                    }
                }

                if (!profile.providers.announcements!!.categoryFilter.matchList(categories)) continue
                if (!profile.providers.announcements!!.htmlFilter.match(content)) continue

                insert(profile.feed, AnnouncementFeedEntry(
                    title = title,
                    content = content,
                    categories = categories,
                    sourceFingerprint = MessageDigest.getInstance("SHA-256")
                        .digest(content.toByteArray())
                        .toString(),
                    metadata = FeedEntryMetadata(creationTime = creationTime)
                ))
            }
        }
    }

    object Definition : SimpleProviderDefinition<AnnouncementFeedEntry> {
        override val name: String = "Новости из сайта Лицея 1533"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.announcements != null }
        override val factory: (Profile) -> SimpleProvider<AnnouncementFeedEntry> = { AnnouncementProvider() }
    }
}