package io.github.kanpov.litaggregator.desktop

import androidx.compose.ui.window.singleWindowApplication
import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.desktop.platform.DesktopGoogleAuthorizer
import io.github.kanpov.litaggregator.engine.Engine
import io.github.kanpov.litaggregator.engine.ProfileLoadResult
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.settings.*
import io.github.kanpov.litaggregator.engine.profile.EncryptionOptions
import io.github.kanpov.litaggregator.engine.profile.Profile
import kotlinx.coroutines.*

fun main() = singleWindowApplication {
    val engine = Engine(DesktopEnginePlatform, "profile")
    if (engine.loadProfile("test_pass") == ProfileLoadResult.NoSuchProfile) {
        engine.createProfile(
            EncryptionOptions(),
            Profile(IdentitySettings("t", 8, 1), ProviderSettings(), Authorization(), FeedSettings(), Feed()),
            "test_pass"
        )
    }

    CoroutineScope(Dispatchers.Default).launch {
        engine.setupAuthorizer(DesktopGoogleAuthorizer())
    }
}

