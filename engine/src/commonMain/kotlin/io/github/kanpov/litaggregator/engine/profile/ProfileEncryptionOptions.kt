package io.github.kanpov.litaggregator.engine.profile

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

@Serializable
data class ProfileEncryptionOptions(
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
