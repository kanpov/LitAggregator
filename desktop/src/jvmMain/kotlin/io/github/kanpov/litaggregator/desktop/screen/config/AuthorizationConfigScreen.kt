package io.github.kanpov.litaggregator.desktop.screen.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.desktop.platform.DesktopGoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.CredentialPair
import io.github.kanpov.litaggregator.engine.authorizer.UlyssesAuthorizer
import io.github.kanpov.litaggregator.engine.profile.Profile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class AuthorizationConfigScreen(profile: Profile, index: Int) : ConfigScreen(
    Locale["config.authorization"], profile, index) {
    @Composable
    override fun OnboardingContent() {
        Row(
            modifier = Modifier.padding(top = 15.dp)
        ) {
            MeshAuthorizationElement()
            Spacer(modifier = Modifier.width(10.dp))
            UlyssesAuthorizationElement()
            Spacer(modifier = Modifier.width(10.dp))
            GoogleAuthorizationElement()
        }
    }

    @Composable
    private fun RowScope.LoginPasswordAuthorizationElement(title: String, alreadyAuthorized: Boolean,
                                                           authorizer: suspend (String, String) -> Boolean) {
        var login by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        AuthorizationElement(
            title = title,
            authorizer = { authorizer(login, password) },
            alreadyAuthorized = alreadyAuthorized,
            isReadyToAuth = { login.isNotBlank() && password.isNotBlank() }) {

            CredentialAsker(Locale["config.authorization.login"]) { login = it }
            CredentialAsker(Locale["config.authorization.password"], sensitive = true, limiter = 15) { password = it }
        }
    }

    @Composable
    private fun RowScope.MeshAuthorizationElement() {
        LoginPasswordAuthorizationElement(
            title = Locale["config.authorization.mesh"],
            alreadyAuthorized = profile.authorization.mos != null
        ) { login, password ->
            profile.setupAuthorizer(MeshAuthorizer(CredentialPair(login, password)))
        }
    }

    @Composable
    private fun RowScope.UlyssesAuthorizationElement() {
        LoginPasswordAuthorizationElement(
            title = Locale["config.authorization.ulysses"],
            alreadyAuthorized = profile.authorization.ulyss != null
        ) { login, password ->
            profile.setupAuthorizer(UlyssesAuthorizer(CredentialPair(login, password)))
        }
    }

    @Composable
    private fun RowScope.GoogleAuthorizationElement() {
        AuthorizationElement(Locale["config.authorization.google"],
            authorizer = {
                profile.setupAuthorizer(DesktopGoogleAuthorizer())
            },
            alreadyAuthorized = profile.authorization.googleSession != null,
            isReadyToAuth = { true }) {

            H6Text(Locale["config.authorization.account_data_not_required"], italicize = true)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun RowScope.AuthorizationElement(title: String, alreadyAuthorized: Boolean,
                                              authorizer: suspend () -> Boolean, isReadyToAuth: () -> Boolean,
                                              content: @Composable ColumnScope.() -> Unit = {}) {
        Surface(
            shape = RoundedCornerShape(size = 15.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).align(Alignment.CenterVertically)
            ) {
                var authorized by remember { mutableStateOf(alreadyAuthorized) }
                var authorizing by remember { mutableStateOf(false) }
                var authorizationFailed by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                // heading
                Row {
                    H6Text(title, highlight = true, modifier = Modifier.align(Alignment.CenterVertically).scale(1.1f))

                    Spacer(modifier = Modifier.weight(1f))

                    BasicIcon(
                        painter = if (authorized) painterResource("icons/ok.png") else painterResource("icons/cross.png"),
                        size = 40.dp,
                    )
                }

                // content
                if (!authorized) {
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }

                // authorize button
                Spacer(modifier = Modifier.height(10.dp))
                if (!authorizing) { // button to authorize when not authorizing
                    Button(
                        onClick = {
                            authorizing = true
                            coroutineScope.launch {
                                if (authorizer()) {
                                    authorized = true
                                } else {
                                    authorizationFailed = true
                                }
                                authorizing = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        enabled = !authorized && isReadyToAuth()
                    ) {
                        val text = if (authorized) {
                            Locale["button.bound"]
                        } else {
                            if (authorizationFailed) {
                                Locale["button.try_again"]
                            } else {
                                Locale["button.bind"]
                            }
                        }

                        H6Text(text)
                    }
                } else { // progress bar when authorizing
                    CircularProgressIndicator(
                        modifier = Modifier.width(40.dp).align(Alignment.CenterHorizontally).padding(top = 5.dp),
                        color = MaterialTheme.colors.primary,
                    )
                }
            }
        }
    }

    @Composable
    private fun CredentialAsker(title: String, sensitive: Boolean = false, limiter: Int? = null,
                                onValueChange: (String) -> Unit) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 15.dp)
        ) {
            H6Text(title, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(10.dp))

            var credential by remember { mutableStateOf("") }
            TextField(
                value = credential,
                onValueChange = { newValue ->
                    credential = if (limiter != null && newValue.length > limiter) {
                        newValue.substring(0..<limiter)
                    } else {
                        newValue
                    }
                    onValueChange(newValue)
                },
                textStyle = MaterialTheme.typography.h6,
                visualTransformation = if (sensitive) PasswordVisualTransformation() else VisualTransformation.None
            )
        }
    }
}
