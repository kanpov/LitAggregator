package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.io.asFile
import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import io.github.kanpov.litaggregator.engine.util.io.readFile
import io.github.kanpov.litaggregator.engine.util.io.writeFile
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

const val PROFILE_EXTENSION = "agr"

class ProfileManager private constructor(private val profileFile: File, val password: String) {
    var currentProfile: Profile? = null
    private var currentWrapper: ProfileWrapper? = null

    fun create(profile: Profile, options: ProfileEncryptionOptions = ProfileEncryptionOptions()): ProfileResult {
        currentProfile = profile
        currentWrapper = ProfileWrapper.new(options, profile, password) ?: return ProfileResult.PasswordMismatchError
        return writeToDisk()
    }

    fun writeToDisk(): ProfileResult {
        val (wrapperJson, result) = writeToString()

        if (wrapperJson == null) return result
        if (!writeFile(profileFile, wrapperJson)) return ProfileResult.FileError

        return ProfileResult.Success
    }

    fun readFromDisk(): ProfileResult {
        if (!profileFile.exists()) return ProfileResult.NotFoundError
        return readFromString(readFile(profileFile) ?: return ProfileResult.FileError)
    }

    suspend fun withProfileSuspend(scope: suspend Profile.() -> Unit) {
        currentProfile?.scope()
    }

    fun withProfile(scope: Profile.() -> Unit) {
        currentProfile?.scope()
    }

    fun getProfile() = currentProfile

    fun countEntries(): Int? = currentProfile?.feed?.countEntries()

    fun mergeWithProfile(newProfile: Profile): ProfileResult {
        val readResult = readFromDisk()
        if (readResult.isError) return readResult

        withProfile {
            // only keep feed
            this.identity = newProfile.identity
            this.authorization = newProfile.authorization
            this.providers = newProfile.providers
            this.feedSettings = newProfile.feedSettings
        }

        return writeToDisk()
    }

    private fun readFromString(wrapperJson: String): ProfileResult {
        currentWrapper = ProfileWrapper.existing(wrapperJson, password) ?: return ProfileResult.JsonError
        currentProfile = currentWrapper!!.unwrap() ?: return ProfileResult.PasswordMismatchError
        return ProfileResult.Success
    }

    private fun writeToString(): Pair<String?, ProfileResult> {
        if (!currentWrapper!!.rewrap(currentProfile!!)) return null to ProfileResult.PasswordMismatchError

        val wrapperJson: String?
        try {
            wrapperJson = jsonInstance.encodeToString(ProfileWrapper.serializer(), currentWrapper!!)
        } catch (_: Exception) {
            return null to ProfileResult.JsonError
        }

        return wrapperJson to ProfileResult.Success
    }

    companion object {
        private val PROFILE_FILENAME_RANGE = 10000..1000000

        fun fromCache(cachedProfile: CachedProfile, password: String): Pair<ProfileResult, ProfileManager?> {
            val manager = ProfileManager(cachedProfile.file, password)
            val readResult = manager.readFromDisk()

            return if (readResult.isError) {
                readResult to null
            } else {
                readResult to manager
            }
        }

        fun fromNew(profile: Profile, password: String): Pair<ProfileResult, ProfileManager?> {
            val relativePath = "${Random.nextInt(PROFILE_FILENAME_RANGE)}.$PROFILE_EXTENSION"
            val file = EnginePlatform.current.getPersistentPath(relativePath).asFile()

            val manager = ProfileManager(file, password)
            val createResult = manager.create(profile, ProfileEncryptionOptions())

            return if (createResult.isError) {
                createResult to null
            } else {
                ProfileCache.add(CachedProfile(relativePath, profile.identity.profileName))
                createResult to manager
            }
        }

        fun fromReconfigured(profile: Profile, password: String): Pair<ProfileResult, ProfileManager?> {
            val cachedProfile = ProfileCache.iterator().asSequence()
                .first { it.profileName == profile.identity.profileName }
            val manager = ProfileManager(cachedProfile.file, password)
            val mergeResult = manager.mergeWithProfile(profile)

            return if (mergeResult.isError) {
                mergeResult to null
            } else {
                mergeResult to manager
            }
        }
    }
}
