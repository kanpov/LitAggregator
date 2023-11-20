package io.github.kanpov.litaggregator.desktop.screen.config

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import io.github.kanpov.litaggregator.desktop.MEDIUM_WINDOW_SIZE
import io.github.kanpov.litaggregator.desktop.components.BasicIcon
import io.github.kanpov.litaggregator.desktop.components.H5Text
import io.github.kanpov.litaggregator.desktop.components.H6Text
import io.github.kanpov.litaggregator.desktop.resizeAppWindow
import io.github.kanpov.litaggregator.desktop.screen.main.MainScreen
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorization.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.FeedSettings
import io.github.kanpov.litaggregator.engine.settings.IdentitySettings
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

abstract class ConfigScreen(private val name: String, protected val profile: Profile, private val index: Int) : Screen {
    private lateinit var setValidity: (String, Boolean) -> Unit
    private lateinit var getValidity: (String) -> Boolean

    @Composable
    override fun Content() {
        resizeAppWindow(MEDIUM_WINDOW_SIZE)

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
            // creating profile heading
            Spacer(modifier = Modifier.height(5.dp))
            H5Text(Locale["config.configuring_profile"], modifier = Modifier.align(Alignment.CenterHorizontally))

            // category sub-heading
            H6Text(
                "${index + 1}. $name", highlight = true,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp)
            )

            // category content
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                OnboardingContent()
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // next and before
            Row(
                modifier = Modifier.padding(5.dp)
            ) {
                SwitchButton(if (index == 0) Locale["button.cancel"] else Locale["button.previous"],
                    offset = -1, navigator, color = MaterialTheme.colors.primary)

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                SwitchButton(if (index == screenInvokers.size - 1) Locale["button.finish"] else Locale["button.next"],
                    offset = 1, navigator,
                    enabled = validityTracker.all { (_, valid) -> valid }, color = MaterialTheme.colors.primaryVariant)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    protected fun ValidatedQuestion(text: String, onChangeAnswer: (String) -> Unit, validator: (String) -> Boolean,
                                    placeholder: String = "", knownValue: String? = null) {
        Row(
            modifier = Modifier.padding(top = 10.dp)
        ) {
            // validity icon
            val iconPath = if (getValidity(text)) "icons/ok.png" else "icons/cross.png"
            BasicIcon(painterResource(iconPath), 40.dp, modifier = Modifier.align(Alignment.CenterVertically))

            // text
            H6Text(text, modifier = Modifier.align(Alignment.CenterVertically).padding(start = 15.dp))

            // set up question's answer based off known value if needed
            var answer by remember { mutableStateOf("") }
            if (knownValue != null && validator(knownValue) && answer == "") {
                onChangeAnswer(knownValue)
            }

            // input field
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
                singleLine = true,
                textStyle = MaterialTheme.typography.h6,
                placeholder = {
                    H6Text(placeholder, italicize = true)
                }
            )
        }
    }

    @Composable
    private fun SwitchButton(text: String, offset: Int, navigator: Navigator, color: Color, enabled: Boolean = true) {
        val onClick = { switchConfigScreen(navigator, profile, index, offset) }

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = color
            ),
            enabled = enabled
        ) {
            H6Text(text)
        }
    }

    @Composable
    abstract fun OnboardingContent()

    companion object {
        private val screenInvokers: List<(Profile, Int) -> ConfigScreen> = listOf(
            ::IdentityConfigScreen,
            ::AuthorizationConfigScreen,
            ::ProviderConfigScreen,
            ::FeedConfigScreen
        )

        fun startConfig(navigator: Navigator) {
            val emptyProfile = Profile(IdentitySettings(), ProviderSettings(), AuthorizationState(), FeedSettings(), Feed())
            navigator.push(screenInvokers.first().invoke(emptyProfile, 0))
        }

        private fun switchConfigScreen(navigator: Navigator, profile: Profile, index: Int, offset: Int) {
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

            if (offset > 0) { // when moving forward, push a new screen
                val newScreen = screenInvokers[newIndex].invoke(profile, newIndex)
                navigator.push(newScreen)
            } else { // when moving backward, pop back to an existing previous screen
                navigator.pop()
            }
        }
    }
}