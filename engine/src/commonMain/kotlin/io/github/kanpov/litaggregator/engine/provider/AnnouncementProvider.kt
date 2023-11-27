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
import org.jsoup.nodes.Element
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

            val dayToPostDivMapping = buildMap<Element, Set<Element>> {
                val currentPostDivs = mutableSetOf<Element>()
                var currentDay: Element? = null

                for (child in groupingContentDiv.children()) {
                    if (child.tagName() == "h3") {
                        if (currentDay != null) {
                            this[currentDay] = currentPostDivs.toSet()
                            currentPostDivs.clear()
                        }
                        currentDay = child
                    } else if (child.classNames().contains("views-row")) {
                        currentPostDivs += child
                    }
                }
                this[currentDay!!] = currentPostDivs.toSet()
            }

            for ((day, postDivs) in dayToPostDivMapping) {
                for (postDiv in postDivs) {
                    val title = postDiv.getElementsByTag("h4").first()?.text() ?: continue
                    val content = postDiv.getElementsByTag("p").first()?.text() ?: continue

                    val dayValue = day.text().padLeft(until = 2, with = '0')
                    val monthInt = listOf(
                        "январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август",
                        "сентябрь", "октябрь", "ноябрь", "декабрь"
                    ).indexOf(monthLiteral.lowercase()) + 1
                    val monthValue = monthInt.toString().padLeft(until = 2, with = '0')
                    val creationTime = TimeFormatters.dottedMeshDate.parseInstant("$dayValue.$monthValue.$year")

                    if (creationTime.isBefore(earliestTime)) continue

                    val categories = buildList {
                        postDiv.getElementsByClass("category-list").let { matches ->
                            if (matches.isEmpty()) return@let

                            for (attachment in matches.first()!!.getElementsByTag("a")) {
                                this += attachment.text()
                            }
                        }
                    }

                    if (!profile.providers.announcements!!.categoryFilter.matchList(categories)) continue
                    if (!profile.providers.announcements!!.htmlFilter.match(content)) continue

                    insert(
                        profile.feed, AnnouncementFeedEntry(
                            title = title,
                            content = content,
                            categories = categories,
                            sourceFingerprint = MessageDigest.getInstance("SHA-256")
                                .digest(content.toByteArray())
                                .toString(),
                            metadata = FeedEntryMetadata(creationTime = creationTime, sourceName = "Сайт Лицея")
                        )
                    )
                }
            }
        }
    }

    object Definition : SimpleProviderDefinition<AnnouncementFeedEntry> {
        override val name: String = "Новости Лицея"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.announcements != null }
        override val factory: (Profile) -> SimpleProvider<AnnouncementFeedEntry> = { AnnouncementProvider() }
    }
}