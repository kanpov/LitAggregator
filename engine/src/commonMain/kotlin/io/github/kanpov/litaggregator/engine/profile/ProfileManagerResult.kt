package io.github.kanpov.litaggregator.engine.profile

enum class ProfileManagerResult {
    FileError, // failed to write/read local file
    JsonError, // failed to serialize/deserialize wrapper JSON
    PasswordMismatchError, // failed to encrypt/decrypt profile, likely due to a password mismatch
    NotFoundError, // no profile found
    MissingAuthorizationError, // not authorized to perform the action (for cloud, where Google auth is needed)
    OversizeProfileError, // trying to upload >5MB profile
    Success;

    fun asError(): ProfileManagerResult? {
        return if (this != Success) this else null
    }
}
