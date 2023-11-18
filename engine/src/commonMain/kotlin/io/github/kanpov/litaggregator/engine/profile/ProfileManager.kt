package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.io.asFile
import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import io.github.kanpov.litaggregator.engine.util.io.readFile
import io.github.kanpov.litaggregator.engine.util.io.writeFile
import java.io.File
import java.time.Instant

private const val PROFILE_EXTENSION = "agr"

class ProfileManager private constructor(private val profileFile: File, private val password: String) {
    private var currentProfile: Profile? = null
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

        // TODO: remove in prod
        val snapshotFile = EnginePlatform.current.getCachePath("snapshot_${Instant.now().epochSecond}.json")
        writeFile(snapshotFile, jsonInstance.encodeToString(Profile.serializer(), currentProfile!!))

        return ProfileResult.Success
    }

    fun readFromDisk(): ProfileResult {
        if (!profileFile.exists()) return ProfileResult.NotFoundError
        return readFromString(readFile(profileFile) ?: return ProfileResult.FileError)
    }

    suspend fun withProfile(scope: suspend Profile.() -> Unit) {
        currentProfile?.scope()
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
        fun fromCache(cachedProfile: CachedProfile, password: String): Pair<ProfileResult, ProfileManager?> {
            val file = EnginePlatform.current.getPersistentPath(cachedProfile.relativePath).asFile()
            val manager = ProfileManager(file, password)
            val readResult = manager.readFromDisk()

            return if (readResult.isError) {
                readResult to null
            } else {
                readResult to manager
            }
        }

        fun fromNew(profile: Profile, options: ProfileEncryptionOptions,
                    profileName: String, password: String): Pair<ProfileResult, ProfileManager?> {
            val relativePath = "$profileName.$PROFILE_EXTENSION"
            val file = EnginePlatform.current.getPersistentPath(relativePath).asFile()

            val manage=
        }

//        fun locateCachedProfiles(): List<File> {
//            val cacheFile = EnginePlatform.current.getPersistentPath(PROFILE_CACHE_RELATIVE_PATH).asFile()
//            if (!cacheFile.exists()) return emptyList() // no cache
//
//            val entries = readFile(cacheFile)?.lines() ?: return emptyList()
//            return buildList {
//                for (entry in entries) {
//                    if (entry.isBlank()) continue
//
//                    val entryFile = EnginePlatform.current.getPersistentPath(entry).asFile()
//                    if (entryFile.exists()) {
//                        this += entryFile
//                    }
//                }
//            }
//        }
//
//        fun fromCachedProfile(cachedProfileFile: File, password: String) = ProfileManager(cachedProfileFile, password)
//            .apply { readFromDisk() }
//
//        fun fromNewProfile(profile: Profile, options: ProfileEncryptionOptions, profileName: String, password: String): ProfileManager {
//            val profileRelativePath = "$profileName.agr"
//            val cacheFile = EnginePlatform.current.getPersistentPath(PROFILE_CACHE_RELATIVE_PATH).asFile()
//            writeFile(cacheFile, profileRelativePath)
//
//            val profileFile = EnginePlatform.current.getPersistentPath(profileRelativePath).asFile()
//            profileFile.createNewFile()
//
//            return ProfileManager(profileFile, password).apply {
//                this.create(profile, options)
//            }
//        }
    }
}
