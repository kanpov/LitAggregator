package io.github.kanpov.litaggregator.desktop

import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.profile.ProfileEncryptionOptions
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.FeedSettings
import io.github.kanpov.litaggregator.engine.settings.IdentitySettings
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import kotlinx.coroutines.runBlocking

fun main() {
    EnginePlatform.current = DesktopEnginePlatform

    // basic no-ask logic to load from cache or create profile and load into manager
    val cachedProfileFile = ProfileManager.tryLocateCachedProfile()
    val manager: ProfileManager = if (cachedProfileFile == null) {
        ProfileManager.fromNewProfile(
            profile = Profile(identity = IdentitySettings("a", 8, 1),
                providers = ProviderSettings(), authorization = Authorization(), feedSettings = FeedSettings(),
                feed = Feed()),
            options = ProfileEncryptionOptions(),
            profileName = "a",
            password = "test"
        )
    } else {
        ProfileManager.fromCachedProfile(cachedProfileFile, "test")
    }

    runBlocking {
        manager.withProfile {
            refreshFeed()
        }

        manager.writeToDisk()
    }
}
