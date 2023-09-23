package io.github.kanpov.litaggregator.desktop

import androidx.compose.ui.window.singleWindowApplication
import io.github.kanpov.litaggregator.desktop.runtime.DesktopEngineRuntime
import io.github.kanpov.litaggregator.engine.Engine
import io.github.kanpov.litaggregator.engine.data.*
import io.github.kanpov.litaggregator.engine.profile.EncryptionOptions
import io.github.kanpov.litaggregator.engine.profile.Profile

fun main() = singleWindowApplication {
    val engine = Engine(DesktopEngineRuntime, "TestProfile")
    if (!engine.loadProfile("test_pass")) {
        engine.createProfile(
            EncryptionOptions(),
            Profile(Identity("t", 8, 1), ProviderSettings(), Authorization(), FeedSettings(), Feed()),
            "test_pass"
        )
    }
}

