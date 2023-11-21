package io.github.kanpov.litaggregator.desktop.screen.config

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.github.kanpov.litaggregator.desktop.Locale
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.MAX_VALID_GROUP
import io.github.kanpov.litaggregator.engine.settings.MAX_VALID_PARALLEL
import io.github.kanpov.litaggregator.engine.settings.MIN_VALID_GROUP
import io.github.kanpov.litaggregator.engine.settings.MIN_VALID_PARALLEL

class IdentityConfigScreen(profile: Profile, index: Int) : ConfigScreen(
    Locale["config.identity"], profile, index) {
    @Composable
    override fun ConfigContent() {
        Column {
            ValidatedQuestion(
                text = Locale["config.identity.profile_name"],
                onChangeAnswer = { profile.identity.profileName = it },
                validator = { it.isNotBlank() && it.length <= 17 },
                placeholder = Locale["config.identity.profile_name_hint"],
                knownValue = profile.identity.profileName
            )

            ValidatedQuestion(
                text = Locale["config.identity.password"],
                onChangeAnswer = { bufferedPassword = it },
                validator = { it.length >= 6 },
                placeholder = Locale["config.identity.password_hint"],
                knownValue = bufferedPassword,
                sensitive = true
            )

            ValidatedQuestion(
                text = Locale["config.identity.password_repeat"],
                onChangeAnswer = {},
                validator = { it == bufferedPassword },
                placeholder = Locale["config.identity.password_hint"],
                knownValue = bufferedPassword,
                sensitive = true
            )

            ValidatedQuestion(
                text = Locale["config.identity.parallel"],
                onChangeAnswer = { profile.identity.parallel = it.toInt() },
                validator = { it.toIntOrNull() != null && it.toInt() >= MIN_VALID_PARALLEL && it.toInt() <= MAX_VALID_PARALLEL },
                placeholder = Locale["config.identity.parallel_hint"],
                knownValue = profile.identity.parallel.toString()
            )

            ValidatedQuestion(
                text = Locale["config.identity.group"],
                onChangeAnswer = { profile.identity.group = it.toInt() },
                validator = { it.toIntOrNull() != null && it.toInt() >= MIN_VALID_GROUP && it.toInt() <= MAX_VALID_GROUP },
                placeholder = Locale["config.identity.group_hint"],
                knownValue = profile.identity.group.toString()
            )
        }
    }
}
