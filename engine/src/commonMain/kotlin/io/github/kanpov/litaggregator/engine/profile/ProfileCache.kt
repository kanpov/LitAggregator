package io.github.kanpov.litaggregator.engine.profile

import co.touchlab.kermit.Logger
import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.util.io.asFile
import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import io.github.kanpov.litaggregator.engine.util.io.readFile
import io.github.kanpov.litaggregator.engine.util.io.writeFile
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class CachedProfile(
    val relativePath: String,
    val profileName: String,
    var starred: Boolean
) {
    val file: File by lazy { EnginePlatform.current.getPersistentPath(relativePath).asFile() }
}

@Serializable
private data class ProfileCacheWrapper(
    val profiles: MutableList<CachedProfile>
)

private const val PROFILE_CACHE_PATH = "profile_cache.json"

object ProfileCache {
    private val cacheFile: File by lazy { EnginePlatform.current.getPersistentPath(PROFILE_CACHE_PATH).asFile() }
    private var wrapper = ProfileCacheWrapper(mutableListOf())

    operator fun iterator() = wrapper.profiles.iterator()

    fun isNotEmpty() = wrapper.profiles.isNotEmpty()

    fun write(): Boolean {
        val wrapperJson = jsonInstance.encodeToString(ProfileCacheWrapper.serializer(), wrapper)
        if (!writeFile(cacheFile, wrapperJson)) {
            return handleIOError(verb = "write", reason = "denied I/O permissions")
        }
        return true
    }

    fun read(): Boolean {
        val wrapperJson = readFile(cacheFile) ?: return handleIOError(verb = "read", reason = "malformed file")

        try {
            wrapper = jsonInstance.decodeFromString(ProfileCacheWrapper.serializer(), wrapperJson)
        } catch (_: Exception) {
            return handleIOError(verb = "read", reason = "malformed JSON")
        }

        return true
    }

    fun remove(cachedProfile: CachedProfile) {
        wrapper.profiles.remove(cachedProfile)
        write()
    }

    fun add(profile: Profile, relativePath: String) {
        wrapper.profiles.add(CachedProfile(
            relativePath = relativePath,
            profileName = profile.identity.profileName,
            starred = false
        ))
        write()
    }

    fun exists(): Boolean = cacheFile.exists()

    private fun handleIOError(verb: String, reason: String): Boolean {
        Logger.e { "Failed to $verb profile cache because of $reason. Aborting operation and emptying file" }
        cacheFile.delete()
        cacheFile.createNewFile()
        return false
    }
}
