package io.github.kanpov.litaggregator.engine
//
//import co.touchlab.kermit.Logger
//import io.github.kanpov.litaggregator.engine.authorizer.Authorizer
//import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
//import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
//import io.github.kanpov.litaggregator.engine.authorizer.UlyssAuthorizer
//import io.github.kanpov.litaggregator.engine.feed.Feed
//import io.github.kanpov.litaggregator.engine.feed.sortedByRelevancy
//import io.github.kanpov.litaggregator.engine.profile.EncryptionOptions
//import io.github.kanpov.litaggregator.engine.profile.Profile
//import io.github.kanpov.litaggregator.engine.profile.ProfileWrapper
//import io.github.kanpov.litaggregator.engine.provider.AuthorizedProviderDefinition
//import io.github.kanpov.litaggregator.engine.provider.SimpleProviderDefinition
//import io.github.kanpov.litaggregator.engine.util.io.asFile
//import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
//import io.github.kanpov.litaggregator.engine.util.io.readFile
//import io.github.kanpov.litaggregator.engine.util.io.writeFile
//import java.io.File
//import java.time.Instant
//
//class Engine(platform: EnginePlatform, profileName: String) {
//    private val profileFile: File
//    private lateinit var profileWrapper: ProfileWrapper
//    private lateinit var profile: Profile
//
//    init {
//        Logger.i { "Running the engine on platform: ${platform.name}" }
//
//        EnginePlatform.current = platform
//
//        profileFile = platform.getPersistentPath("$profileName.json").asFile()
//    }
//
//    fun createProfile(encryptionOptions: EncryptionOptions, profile: Profile, password: String) {
//        profileWrapper = ProfileWrapper.new(encryptionOptions, profile, password)
//        this.profile = profile
//        writeFile(profileFile, jsonInstance.encodeToString(ProfileWrapper.serializer(), this.profileWrapper))
//        Logger.i { "Created new profile: ${profileFile.absolutePath}" }
//    }
//
//    fun loadProfile(password: String): ProfileLoadResult {
//        if (!profileFile.exists()) {
//            Logger.i { "Attempt to load profile was unsuccessful due to it not existing: ${profileFile.absolutePath}" }
//            return ProfileLoadResult.NoSuchProfile
//        }
//
//        profileWrapper = ProfileWrapper.existing(readFile(profileFile), password) ?: return ProfileLoadResult.CorruptedProfile.also {
//            Logger.i { "Attempt to load profile was unsuccessful due to it being corrupted: ${profileFile.absolutePath}" }
//        }
//
//        profile = profileWrapper.unwrap() ?: return ProfileLoadResult.WrongPassword.also {
//            Logger.i { "Attempt to load profile was unsuccessful due to an incorrect password: ${profileFile.absolutePath}" }
//        }
//
//        Logger.i { "Loaded profile: ${profileFile.absolutePath}" }
//        return ProfileLoadResult.Successful
//    }
//
//    fun saveProfile() {
//        profileWrapper.rewrap(profile)
//        val profileWrapperJson = jsonInstance.encodeToString(ProfileWrapper.serializer(), profileWrapper)
//        val snapshotJson = jsonInstance.encodeToString(Profile.serializer(), profile)
//        writeFile(profileFile, profileWrapperJson)
//        Logger.i { "Saved profile: ${profileFile.absolutePath}" }
//
//        // debug snapshots
//        val snapshotFile = EnginePlatform.current
//            .getPersistentPath("snapshot_${Instant.now().toEpochMilli()}.json")
//            .asFile()
//        writeFile(snapshotFile, snapshotJson)
//    }
//
//    suspend fun setupAuthorizer(authorizer: Authorizer): Boolean {
//        if (!authorizer.authorize()) return false
//        Logger.i { "New authorizer has been set up: ${authorizer.name}" }
//
//        when (authorizer) {
//            is UlyssAuthorizer -> profile.authorization.ulyss = authorizer
//            is MosAuthorizer -> profile.authorization.mos = authorizer
//            is GoogleAuthorizer -> profile.authorization.google = authorizer
//        }
//
//        return true
//    }
//
//    fun withFeed(scope: Feed.() -> Unit) {
//        profile.feed.scope()
//        shrinkFeed()
//    }
//
//    suspend fun refreshFeed(): Pair<Feed, Set<String> /* providers that failed */> {
//        val errors = mutableSetOf<String>()
//        var runProviders = 0
//        Logger.i { "Feed refresh has been started" }
//
//        SimpleProviderDefinition.all.forEach { definition ->
//            if (definition.isEnabled(profile.providers)) {
//                if (!definition.factory(profile).run(profile)) {
//                    Logger.i { "Simple provider ${definition.name} has failed" }
//                    errors += definition.name
//                } else {
//                    Logger.i { "Simple provider ${definition.name} was successful" }
//                }
//                runProviders++
//            }
//        }
//
//        AuthorizedProviderDefinition.all.forEach { definition ->
//            if (definition.isEnabled(profile.providers) && definition.isAuthorized(profile.authorization)) {
//                if (!definition.factory(profile).run(profile)) {
//                    Logger.i { "Authorized provider ${definition.name} has failed" }
//                    errors += definition.name
//                } else {
//                    Logger.i { "Authorized provider ${definition.name} was successful" }
//                }
//                runProviders++
//            }
//        }
//
//        if (errors.isEmpty()) {
//            Logger.i { "Feed refresh has completed without any errors in $runProviders configured provider(s)" }
//        } else {
//            Logger.i { "Feed refresh has completed with errors in the following of $runProviders configured provider(s): ${errors.joinToString()}" }
//        }
//
//        return profile.feed to errors
//    }
//
//    private fun shrinkFeed() {
//        for ((poolName, pool) in profile.feed.allPools) {
//            val entries = pool.sortedByRelevancy()
//
//            if (entries.size <= profile.feedSettings.maxPoolSize) continue
//
//            val diff = entries.size - profile.feedSettings.maxPoolSize
//            for (i in profile.feedSettings.maxPoolSize..<profile.feedSettings.maxPoolSize + diff) {
//                pool.remove(entries[i])
//            }
//
//            Logger.i { "Shrunk pool of $poolName by $diff entries" }
//        }
//    }
//}
//
//enum class ProfileLoadResult {
//    Successful,
//    NoSuchProfile,
//    CorruptedProfile,
//    WrongPassword
//}
