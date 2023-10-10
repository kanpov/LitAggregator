package io.github.kanpov.litaggregator.desktop

import io.github.kanpov.litaggregator.desktop.platform.DesktopEnginePlatform
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.profile.ProfileActionResult
import io.github.kanpov.litaggregator.engine.profile.ProfileManager
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.FeedSettings
import io.github.kanpov.litaggregator.engine.settings.IdentitySettings
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings

fun main() {
    EnginePlatform.current = DesktopEnginePlatform
    val manager = ProfileManager("test_profile", "test_pwd")

    if (manager.readFromDisk() == ProfileActionResult.NotFound) {
        manager.create(
            Profile(IdentitySettings("test", 8, 1), ProviderSettings(), Authorization(), FeedSettings(), Feed()),
        )
    }

    manager.writeToDisk()
}
