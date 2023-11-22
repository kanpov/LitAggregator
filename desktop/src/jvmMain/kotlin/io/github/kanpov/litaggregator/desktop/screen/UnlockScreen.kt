package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.desktop.browser.BrowserScreen
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.engine.profile.CachedProfile
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class UnlockScreen(private val cachedProfile: CachedProfile) : Screen {
    @OptIn(ExperimentalResourceApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            BasicIcon(
                painter = painterResource("icons/back_arrow.png"),
                size = 30.dp,
                modifier = Modifier.clickable { navigator.pop() }
                    .align(Alignment.TopStart).padding(top = 5.dp, start = 5.dp)
            )

            Column(modifier = Modifier.align(Alignment.Center)) {
                H6Text(
                    Locale["unlock.enter_password"],
                    highlight = true,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                var password by remember { mutableStateOf("") }
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally),
                    visualTransformation = PasswordVisualTransformation()
                )

                var showError by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        enterProfile(navigator, password) {
                            coroutineScope.launch {
                                if (!showError) {
                                    showError = true
                                    delay(2000L)
                                    showError = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally)
                ) {
                    H6Text(Locale["button.login"])
                }

                if (showError) {
                    H6Text(
                        Locale["unlock.repeat_attempt"],
                        italicize = true,
                        modifier = Modifier.padding(top = 10.dp).align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    private fun enterProfile(navigator: Navigator, password: String, errorCallback: () -> Unit) {
        val (_, manager) = ProfileManager.fromCache(cachedProfile, password)

        if (manager == null) {
            errorCallback()
        } else {
            navigator.push(BrowserScreen(manager))
        }
    }
}
