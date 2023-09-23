package io.github.kanpov.litaggregator.engine

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.kanpov.litaggregator.engine.profile.EncryptionOptions
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.profile.WrappedProfile
import io.github.kanpov.litaggregator.engine.util.jsonInstance
import io.github.kanpov.litaggregator.engine.util.readFile
import io.github.kanpov.litaggregator.engine.util.writeFile
import java.io.File

class Engine(runtime: EngineRuntime, profileName: String) {
    private val profileFile: File
    private lateinit var wrappedProfile: WrappedProfile
    private lateinit var profile: Profile

    init {
        Napier.base(DebugAntilog())
        Napier.i { "Running the engine on platform: ${runtime.name}" }

        EngineRuntime.current = runtime

        val profilePath = runtime.getPersistentPath("$profileName.json")
        profileFile = File(profilePath)
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
        return true
    }

    fun saveProfile() {
        wrappedProfile.rewrap(profile)
        writeFile(profileFile, jsonInstance.encodeToString(WrappedProfile.serializer(), wrappedProfile))
    }
}
