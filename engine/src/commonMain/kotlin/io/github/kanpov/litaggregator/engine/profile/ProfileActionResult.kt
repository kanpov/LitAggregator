package io.github.kanpov.litaggregator.engine.profile

enum class ProfileActionResult {
    FileError,
    JsonError,
    EncryptionError,
    NotFound,
    Success;

    fun asError(): ProfileActionResult? {
        return if (this != Success) this else null
    }
}
