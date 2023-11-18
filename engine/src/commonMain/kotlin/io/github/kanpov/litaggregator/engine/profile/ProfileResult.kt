package io.github.kanpov.litaggregator.engine.profile

enum class ProfileResult {
    FileError, // failed to write/read local file
    JsonError, // failed to serialize/deserialize wrapper JSON
    PasswordMismatchError, // failed to encrypt/decrypt profile, likely due to a password mismatch
    NotFoundError, // no profile found
    MissingAuthorizationError, // not authorized to perform the action (for cloud, where Google auth is needed)
    OversizeProfileError, // trying to upload >5MB profile
    Success;

    val isError: Boolean
        get() = this != Success
}
