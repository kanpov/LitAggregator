package io.github.kanpov.litaggregator.desktop.screen.config

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.github.kanpov.litaggregator.desktop.platform.DesktopLocale
import io.github.kanpov.litaggregator.engine.profile.Profile

class FeedConfigScreen(profile: Profile, index: Int) : ConfigScreen(DesktopLocale["config.feed"], profile, index) {
    @Composable
    override fun ConfigContent() {
        Column {
            ValidatedQuestion(
                text = DesktopLocale["config.feed.max_pool_size"],
                onChangeAnswer = { profile.feedSettings.maxPoolSize = it.toInt() },
                validator = { it.toIntOrNull() != null && it.toInt() >= 10 && it.toInt() <= 10000 },
                placeholder = DesktopLocale["config.feed.max_pool_size_hint"],
                knownValue = profile.feedSettings.maxPoolSize.toString()
            )

            ValidatedQuestion(
                text = DesktopLocale["config.feed.look_behind_days"],
                onChangeAnswer = { profile.feedSettings.lookBehindDays = it.toInt() },
                validator = { it.toIntOrNull() != null && it.toInt() >= 1 && it.toInt() <= 250 },
                placeholder = DesktopLocale["config.feed.look_behind_days_hint"],
                knownValue = profile.feedSettings.lookBehindDays.toString()
            )

            ValidatedQuestion(
                text = DesktopLocale["config.feed.look_ahead_days"],
                onChangeAnswer = { profile.feedSettings.lookAheadDays = it.toInt() },
                validator = { it.toIntOrNull() != null && it.toInt() >= 1 && it.toInt() <= 100 },
                placeholder = DesktopLocale["config.feed.look_ahead_days_hint"],
                knownValue = profile.feedSettings.lookAheadDays.toString()
            )
        }
    }
}
