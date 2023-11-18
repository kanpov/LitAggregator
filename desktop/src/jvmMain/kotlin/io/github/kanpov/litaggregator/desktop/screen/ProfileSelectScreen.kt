package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.engine.profile.CachedProfile
import io.github.kanpov.litaggregator.engine.profile.ProfileCache
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class ProfileSelectScreen : Screen {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            // heading
            Text(
                text = Locale["profile_select.select_your_profile"],
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight()
            ) {
                ProfileButtons()
                RecentProfiles()
            }
        }
    }

    @Composable
    private fun RowScope.ProfileButtons() {
        Column(
            modifier = Modifier.align(Alignment.CenterVertically)
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
                Text(
                    text = Locale["profile_select.load_profile"],
                    style = MaterialTheme.typography.h6
                )
            }

            // create new profile button
            Button(
                onClick = {
                    // TODO implement
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Green
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 15.dp)
            ) {
                Text(
                    text = Locale["profile_select.create_profile"],
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }

    @Composable
    private fun RowScope.RecentProfiles() {
        Column(
            modifier = Modifier.padding(start = 30.dp).align(Alignment.CenterVertically)
        ) {
            Text(
                text = Locale["profile_select.recent_profiles"],
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            val starPrioritizedProfiles = ProfileCache.iterator()
                .asSequence().sortedByDescending { it.starred }.toList()
            if (starPrioritizedProfiles.isEmpty()) {
                Text(
                    text = Locale["profile_select.no_saved_profiles"],
                    style = MaterialTheme.typography.h6,
                    fontStyle = FontStyle.Italic
                )
            } else {
                for (cachedProfile in starPrioritizedProfiles) {
                    RecentProfile(cachedProfile)
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun RecentProfile(cachedProfile: CachedProfile) {
        Row {
            // profile icon
            Image(
                painter = painterResource("icons/profile.png"),
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.CenterVertically)
            )

            // profile name
            Text(
                text = cachedProfile.profileName,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 5.dp)
                    .clickable {
                    // TODO implement
                }
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // starred or not
            var starred by remember { mutableStateOf(cachedProfile.starred) }
            Image(
                painter = if (starred) painterResource("icons/star_filled.png") else painterResource("icons/star.png"),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 10.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                    starred = !starred
                    cachedProfile.starred = starred
                    ProfileCache.write()
                }
            )

            // info
            var infoDialogShown by remember { mutableStateOf(false) }

            Image(
                painter = painterResource("icons/info.png"),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
                    .padding(start = 5.dp)
                    .clickable {
                    infoDialogShown = true
                }
            )

            // delete
            var confirmDeleteDialogShown by remember { mutableStateOf(false) }

            Image(
                painter = painterResource("icons/file_delete.png"),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
                    .padding(start = 5.dp)
                    .clickable {
                    confirmDeleteDialogShown = true
                }
            )

            // dialogs
            if (confirmDeleteDialogShown) {
                ConfirmDeleteDialog(cachedProfile) { confirmDeleteDialogShown = false }
            }
            if (infoDialogShown) {
                InfoDialog(cachedProfile) { infoDialogShown = false }
            }
        }
    }

    @Composable
    private fun ConfirmDeleteDialog(cachedProfile: CachedProfile, onDismissRequest: () -> Unit) {
        AlertDialog(
            title = {
                Text(
                    text = Locale["profile_select.profile_delete_dialog.heading"],
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = Locale["profile_select.profile_delete_dialog.text", cachedProfile.profileName],
                    style = MaterialTheme.typography.h6
                )
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                Button(
                    onClick = {
                        // TODO implement
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = Locale["button.confirm"],
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Text(
                        text = Locale["button.cancel"],
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        Text(
                            text = Locale["profile_select.info_dialog.path_to_file"],
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = cachedProfile.file.absolutePath,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        val kbFileSize = cachedProfile.file.length() / 1024L

                        Text(
                            text = Locale["profile_select.info_dialog.file_size"],
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 15.dp)
                        )

                        Text(
                            text = Locale["profile_select.info_dialog.kb", kbFileSize],
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}