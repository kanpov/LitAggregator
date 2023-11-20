package io.github.kanpov.litaggregator.engine.authorizer

import io.github.kanpov.litaggregator.engine.EnginePlatform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AuthorizationState(
    var ulyss: UlyssesAuthorizer? = null,
    var mos: MeshAuthorizer? = null,
    @SerialName("google") var googleSession: GoogleSession? = null
) {
    @Transient private var backer: GoogleAuthorizer? = null
    var google: GoogleAuthorizer
        get() = backer ?: EnginePlatform.current.googleAuthorizerFactory(googleSession!!).also { backer = it }
        set(value) {
            backer = value
            googleSession = value.session
        }
}
