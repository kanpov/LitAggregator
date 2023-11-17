package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.locale.Locale
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.io.File

class ProfileSelectScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            // heading
            Text(
                text = Locale.current.profileSelect.selectYourProfile,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp)
            ) {
                ProfileButtons()
                RecentProfiles()
            }
        }
    }

    @Composable
    private fun ProfileButtons() {
        Column(
            modifier = Modifier.padding(top = 10.dp)
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
                    text = Locale.current.profileSelect.loadProfile,
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
                    text = Locale.current.profileSelect.createProfile,
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }

    @Composable
    private fun RecentProfiles() {
        val cachedProfiles = ProfileManager.locateCachedProfiles()

        Column(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp)
        ) {
            Text(
                text = Locale.current.profileSelect.recentProfiles,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )

            for (cachedProfile in cachedProfiles) {
                RecentProfile(cachedProfile)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun RecentProfile(file: File) {
        Row(
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Image(
                painter = painterResource("icons/file.png"),
                contentDescription = null,
                modifier = Modifier.size(40.dp).align(Alignment.CenterVertically)
            )

            Text(
                text = file.absolutePath,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically).clickable {
                    // TODO implement
                }
            )

            var alertShown by remember { mutableStateOf(false) }
            var alertFilePath: String? by remember { mutableStateOf(null) }

            Image(
                painter = painterResource("icons/file_delete.png"),
                contentDescription = null,
                modifier = Modifier.size(40.dp).align(Alignment.CenterVertically).clickable {
                    alertShown = true
                }
            )

            if (!alertShown) return@Row

            AlertDialog(
                title = {

                },
                text = {

                },
                onDismissRequest = {

                },
                confirmButton = {

                },
                dismissButton = {

                }
            )
        }
    }
}