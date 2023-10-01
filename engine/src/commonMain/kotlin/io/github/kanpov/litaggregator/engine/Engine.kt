package io.github.kanpov.litaggregator.engine

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.kanpov.litaggregator.engine.authorizer.Authorizer
import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.UlyssAuthorizer
import io.github.kanpov.litaggregator.engine.feed.Feed
import io.github.kanpov.litaggregator.engine.profile.EncryptionOptions
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.profile.WrappedProfile
import io.github.kanpov.litaggregator.engine.provider.AuthorizedProviderDefinition
import io.github.kanpov.litaggregator.engine.provider.SimpleProviderDefinition
import io.github.kanpov.litaggregator.engine.util.asFile
import io.github.kanpov.litaggregator.engine.util.jsonInstance
import io.github.kanpov.litaggregator.engine.util.readFile
import io.github.kanpov.litaggregator.engine.util.writeFile
import java.io.File
import java.time.Instant

class Engine(platform: EnginePlatform, profileName: String) {
    private val profileFile: File
    private lateinit var wrappedProfile: WrappedProfile
    private lateinit var profile: Profile

    init {
        Napier.base(DebugAntilog())
        Napier.i { "Running the engine on platform: ${platform.name}" }

        EnginePlatform.current = platform

        profileFile = platform.getPersistentPath("$profileName.json").asFile()
    }

    fun createProfile(encryptionOptions: EncryptionOptions, profile: Profile, password: String) {
        wrappedProfile = WrappedProfile.new(encryptionOptions, profile, password)
        this.profile = profile
        writeFile(profileFile, jsonInstance.encodeToString(WrappedProfile.serializer(), this.wrappedProfile))
    }

    fun loadProfile(password: String): Boolean {
        if (!profileFile.exists()) return false
        wrappedProfile = WrappedProfile.existing(readFile(profileFile), password) ?: return false
        profile = wrappedProfile.unwrap() ?: return false
        profile.feed.homework.clear()
        return true
    }

    fun saveProfile() {
        wrappedProfile.rewrap(profile)
        val wrappedProfileJson = jsonInstance.encodeToString(WrappedProfile.serializer(), wrappedProfile)
        val snapshotJson = jsonInstance.encodeToString(Profile.serializer(), profile)
        writeFile(profileFile, wrappedProfileJson)

        // snapshot
        val snapshotFile = EnginePlatform.current
            .getPersistentPath("snapshot_${Instant.now().toEpochMilli()}.json")
            .asFile()
        writeFile(snapshotFile, snapshotJson)
    }

    suspend fun setupAuthorizer(authorizer: Authorizer): Boolean {
        if (!authorizer.authorize()) return false

        when (authorizer) {
            is UlyssAuthorizer -> profile.authorization.ulyss = authorizer
            is MosAuthorizer -> profile.authorization.mos = authorizer
            is GoogleAuthorizer -> profile.authorization.google = authorizer
        }

        return true
    }

    suspend fun refreshFeed(): Pair<Feed, Set<String> /* providers that failed */> {
        val errors = mutableSetOf<String>()

        SimpleProviderDefinition.all.forEach { definition ->
            if (definition.isEnabled(profile.providers)) {
                if (!definition.factory().run(profile)) {
                    errors += definition.name
                }
            }
        }

        AuthorizedProviderDefinition.all.forEach { definition ->
            if (definition.isEnabled(profile.providers) && definition.isAuthorized(profile.authorization)) {
                if (!definition.factory(profile.authorization).run(profile)) {
                    errors += definition.name
                }
            }
        }

        return profile.feed to errors
    }
}
