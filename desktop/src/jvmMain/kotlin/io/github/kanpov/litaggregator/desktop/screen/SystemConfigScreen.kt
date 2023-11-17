package io.github.kanpov.litaggregator.desktop.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.locale.Locale
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.desktop.platform.DesktopSystemConfig
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class SystemConfigScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            // heading
            Text(
                text = Locale.current.systemConfig.configureYourSystem,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // language setting
            DropdownSetting(
                label = Locale.current.systemConfig.uiLanguage,
                startOption = Locale.locales.first { it.localeId == DesktopEnginePlatform.systemConfig!!.localeId }.localeName,
                options = Locale.locales.map { it.localeName },
                onSelectOption = { localeName ->
                    DesktopEnginePlatform.updateSystemConfig {
                        localeId = Locale.locales.first { it.localeName == localeName }.localeId
                    }
                }
            )

            // web driver support setting
            StaticSetting(
                ok = DesktopEnginePlatform.systemConfig!!.supportsWebDriver,
                label = Locale.current.systemConfig.supportsWebDriver
            )

            // awt desktop support setting
            StaticSetting(
                ok = DesktopEnginePlatform.systemConfig!!.supportsAwtDesktop,
                label = Locale.current.systemConfig.supportsAwtDesktop
            )

            // shell browser support setting
            StaticSetting(
                ok = DesktopEnginePlatform.systemConfig!!.supportsShellBrowserInvocation,
                label = Locale.current.systemConfig.supportsShellBrowserInvocation,
                whenOk = {
                    // exact shell setting
                    TypedSetting(
                        label = Locale.current.systemConfig.shellBinary,
                        defaultValue = DesktopEnginePlatform.systemConfig!!.shellBinary,
                        setValue = { shellBinary = it }
                    )

                    // exact browser setting
                    TypedSetting(
                        label = Locale.current.systemConfig.browserBinary,
                        defaultValue = DesktopEnginePlatform.systemConfig!!.browserBinary,
                        setValue = { browserBinary = it }
                    )
                }
            )

            // continue button
            Button(
                onClick = {
                    navigator.push(ProfileSelectScreen())
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Cyan
                ),
                modifier = Modifier.padding(top = 15.dp).scale(1.1f).align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = Locale.current.button.continueButton,
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun StaticSetting(ok: Boolean, label: String, whenOk: @Composable () -> Unit = { }) {
        val iconPainter = if (ok) painterResource("icons/ok.png") else painterResource("icons/cross.png")

        Row(modifier = Modifier.padding(top = 10.dp)) {
            SettingIcon(iconPainter)

            Text(
                text = label,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 10.dp)
            )
        }

        if (ok) whenOk()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun QuestionSetting(label: String, content: @Composable () -> Unit) {
        Row(modifier = Modifier.padding(top = 10.dp)) {
            SettingIcon(painterResource("icons/question_mark.png"))

            Text(
                text = label,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 10.dp)
            )

            content()
        }
    }

    @Composable
    private fun TypedSetting(label: String, defaultValue: String, setValue: DesktopSystemConfig.(String) -> Unit) {
        QuestionSetting(label = label) {
            var textFieldValue by remember { mutableStateOf(TextFieldValue(
                annotatedString = AnnotatedString(text = defaultValue)
            )) }

            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    DesktopEnginePlatform.updateSystemConfig {
                        this.setValue(newValue.text)
                    }
                },
                textStyle = MaterialTheme.typography.h6,
                singleLine = true,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }

    @Composable
    private fun DropdownSetting(label: String, startOption: String, options: List<String>, onSelectOption: (String) -> Unit) {
        QuestionSetting(label = label) {
            var expanded by remember { mutableStateOf(false) }
            var selectedOption by remember { mutableStateOf(startOption) }

            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { expanded = !expanded }.size(35.dp)
                    )
                },
                readOnly = true,
                textStyle = MaterialTheme.typography.h6,
                modifier = Modifier.padding(start = 10.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.padding(start = 10.dp)
            ) {
                for (option in options) {
                    DropdownMenuItem(onClick = {
                        onSelectOption(option)
                        selectedOption = option
                        expanded = false
                    }) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SettingIcon(painter: Painter) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
    }
}