package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.engine.feed.entry.Rating
import io.github.kanpov.litaggregator.engine.feed.entry.RatingFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.RatingTrend
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class RatingFeedEntryRenderer(entry: RatingFeedEntry) : FeedEntryRenderer<RatingFeedEntry>(entry) {
    @Composable
    private fun RatingBox(modifier: Modifier = Modifier) {
        ColoredFrame(Color.Black, modifier, width = 75.dp, height = 75.dp) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    entry.overallRating.rankPlace.toString(),
                    fontSize = 2.5.em,
                    color = getRewardColor(entry.overallRating.rankPlace),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(
                    entry.overallRating.averageMark.toString(),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 3.dp, end = 3.dp)
                )
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun RatingList(source: Map<String, Rating>, showDetails: Boolean = true) {
        for ((heading, rating) in source) {
            Row {
                BasicIcon(
                    painter = painterResource("icons/dot.png"),
                    size = 15.dp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                val formattedHeading = if (showDetails) {
                    heading
                } else {
                    heading.split(' ').take(2).joinToString(separator = " ") // first 2 words
                }
                Text(
                    "$formattedHeading:",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 5.dp)
                )
                Text(
                    rating.averageMark.toString(),
                    modifier = Modifier.padding(start = 3.dp)
                )

                if (!showDetails) return@Row

                val placeTranslation = DesktopLocale["browser.ratings.place_formatting", rating.rankPlace]
                val trendTranslation = when (rating.trend) {
                    RatingTrend.Stable -> DesktopLocale["browser.ratings.stagnation"]
                    RatingTrend.Increasing -> DesktopLocale["browser.ratings.increase"]
                    RatingTrend.Decreasing -> DesktopLocale["browser.ratings.decrease"]
                }
                Text(
                    "($placeTranslation, $trendTranslation)",
                    modifier = Modifier.padding(start = 3.dp)
                )
            }
        }
    }

    @Composable
    override fun ColumnScope.PreviewContent() {
        PreviewHeading(
            DesktopLocale["browser.ratings.title_formatting", TimeFormatters.dottedMeshDate.format(entry.metadata.creationTime)]
        )

        Row(modifier = Modifier.padding(top = 5.dp)) {
            RatingBox()
            if (entry.classmateRatings == null) return@Row
            Column(modifier = Modifier.padding(start = 10.dp)) {
                RatingList(takeFromMap(entry.classmateRatings!!, 5), showDetails = false)
            }
        }
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        RatingBox()
        DetailedSubHeading(DesktopLocale["browser.ratings.quarterly_marks"])
        RatingList(entry.perSubjectRatings)
        if (entry.classmateRatings != null) {
            DetailedSubHeading(DesktopLocale["browser.ratings.classmate_ratings"])
            RatingList(entry.classmateRatings!!)
        }
    }

    private fun <K, V> takeFromMap(originalMap: Map<K, V>, n: Int): Map<K, V> {
        return buildMap {
            for (key in originalMap.keys.take(n)) {
                this[key] = originalMap[key] ?: continue
            }
        }
    }
}
