package io.github.kanpov.litaggregator.desktop.screen.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class ProviderConfigScreen(profile: Profile, index: Int) : ConfigScreen(Locale["config.provider"], profile, index) {
    @Composable
    override fun OnboardingContent() {
        Row(
            modifier = Modifier.padding(top = 15.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MeshContainer()
                Spacer(modifier = Modifier.height(10.dp))
                GoogleContainer()
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                UlyssesContainer()
                Spacer(modifier = Modifier.height(10.dp))
                PortfolioContainer()
            }
        }
    }

    @Composable
    private fun MeshContainer() {
        Container(
            heading = Locale["config.provider.heading_mesh"],
            modifier = Modifier.fillMaxWidth(),
            requirementMet = profile.authorization.mos != null
        ) {
            ProviderElement(
                name = Locale["config.provider.mesh.homework"],
                onEnable = { profile.providers.meshHomework = MeshHomeworkProviderSettings() },
                onDisable = { profile.providers.meshHomework = null },
                detector = profile.providers.meshHomework
            ) {
                TextInputSetting(
                    name = Locale["config.provider.mesh.homework.title_formatter"],
                    onValueChange = { profile.providers.meshHomework!!.titleFormatter = it },
                    defaultValue = profile.providers.meshHomework!!.titleFormatter
                )
                BooleanInputSetting(
                    name = Locale["config.provider.mesh.homework.only_include_oo"],
                    onValueChange = { profile.providers.meshHomework!!.onlyIncludeOO = it },
                    defaultValue = profile.providers.meshHomework!!.onlyIncludeOO
                )
            }

            ProviderElement(
                name = Locale["config.provider.mesh.marks"],
                onEnable = { profile.providers.meshMarks = MeshMarkProviderSettings() },
                onDisable = { profile.providers.meshMarks = null },
                detector = profile.providers.meshMarks
            ) {
                // TODO add settings
            }

            ProviderElement(
                name = Locale["config.provider.mesh.ratings"],
                onEnable = { profile.providers.meshRatings = MeshRatingProviderSettings() },
                onDisable = { profile.providers.meshRatings = null },
                detector = profile.providers.meshRatings
            ) {
                // TODO add settings
            }

            ProviderElement(
                name = Locale["config.provider.mesh.visits"],
                onEnable = { profile.providers.meshVisits = MeshVisitProviderSettings() },
                onDisable = { profile.providers.meshVisits = null },
                detector = profile.providers.meshVisits
            ) {
                // TODO add settings
            }

            ProviderElement(
                name = Locale["config.provider.mesh.banners"],
                onEnable = { profile.providers.meshBanners = MeshBannerProviderSettings() },
                onDisable = { profile.providers.meshBanners = null },
                detector = profile.providers.meshBanners
            ) {
                // TODO add settings
            }
        }
    }

    @Composable
    private fun UlyssesContainer() {
        Container(
            heading = Locale["config.provider.heading_lit"],
            modifier = Modifier.fillMaxWidth(),
            requirementMet = profile.authorization.ulyss != null
        ) {
            ProviderElement(
                name = Locale["config.provider.lit.homework"],
                onEnable = { profile.providers.ulyss = UlyssProviderSettings() },
                onDisable = { profile.providers.ulyss = null },
                detector = profile.providers.ulyss
            ) {
                // TODO add settings
            }

            ProviderElement(
                name = Locale["config.provider.lit.announcements"],
                onEnable = { profile.providers.announcements = AnnouncementProviderSettings() },
                onDisable = { profile.providers.announcements = null },
                detector = profile.providers.announcements
            ) {
                // TODO add settings
            }
        }
    }

    @Composable
    private fun GoogleContainer() {
        Container(
            heading = Locale["config.provider.heading_google"],
            modifier = Modifier.fillMaxWidth(),
            requirementMet = profile.authorization.googleSession != null
        ) {
            ProviderElement(
                name = Locale["config.provider.google.classroom"],
                onEnable = { profile.providers.classroom = ClassroomProviderSettings() },
                onDisable = { profile.providers.classroom = null },
                detector = profile.providers.classroom
            ) {
                // TODO add settings
            }
        }
    }

    @Composable
    private fun PortfolioContainer() {
        Container(
            heading = Locale["config.provider.heading_portfolio"],
            modifier = Modifier.fillMaxWidth(),
            requirementMet = profile.authorization.mos != null
        ) {
            ProviderElement(
                name = Locale["config.provider.portfolio.diagnostics"],
                onEnable = { profile.providers.portfolioDiagnostics = PortfolioDiagnosticProviderSettings() },
                onDisable = { profile.providers.portfolioDiagnostics = null },
                detector = profile.providers.portfolioDiagnostics
            ) {
                // TODO add settings
            }

            ProviderElement(
                name = Locale["config.provider.portfolio.events"],
                onEnable = { profile.providers.portfolioEvents = PortfolioEventProviderSettings() },
                onDisable = { profile.providers.portfolioEvents = null },
                detector = profile.providers.portfolioEvents
            ) {
                // TODO add settings
            }
        }
    }

    @Composable
    private fun TextInputSetting(name: String, defaultValue: String, onValueChange: (String) -> Unit) {
        var value by remember { mutableStateOf(defaultValue) }
        BaseSetting(name) {
            TextField(
                value = value,
                onValueChange = {
                    value = it
                    onValueChange(it)
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.h6
            )
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun BooleanInputSetting(name: String, defaultValue: Boolean, onValueChange: (Boolean) -> Unit) {
        var checked by remember { mutableStateOf(defaultValue) }
        BaseSetting(name) {
            BasicIcon(
                painter = if (checked) painterResource("icons/tick.png") else painterResource("icons/untick.png"),
                size = 30.dp,
                modifier = Modifier.align(Alignment.CenterVertically).clickable {
                    checked = !checked
                    onValueChange(checked)
                }
            )
            Spacer(modifier = Modifier.width(30.dp))
        }
    }

    @Composable
    private fun BaseSetting(name: String, content: @Composable RowScope.() -> Unit) {
        Row {
            H6Text(name, modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.weight(1f))
            content()
        }
        Spacer(modifier = Modifier.height(10.dp))
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun ProviderElement(name: String, onEnable: () -> Unit, onDisable: () -> Unit, detector: Any?,
                                alertContent: @Composable ColumnScope.() -> Unit) {
        var enabled by remember { mutableStateOf(detector != null) }
        var showingSettings by remember { mutableStateOf(false) }

        Row {
            BasicIcon(
                painter = painterResource("icons/dot.png"),
                size = 25.dp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            H6Text(
                text = name,
                modifier = Modifier.align(Alignment.CenterVertically),
                highlight = enabled
            )

            Spacer(modifier = Modifier.weight(1f))

            BasicIcon(
                painter = if (enabled) painterResource("icons/tick.png") else painterResource("icons/untick.png"),
                size = 30.dp,
                modifier = Modifier.align(Alignment.CenterVertically).clickable {
                    enabled = !enabled
                    if (enabled) onEnable()
                    if (!enabled) onDisable()
                }
            )

            if (enabled) {
                BasicIcon(
                    painter = painterResource("icons/settings.png"),
                    size = 30.dp,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 10.dp)
                        .clickable { showingSettings = !showingSettings }
                )
            }

            if (!showingSettings) return@Row

            AlertDialog(
                title = {
                    H6Text(Locale["config.provider.settings"], highlight = true)
                },
                text = {
                    Column {
                        Spacer(modifier = Modifier.height(15.dp))
                        alertContent()
                    }
                },
                onDismissRequest = {
                    showingSettings = false
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showingSettings = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary
                        ),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        H6Text(Locale["button.ok"])
                    }
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    @Composable
    private fun Container(heading: String, modifier: Modifier, requirementMet: Boolean,
                          content: @Composable ColumnScope.() -> Unit) {
        Surface(
            shape = RoundedCornerShape(size = 15.dp),
            border = BorderStroke(2.dp, Color.Black),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                H6Text(heading, highlight = true, modifier = Modifier.align(Alignment.CenterHorizontally).scale(1.1f))
                Spacer(modifier = Modifier.height(10.dp))
                if (requirementMet) {
                    content()
                } else {
                    H6Text(
                        text = Locale["config.provider.source_not_bound"],
                        italicize = true,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}