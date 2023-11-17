package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.io.asFile
import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import io.github.kanpov.litaggregator.engine.util.io.readFile
import io.github.kanpov.litaggregator.engine.util.io.writeFile
import java.io.File
import java.time.Instant

private const val PROFILE_CACHE_RELATIVE_PATH = "profile_cache.txt"

class ProfileManager private constructor(private val profileFile: File, private val password: String) {
    private var currentProfile: Profile? = null
    private var currentWrapper: ProfileWrapper? = null

    fun create(profile: Profile, options: ProfileEncryptionOptions = ProfileEncryptionOptions()): ProfileManagerResult {
        currentProfile = profile
        currentWrapper = ProfileWrapper.new(options, profile, password) ?: return ProfileManagerResult.PasswordMismatchError
        return writeToDisk()
    }

    fun writeToDisk(): ProfileManagerResult {
        val (wrapperJson, result) = writeToString()

        if (wrapperJson == null) return result
        if (!writeFile(profileFile, wrapperJson)) return ProfileManagerResult.FileError

        // TODO: remove in prod
        val snapshotFile = EnginePlatform.current.getCachePath("snapshot_${Instant.now().epochSecond}.json")
        writeFile(snapshotFile, jsonInstance.encodeToString(Profile.serializer(), currentProfile!!))

        return ProfileManagerResult.Success
    }

    fun readFromDisk(): ProfileManagerResult {
        if (!profileFile.exists()) return ProfileManagerResult.NotFoundError
        return readFromString(readFile(profileFile) ?: return ProfileManagerResult.FileError)
    }

    suspend fun withProfile(scope: suspend Profile.() -> Unit) {
        currentProfile?.scope()
    }

    private fun readFromString(wrapperJson: String): ProfileManagerResult {
        currentWrapper = ProfileWrapper.existing(wrapperJson, password) ?: return ProfileManagerResult.JsonError
        currentProfile = currentWrapper!!.unwrap() ?: return ProfileManagerResult.PasswordMismatchError
        return ProfileManagerResult.Success
    }

    private fun writeToString(): Pair<String?, ProfileManagerResult> {
        if (!currentWrapper!!.rewrap(currentProfile!!)) return null to ProfileManagerResult.PasswordMismatchError

        val wrapperJson: String?
        try {
            wrapperJson = jsonInstance.encodeToString(ProfileWrapper.serializer(), currentWrapper!!)
        } catch (_: Exception) {
            return null to ProfileManagerResult.JsonError
        }

        return wrapperJson to ProfileManagerResult.Success
    }

    companion object {
        // working with profile cache (e.g. saving the currently selected profile over multiple app launches)

        fun locateCachedProfiles(): List<File> {
            val cacheFile = EnginePlatform.current.getPersistentPath(PROFILE_CACHE_RELATIVE_PATH).asFile()
            if (!cacheFile.exists()) return emptyList() // no cache

            val entries = readFile(cacheFile)?.lines() ?: return emptyList()
            return buildList {
                for (entry in entries) {
                    if (entry.isBlank()) continue

                    val entryFile = EnginePlatform.current.getPersistentPath(entry).asFile()
                    if (entryFile.exists()) {
                        this += entryFile
                    }
                }
            }
        }

        fun fromCachedProfile(cachedProfileFile: File, password: String) = ProfileManager(cachedProfileFile, password)
            .apply { readFromDisk() }

        fun fromNewProfile(profile: Profile, options: ProfileEncryptionOptions, profileName: String, password: String): ProfileManager {
            val profileRelativePath = "$profileName.agr"
            val cacheFile = EnginePlatform.current.getPersistentPath(PROFILE_CACHE_RELATIVE_PATH).asFile()
            writeFile(cacheFile, profileRelativePath)

            val profileFile = EnginePlatform.current.getPersistentPath(profileRelativePath).asFile()
            profileFile.createNewFile()

            return ProfileManager(profileFile, password).apply {
                this.create(profile, options)
            }
        }
    }
}
