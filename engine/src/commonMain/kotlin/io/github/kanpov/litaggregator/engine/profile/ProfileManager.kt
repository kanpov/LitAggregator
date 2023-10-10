package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.io.asFile
import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import io.github.kanpov.litaggregator.engine.util.io.readFile
import io.github.kanpov.litaggregator.engine.util.io.writeFile

class ProfileManager(private val profileName: String, private val password: String) {
    private val profileFile = EnginePlatform.current
        .getPersistentPath("$profileName.json")
        .asFile()
    private var currentProfile: Profile? = null
    private var currentWrapper: ProfileWrapper? = null

    fun create(profile: Profile, options: ProfileEncryptionOptions = ProfileEncryptionOptions()): ProfileActionResult {
        currentProfile = profile
        currentWrapper = ProfileWrapper.new(options, profile, password) ?: return ProfileActionResult.EncryptionError
        return writeToDisk()
    }

    fun writeToDisk(): ProfileActionResult {
        if (!currentWrapper!!.rewrap(currentProfile!!)) return ProfileActionResult.EncryptionError

        val wrapperJson: String?
        try {
            wrapperJson = jsonInstance.encodeToString(ProfileWrapper.serializer(), currentWrapper!!)
        } catch (_: Exception) {
            return ProfileActionResult.JsonError
        }

        if (!writeFile(profileFile, wrapperJson)) return ProfileActionResult.FileError

        return ProfileActionResult.Success
    }

    fun readFromDisk(): ProfileActionResult {
        if (!profileFile.exists()) return ProfileActionResult.NotFound

        val wrapperJson = readFile(profileFile) ?: return ProfileActionResult.FileError
        currentWrapper = ProfileWrapper.existing(wrapperJson, password) ?: return ProfileActionResult.JsonError
        currentProfile = currentWrapper!!.unwrap() ?: return ProfileActionResult.EncryptionError

        return ProfileActionResult.Success
    }
}
