package io.github.kanpov.litaggregator.engine.profile

import io.github.kanpov.litaggregator.engine.util.io.jsonInstance
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding"
private const val KEY_GEN_ALGORITHM = "PBKDF2WithHmacSHA256"
val bufferCharset = Charsets.UTF_32 // string and byte[] conversions need a unified charset

@Serializable
data class ProfileWrapper internal constructor(
    val encryption: ProfileEncryptionOptions,
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

    fun rewrap(profile: Profile): Boolean {
        return try {
            val jsonData = jsonInstance.encodeToString(Profile.serializer(), profile)
            val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, iv)
            val binaryData = cipher.doFinal(jsonData.toByteArray(bufferCharset))
            data = Base64.getEncoder().encodeToString(binaryData)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun unwrap(): Profile? {
        return try {
            val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            val jsonData = cipher.doFinal(Base64.getDecoder().decode(data)).toString(bufferCharset)
            jsonInstance.decodeFromString(jsonData)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun new(encryptionOptions: ProfileEncryptionOptions, profile: Profile, password: String): ProfileWrapper? {
            return try {
                ProfileWrapper(encryptionOptions, data = "", password = password).apply { rewrap(profile) }
            } catch (_: Exception) {
                null
            }
        }

        fun existing(json: String, password: String): ProfileWrapper? {
            val profileWrapper: ProfileWrapper
            try {
                profileWrapper = jsonInstance.decodeFromString<ProfileWrapper>(json)
            } catch (_: Exception) {
                return null
            }

            return profileWrapper.also { it.password = password }
        }
    }
}
