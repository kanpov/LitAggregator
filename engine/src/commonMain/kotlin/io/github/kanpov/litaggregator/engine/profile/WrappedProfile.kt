package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.asKotlinRandom

private const val ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding"
private const val KEY_GEN_ALGORITHM = "PBKDF2WithHmacSHA256"
val bufferCharset = Charsets.UTF_32 // string and byte[] conversions need a unified charset

@Serializable
data class WrappedProfile internal constructor(
    val encryption: EncryptionOptions,
    var data: String,
    @Transient var password: String = ""
) {
    @Transient private lateinit var iv: IvParameterSpec
    @Transient private var internalKey: SecretKey? = null
    private val key: SecretKey
        get() = internalKey ?: generateData().also { internalKey = it }

    private fun generateData(): SecretKey {
        iv = IvParameterSpec(encryption.iv.toByteArray())
        val keyFactory = SecretKeyFactory.getInstance(KEY_GEN_ALGORITHM)
        val keySpec = PBEKeySpec(
            password.toCharArray(),
            encryption.salt.toByteArray(),
            encryption.keyIterations,
            encryption.keyHasher.bitLength)
        return SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, "AES")
    }

    fun rewrap(profile: Profile) {
        val jsonData = jsonInstance.encodeToString(Profile.serializer(), profile)
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val binaryData = cipher.doFinal(jsonData.toByteArray(bufferCharset))
        data = Base64.getEncoder().encodeToString(binaryData)
    }

    fun unwrap(): Profile? {
        return try {
            val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            val jsonData = cipher.doFinal(Base64.getDecoder().decode(data)).toString(bufferCharset)
            jsonInstance.decodeFromString(jsonData)
        } catch (e: Exception) {
            println(e.stackTraceToString())
            null
        }
    }

    companion object {
        fun new(encryptionOptions: EncryptionOptions, profile: Profile, password: String): WrappedProfile {
            return WrappedProfile(encryptionOptions, data = "", password = password).apply { rewrap(profile) }
        }

        fun existing(json: String, password: String): WrappedProfile? {
            val wrappedProfile: WrappedProfile
            try {
                wrappedProfile = jsonInstance.decodeFromString<WrappedProfile>(json)
            } catch (_: Exception) {
                return null
            }

            return wrappedProfile.also { it.password = password }
        }
    }
}

@Serializable
data class EncryptionOptions(
    val iv: String = createRandomString(16),
    val salt: String = createRandomString(16),
    val keyHasher: SupportedHasher = SupportedHasher.SHA256,
    val keyIterations: Int = 65536
) {
    companion object {
        private val secureRandom = SecureRandom.getInstanceStrong().asKotlinRandom()
        private val alphanumeric = ('A'..'Z') + ('a'..'z') + ('0'..'9')

        fun createRandomString(length: Int): String {
            return buildString(length) {
                repeat(length) { append(alphanumeric.random(secureRandom)) }
            }
        }
    }
}

@Serializable
enum class SupportedHasher(@Transient val bitLength: Int) {
    SHA128(128),
    SHA256(256),
    SHA512(512)
}
