package io.github.kanpov.litaggregator.desktop.screen.browser

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.aay.compose.barChart.BarChart
import com.aay.compose.barChart.model.BarParameters
import com.aay.compose.baseComponents.model.LegendPosition
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.engine.feed.entry.DiagnosticFeedEntry
import io.github.kanpov.litaggregator.engine.feed.entry.DiagnosticResultComparison
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class DiagnosticFeedEntryRenderer(entry: DiagnosticFeedEntry) : FeedEntryRenderer<DiagnosticFeedEntry>(entry) {
    @Composable
    private fun Stat(text: Any, color: Color, startPadding: Dp = 0.dp) {
        Text(
            text.toString(),
            fontSize = 2.em,
            color = color,
            modifier = Modifier.padding(start = startPadding)
        )
    }

    @Composable
    private fun OverallResult(modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Row {
                Stat(entry.yourResult, Color.Blue)
                Stat("/", Color.Black, startPadding = 3.dp)
                Stat(entry.maxResult, Color.Red, startPadding = 3.dp)
            }
            Text(
                DesktopLocale["browser.diagnostics.points"],
                fontSize = 1.02.em,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun PreviewPlacement(label: String, value: Float) {
        Row {
            BasicIcon(
                painter = painterResource("icons/dot.png"),
                size = 15.dp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text("$label:", fontWeight = FontWeight.SemiBold)
            Text(DesktopLocale["browser.diagnostics.place_formatting", value], modifier = Modifier.padding(start = 3.dp))
        }
        Spacer(modifier = Modifier.height(5.dp))
    }

    @Composable
    private fun RowScope.DetailedChart(comparison: DiagnosticResultComparison, xAxisLabel: String) {
        val barParameters = listOf(
            BarParameters(
                dataName = "",
                data = listOf(comparison.sameResultAmount.toDouble()),
                barColor = Color.Cyan
            ),
            BarParameters(
                dataName = "",
                data = listOf(comparison.firstPlaceAmount.toDouble()),
                barColor = Color.Gold
            ),
            BarParameters(
                dataName = "",
                data = listOf(comparison.secondPlaceAmount.toDouble()),
                barColor = Color.Silver
            ),
            BarParameters(
                dataName = "",
                data = listOf(comparison.thirdPlaceAmount.toDouble()),
                barColor = Color.Bronze
            )
        )

        Box(modifier = Modifier.size(width = 215.dp, height = 400.dp)) {
            BarChart(
                chartParameters = barParameters,
                gridColor = Color.DarkGray,
                xAxisData = listOf(xAxisLabel).map { DesktopLocale["browser.diagnostics.$it"] },
                barWidth = 20.dp,
                legendPosition = LegendPosition.DISAPPEAR
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun RowScope.DetailedLegendElement(tint: Color, text: String) {
        BasicIcon(
            painter = painterResource("icons/dot.png"),
            size = 20.dp,
            modifier = Modifier.align(Alignment.CenterVertically),
            tint = tint
        )
        Text(text, modifier = Modifier.padding(start = 3.dp))
        Spacer(modifier = Modifier.width(10.dp))
    }

    @Composable
    override fun ColumnScope.PreviewContent() {
        PreviewHeading(DesktopLocale["browser.diagnostics.title_formatting", entry.subject, entry.years])

        Row(modifier = Modifier.padding(top = 5.dp)) {
            OverallResult(Modifier.align(Alignment.CenterVertically))

            if (entry.comparisonToGroup == null) return@Row

            Spacer(modifier = Modifier.weight(1f))
            Column(modifier = Modifier.padding(start = 5.dp).align(Alignment.CenterVertically)) {
                PreviewPlacement(DesktopLocale["browser.diagnostics.among_group"], entry.comparisonToGroup!!.percentile)
                PreviewPlacement(DesktopLocale["browser.diagnostics.among_school"], entry.comparisonToSchool!!.percentile)
                PreviewPlacement(DesktopLocale["browser.diagnostics.among_region"], entry.comparisonToRegion!!.percentile)
            }
        }
    }

    @Composable
    override fun ColumnScope.DetailedContent() {
        Row {
            OverallResult()

            Spacer(modifier = Modifier.width(30.dp))

            Column {
                TextProperty(DesktopLocale["browser.diagnostics.subject"], entry.subject)
                TextProperty(DesktopLocale["browser.diagnostics.years"], entry.years)
                TextProperty(DesktopLocale["browser.diagnostics.level"], entry.yourLevel)
            }
        }

        if (entry.comparisonToGroup != null) {
            Row(modifier = Modifier.padding(top = 10.dp)) {
                DetailedLegendElement(Color.Cyan, DesktopLocale["browser.diagnostics.same_result"])
                DetailedLegendElement(Color.Gold, DesktopLocale["browser.diagnostics.first_place_result"])
                DetailedLegendElement(Color.Silver, DesktopLocale["browser.diagnostics.second_place_result"])
                DetailedLegendElement(Color.Bronze, DesktopLocale["browser.diagnostics.third_place_result"])
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                DetailedChart(entry.comparisonToGroup!!, "among_group")
                DetailedChart(entry.comparisonToSchool!!, "among_school")
                DetailedChart(entry.comparisonToRegion!!, "among_region")
            }
        }
    }
}