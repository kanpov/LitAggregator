package io.github.kanpov.litaggregator.desktop

import androidx.compose.ui.window.singleWindowApplication
import io.github.kanpov.litaggregator.desktop.runtime.DesktopEngineRuntime
import io.github.kanpov.litaggregator.engine.Engine
import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.StandardClientCredentials
import io.github.kanpov.litaggregator.engine.authorizer.UlyssAuthorizer
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.settings.*
import io.github.kanpov.litaggregator.engine.profile.EncryptionOptions
import io.github.kanpov.litaggregator.engine.profile.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() = singleWindowApplication {
    val engine = Engine(DesktopEngineRuntime, "TestProfile")
    if (!engine.loadProfile("test_pass")) {
        engine.createProfile(
            EncryptionOptions(),
            Profile(IdentitySettings("t", 8, 1), ProviderSettings(), Authorization(), FeedSettings(), Feed()),
            "test_pass"
        )
    }

    CoroutineScope(Dispatchers.Default).launch {
        val (feed, errors) = engine.fetchFeed()

        println("Feed: $feed")
        println("Errors in: $errors")

//        val r = engine.setupAuthorizer(UlyssAuthorizer(StandardClientCredentials("Karpov.Anton", "894052")))
    }
}

