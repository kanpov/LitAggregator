package io.github.kanpov.litaggregator.desktop.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H5Text
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.desktop.screen.main.MainScreen
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.FeedSettings
import io.github.kanpov.litaggregator.engine.settings.IdentitySettings
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

abstract class OnboardingScreen(protected val profile: Profile, private val index: Int) : Screen {
    private lateinit var setValidity: (String, Boolean) -> Unit
    private lateinit var getValidity: (String) -> Boolean

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val validityTracker = remember { mutableStateMapOf<String, Boolean>() }

        setValidity = { text, value -> validityTracker[text] = value }
        getValidity = { text ->
            val res = validityTracker[text]
            if (res == null) {
                validityTracker[text] = false
                false
            } else {
                res
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            H5Text(Locale["onboarding.creating_profile"], modifier = Modifier.align(Alignment.CenterHorizontally))

            Column(
                modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally)
            ) {
                OnboardingContent()
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // next and before
            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                SwitchButton(if (index > 0) Locale["button.previous"] else Locale["button.cancel"], offset = -1, navigator)

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                SwitchButton(Locale["button.next"], offset = 1, navigator,
                    enabled = validityTracker.all { (_, valid) -> valid })
            }
        }
    }

    @Composable
    protected fun TextQuestion(text: String, onChangeAnswer: (String) -> Unit,
                               validator: (String) -> Boolean, placeholder: String = "", singleLine: Boolean = true) {
        BaseQuestion(text) {
            var answer by remember { mutableStateOf("") }

            TextField(
                value = answer,
                onValueChange = {
                    answer = it
                    if (validator(it)) {
                        onChangeAnswer(it)
                        setValidity(text, true)
                    } else {
                        setValidity(text, false)
                    }
                },
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.h6,
                placeholder = {
                    H6Text(placeholder, italicize = true)
                }
            )
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun BaseQuestion(text: String, validated: Boolean = true, content: @Composable RowScope.() -> Unit) {
        Row(
            modifier = Modifier.padding(top = 10.dp)
        ) {
            if (validated) {
                val iconPath = if (getValidity(text)) "icons/ok.png" else "icons/cross.png"
                BasicIcon(painterResource(iconPath), 40.dp, modifier = Modifier.align(Alignment.CenterVertically))
            }
            val textPadding = if (validated) 15.dp else 0.dp

            H6Text(text = text, modifier = Modifier.align(Alignment.CenterVertically).padding(start = textPadding))
            Row(
                modifier = Modifier.padding(start = 20.dp)
            ) {
                content()
            }
        }
    }

    @Composable
    private fun SwitchButton(text: String, offset: Int, navigator: Navigator, enabled: Boolean = true) {
        val onClick = { switchOnboardingScreen(navigator, profile, index, offset) }

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Cyan
            ),
            enabled = enabled
        ) {
            H6Text(text, highlight = true)
        }
    }

    @Composable
    abstract fun ColumnScope.OnboardingContent()

    companion object {
        private val screenInvokers: List<(Profile, Int) -> OnboardingScreen> = listOf(::IdentityOnboardingScreen)

        fun startOnboarding(navigator: Navigator) {
            val emptyProfile = Profile(IdentitySettings(), ProviderSettings(), Authorization(), FeedSettings(), Feed())
            navigator.push(screenInvokers.first().invoke(emptyProfile, 0))
        }

        private fun switchOnboardingScreen(navigator: Navigator, profile: Profile, index: Int, offset: Int) {
            val newIndex = index + offset

            if (newIndex < 0) { // onboarding cancelled
                navigator.pop()
                return
            }

            if (newIndex > screenInvokers.size - 1) { // onboarding complete
                // TODO create profile manager and save the newly created profile
                navigator.push(MainScreen())
                return
            }

            val newScreen = screenInvokers[newIndex].invoke(profile, newIndex)
            navigator.push(newScreen)
        }
    }
}