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
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.desktop.platform.DesktopGoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorization.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.authorization.CredentialPair
import io.github.kanpov.litaggregator.engine.authorization.UlyssesAuthorizer
import io.github.kanpov.litaggregator.engine.profile.Profile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class AuthorizationConfigScreen(profile: Profile, index: Int) : ConfigScreen(
    DesktopLocale["config.authorization"], profile, index) {
    @Composable
    override fun ConfigContent() {
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

            CredentialAsker(DesktopLocale["config.authorization.login"]) { login = it }
            CredentialAsker(DesktopLocale["config.authorization.password"], sensitive = true, limiter = 15) { password = it }
        }
    }

    @Composable
    private fun RowScope.MeshAuthorizationElement() {
        LoginPasswordAuthorizationElement(
            title = DesktopLocale["config.authorization.mesh"],
            alreadyAuthorized = profile.authorization.mesh != null
        ) { login, password ->
            profile.setupAuthorizer(MeshAuthorizer(CredentialPair(login, password)))
        }
    }

    @Composable
    private fun RowScope.UlyssesAuthorizationElement() {
        var useKeyWord: Boolean? by remember { mutableStateOf(null) }
        var login: String by remember { mutableStateOf("") }
        var password: String by remember { mutableStateOf("") }

        AuthorizationElement(
            title = DesktopLocale["config.authorization.ulysses"],
            alreadyAuthorized = profile.authorization.ulysses != null,
            authorizer = {
                profile.setupAuthorizer(UlyssesAuthorizer(CredentialPair(login, password)))
            },
            isReadyToAuth = { if (useKeyWord == true) password.isNotBlank() else password.isNotBlank() && login.isNotBlank() }
        )  {
            // choosing method
            if (useKeyWord == null) {
                H6Text(DesktopLocale["config.authorization.choose_method"], modifier = Modifier.padding(top = 20.dp))

                Button(
                    onClick = { useKeyWord = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 15.dp)
                ) {
                    H6Text(DesktopLocale["config.authorization.ulysses.by_account"])
                }

                Button(
                    onClick = { useKeyWord = true },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primaryVariant
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp)
                ) {
                    H6Text(DesktopLocale["config.authorization.ulysses.by_keyword"])
                }
            }

            // auth by keyword
            if (useKeyWord == true) {
                CredentialAsker(
                    DesktopLocale["config.authorization.ulysses.keyword"],
                    sensitive = true,
                    limiter = 20,
                    onValueChange = { password = it }
                )
            }

            // auth by account
            if (useKeyWord == false) {
                CredentialAsker(
                    DesktopLocale["config.authorization.login"],
                    onValueChange = { login = it }
                )
                CredentialAsker(
                    DesktopLocale["config.authorization.password"],
                    sensitive = true,
                    limiter = 15,
                    onValueChange = { password = it }
                )
            }
        }
    }

    @Composable
    private fun RowScope.GoogleAuthorizationElement() {
        AuthorizationElement(
            DesktopLocale["config.authorization.google"],
            authorizer = {
                profile.setupAuthorizer(DesktopGoogleAuthorizer())
            },
            alreadyAuthorized = profile.authorization.googleSession != null,
            isReadyToAuth = { true }) {

            H6Text(DesktopLocale["config.authorization.account_data_not_required"], italicize = true)
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
                            DesktopLocale["button.bound"]
                        } else {
                            if (authorizationFailed) {
                                DesktopLocale["button.try_again"]
                            } else {
                                DesktopLocale["button.bind"]
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
