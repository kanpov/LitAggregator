package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.desktop.SMALL_WINDOW_SIZE
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H5Text
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.desktop.components.HoverableIconButton
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.desktop.resizeAppWindow
import io.github.kanpov.litaggregator.desktop.screen.config.ConfigIntent
import io.github.kanpov.litaggregator.desktop.screen.config.ConfigScreen
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.profile.CachedProfile
import io.github.kanpov.litaggregator.engine.profile.PROFILE_EXTENSION
import io.github.kanpov.litaggregator.engine.profile.ProfileCache
import io.github.kanpov.litaggregator.engine.util.APP_REPOSITORY
import io.github.kanpov.litaggregator.engine.util.APP_VERSION
import io.github.kanpov.litaggregator.engine.util.AppUpdate
import io.github.kanpov.litaggregator.engine.util.checkForUpdates
import io.github.kanpov.litaggregator.engine.util.io.readFile
import io.github.kanpov.litaggregator.engine.util.io.writeFile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.io.File
import java.net.URI
import kotlin.system.exitProcess

class ProfileSelectScreen : Screen {
    private var bufferedUpdate: AppUpdate? = null

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        resizeAppWindow(SMALL_WINDOW_SIZE, false)

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            // heading
            H5Text(DesktopLocale["profile_select.select_your_profile"], modifier = Modifier.align(Alignment.CenterHorizontally))

            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight()
            ) {
                ProfileButtons(navigator)
                RecentProfiles(navigator)
                Spacer(modifier = Modifier.weight(1f))
                BottomRow(navigator)
            }
        }
    }

    @Composable
    private fun ColumnScope.ProfileButtons(navigator: Navigator) {
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp)
        ) {
            // load existing profile button
            var showFilePicker by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    showFilePicker = true
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                H6Text(DesktopLocale["profile_select.load_profile"])
            }

            // create new profile button
            Button(
                onClick = {
                    ConfigScreen.startConfig(navigator, ConfigIntent.CreateNewProfile)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primaryVariant
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 15.dp)
            ) {
                H6Text(DesktopLocale["profile_select.create_profile"])
            }

            // file picking
            FilePicker(show = showFilePicker, fileExtensions = listOf(PROFILE_EXTENSION)) { mpFile ->
                showFilePicker = false
                if (mpFile != null) {
                    val file = File(mpFile.path)
                    val relativePath = file.name
                    writeFile(EnginePlatform.current.getPersistentPath(relativePath), readFile(file) ?: return@FilePicker)
                    ProfileCache.add(CachedProfile(relativePath, profileName = file.nameWithoutExtension))
                    restartScreen(navigator)
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.RecentProfiles(navigator: Navigator) {
        Column(
            modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally)
        ) {
            H6Text(DesktopLocale["profile_select.recent_profiles"], modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            val starPrioritizedProfiles = ProfileCache.iterator()
                .asSequence().sortedByDescending { it.starred }.toList()
            if (starPrioritizedProfiles.isEmpty()) {
                H6Text(DesktopLocale["profile_select.no_saved_profiles"], italicize = true)
            } else {
                for (cachedProfile in starPrioritizedProfiles) {
                    RecentProfile(cachedProfile, navigator)
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun RecentProfile(cachedProfile: CachedProfile, navigator: Navigator) {
        Row {
            // profile icon
            BasicIcon(painterResource("icons/dot.png"), 25.dp, modifier = Modifier.align(Alignment.CenterVertically))

            // profile name
            H6Text(cachedProfile.profileName, highlight = true, modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 15.dp)
                    .scale(1.1f)
                    .clickable {
                        navigator.push(UnlockScreen(cachedProfile))
                    })

            Spacer(modifier = Modifier.weight(1f))

            // starred or not
            BasicIcon(
                painter = if (cachedProfile.starred) painterResource("icons/star_filled.png")
                            else painterResource("icons/star.png"),
                size = 40.dp,
                modifier = Modifier.padding(start = 10.dp).align(Alignment.CenterVertically).clickable {
                    cachedProfile.starred = !cachedProfile.starred
                    ProfileCache.write()
                    restartScreen(navigator)
                }
            )

            // info
            var infoDialogShown by remember { mutableStateOf(false) }

            BasicIcon(
                painter = painterResource("icons/info.png"),
                size = 40.dp,
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 5.dp).clickable {
                    infoDialogShown = true
                }
            )

            // delete
            var confirmDeleteDialogShown by remember { mutableStateOf(false) }

            BasicIcon(
                painter = painterResource("icons/delete.png"),
                size = 40.dp,
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 5.dp).clickable {
                    confirmDeleteDialogShown = true
                }
            )

            // dialogs
            if (confirmDeleteDialogShown) {
                ConfirmDeleteDialog(cachedProfile, navigator) { confirmDeleteDialogShown = false }
            }
            if (infoDialogShown) {
                InfoDialog(cachedProfile) { infoDialogShown = false }
            }
        }
    }

    @Composable
    private fun BottomRow(navigator: Navigator) {
        val coroutineScope = rememberCoroutineScope()

        Row {
            // system config
            HoverableIconButton(
                tooltip = DesktopLocale["browser.action_bar.system_config_tooltip"],
                iconPath = "icons/system_config.png"
            ) {
                navigator.push(SystemConfigScreen())
            }
            // check for updates
            var updateDialogShown by remember { mutableStateOf(false) }
            var errorDialogShown by remember { mutableStateOf(false) }
            HoverableIconButton(
                tooltip = DesktopLocale["profile_select.update_check_tooltip"],
                iconPath = "icons/sync.png"
            ) {
                coroutineScope.launch {
                    try {
                        bufferedUpdate = checkForUpdates()
                    } catch (_: Exception) {
                        errorDialogShown = true
                    }
                    if (!errorDialogShown) updateDialogShown = true
                }
            }
            if (errorDialogShown) {
                ErrorWhileCheckingUpdatesDialog { errorDialogShown = false }
            }
            if (updateDialogShown) {
                UpdatesDialog(bufferedUpdate) { updateDialogShown = false }
            }
            // GitHub repository
            HoverableIconButton(
                tooltip = DesktopLocale["profile_select.github_tooltip"],
                iconPath = "icons/github.png"
            ) {
                DesktopEnginePlatform.openBrowser(URI.create("https://github.com/$APP_REPOSITORY"))
            }
            // exit
            HoverableIconButton(
                tooltip = DesktopLocale["profile_select.exit_tooltip"],
                iconPath = "icons/exit.png"
            ) {
                exitProcess(status = 0)
            }
            // app version
            Spacer(modifier = Modifier.weight(1f))
            H6Text(DesktopLocale["profile_select.version_formatting", APP_VERSION], italicize = true)
        }
    }

    @Composable
    private fun ErrorWhileCheckingUpdatesDialog(onDismissRequest: () -> Unit) {
        Dialog(
            onDismissRequest = onDismissRequest
        ) {
            Card(shape = RoundedCornerShape(size = 5.dp)) {
                H6Text(
                    DesktopLocale["profile_select.error_occurred_while_checking_updates"],
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }

    @Composable
    private fun UpdatesDialog(update: AppUpdate?, onDismissRequest: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                H6Text(if (update == null) {
                    DesktopLocale["profile_select.no_updates_available"]
                } else {
                    DesktopLocale["profile_select.updates_available"]
                }, highlight = true)
            },
            text = {
                if (update == null) {
                    H6Text(DesktopLocale["profile_select.no_updates_formatting", APP_VERSION])
                } else {
                    Column {
                        H6Text(DesktopLocale["profile_select.updates_available_1", APP_VERSION, update.version])
                        H6Text(DesktopLocale["profile_select.updates_available_2"], highlight = true, modifier = Modifier.padding(top = 10.dp))
                        H6Text(update.url, color = Color.Blue, modifier = Modifier.clickable {
                            DesktopEnginePlatform.openBrowser(URI.create(update.url))
                        }.padding(top = 5.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    H6Text(DesktopLocale["button.ok"], highlight = true)
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
    }

    @Composable
    private fun ConfirmDeleteDialog(cachedProfile: CachedProfile, navigator: Navigator, onDismissRequest: () -> Unit) {
        AlertDialog(
            title = {
                H6Text(DesktopLocale["profile_select.profile_delete_dialog.heading"], highlight = true)
            },
            text = {
                H6Text(DesktopLocale["profile_select.profile_delete_dialog.text", cachedProfile.profileName])
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                Button(
                    onClick = {
                        onDismissRequest()
                        ProfileCache.remove(cachedProfile)
                        restartScreen(navigator)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    H6Text(DesktopLocale["button.confirm"], highlight = true)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    H6Text(DesktopLocale["button.cancel"], highlight = true)
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
    }

    @Composable
    private fun InfoDialog(cachedProfile: CachedProfile, onDismissRequest: () -> Unit) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(size = 10.dp)
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier.padding(15.dp)
                    ) {
                        H6Text(DesktopLocale["profile_select.info_dialog.path_to_file"], highlight = true)

                        H6Text(cachedProfile.file.absolutePath, modifier = Modifier.padding(top = 10.dp))

                        H6Text(
                            DesktopLocale["profile_select.info_dialog.file_size"],
                            highlight = true, modifier = Modifier.padding(top = 15.dp))

                        val kbFileSize = cachedProfile.file.length() / 1024L
                        H6Text(DesktopLocale["profile_select.info_dialog.kb", kbFileSize], modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
}
