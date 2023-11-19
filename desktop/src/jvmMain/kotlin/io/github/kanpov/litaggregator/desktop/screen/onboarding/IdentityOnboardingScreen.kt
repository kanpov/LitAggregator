package io.github.kanpov.litaggregator.desktop.screen.onboarding

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.MAX_VALID_GROUP
import io.github.kanpov.litaggregator.engine.settings.MAX_VALID_PARALLEL
import io.github.kanpov.litaggregator.engine.settings.MIN_VALID_GROUP
import io.github.kanpov.litaggregator.engine.settings.MIN_VALID_PARALLEL

class IdentityOnboardingScreen(profile: Profile, index: Int) : OnboardingScreen(profile, index) {
    @Composable
    override fun ColumnScope.OnboardingContent() {
        TextQuestion(
            text = Locale["onboarding.identity.profile_name"],
            onChangeAnswer = { profile.identity.profileName = it },
            validator = { it.isNotBlank() },
            placeholder = Locale["onboarding.identity.profile_name_hint"]
        )

        TextQuestion(
            text = Locale["onboarding.identity.parallel"],
            onChangeAnswer = { profile.identity.parallel = it.toInt() },
            validator = { it.toIntOrNull() != null && it.toInt() >= MIN_VALID_PARALLEL && it.toInt() <= MAX_VALID_PARALLEL },
            placeholder = Locale["onboarding.identity.parallel_hint"]
        )

        TextQuestion(
            text = Locale["onboarding.identity.group"],
            onChangeAnswer = { profile.identity.group = it.toInt() },
            validator = { it.toIntOrNull() != null && it.toInt() >= MIN_VALID_GROUP && it.toInt() <= MAX_VALID_GROUP },
            placeholder = Locale["onboarding.identity.group_hint"]
        )
    }
}