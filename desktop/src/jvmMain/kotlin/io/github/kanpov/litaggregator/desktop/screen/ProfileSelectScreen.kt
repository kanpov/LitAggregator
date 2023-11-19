package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.desktop.SMALL_WINDOW_SIZE
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H5Text
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.desktop.resizeAppWindow
import io.github.kanpov.litaggregator.desktop.screen.onboarding.OnboardingScreen
import io.github.kanpov.litaggregator.engine.profile.CachedProfile
import io.github.kanpov.litaggregator.engine.profile.ProfileCache
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class ProfileSelectScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        resizeAppWindow(SMALL_WINDOW_SIZE)

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            // heading
            H5Text(Locale["profile_select.select_your_profile"], modifier = Modifier.align(Alignment.CenterHorizontally))

            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight()
            ) {
                ProfileButtons(navigator)
                RecentProfiles(navigator)
            }
        }
    }

    @Composable
    private fun ColumnScope.ProfileButtons(navigator: Navigator) {
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp)
        ) {
            // load existing profile button
            Button(
                onClick = {
                    // TODO implement
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Cyan
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                H6Text(Locale["profile_select.load_profile"])
            }

            // create new profile button
            Button(
                onClick = {
                    OnboardingScreen.startOnboarding(navigator)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Green
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 15.dp)
            ) {
                H6Text(Locale["profile_select.create_profile"])
            }
        }
    }

    @Composable
    private fun ColumnScope.RecentProfiles(navigator: Navigator) {
        Column(
            modifier = Modifier.padding(top = 20.dp).align(Alignment.CenterHorizontally)
        ) {
            H6Text(Locale["profile_select.recent_profiles"], modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            val starPrioritizedProfiles = ProfileCache.iterator()
                .asSequence().sortedByDescending { it.starred }.toList()
            if (starPrioritizedProfiles.isEmpty()) {
                H6Text(Locale["profile_select.no_saved_profiles"], italicize = true)
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
            BasicIcon(painterResource("icons/profile.png"), 25.dp, modifier = Modifier.align(Alignment.CenterVertically))

            // profile name
            H6Text(cachedProfile.profileName, highlight = true, modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 5.dp)
                    .clickable {
                    // TODO implement
                })

            Spacer(
                modifier = Modifier.weight(1f)
            )

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
                painter = painterResource("icons/file_delete.png"),
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
    private fun ConfirmDeleteDialog(cachedProfile: CachedProfile, navigator: Navigator, onDismissRequest: () -> Unit) {
        AlertDialog(
            title = {
                H6Text(Locale["profile_select.profile_delete_dialog.heading"], highlight = true)
            },
            text = {
                H6Text(Locale["profile_select.profile_delete_dialog.text", cachedProfile.profileName])
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
                    H6Text(Locale["button.confirm"], highlight = true)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Yellow
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    H6Text(Locale["button.cancel"], highlight = true)
                }
            }
        )
    }

    @Composable
    private fun InfoDialog(cachedProfile: CachedProfile, onDismissRequest: () -> Unit) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                shape = RoundedCornerShape(size = 10.dp)
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier.padding(15.dp)
                    ) {
                        H6Text(Locale["profile_select.info_dialog.path_to_file"], highlight = true)

                        H6Text(cachedProfile.file.absolutePath, modifier = Modifier.padding(top = 10.dp))

                        H6Text(Locale["profile_select.info_dialog.file_size"],
                            highlight = true, modifier = Modifier.padding(top = 15.dp))

                        val kbFileSize = cachedProfile.file.length() / 1024L
                        H6Text(Locale["profile_select.info_dialog.kb", kbFileSize], modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
}
