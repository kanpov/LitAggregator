package io.github.kanpov.litaggregator.engine.settings

import io.github.kanpov.litaggregator.engine.EnginePlatform
import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.GoogleClientSession
import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.UlyssAuthorizer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Authorization(
    var ulyss: UlyssAuthorizer? = null,
    var mos: MosAuthorizer? = null,
    @SerialName("google") var googleSession: GoogleClientSession? = null
) {
    @Transient private var backer: GoogleAuthorizer? = null
    var google: GoogleAuthorizer
        get() = backer ?: EnginePlatform.current.googleAuthorizerFactory(googleSession!!).also { backer = it }
        set(value) {
            backer = value
            googleSession = value.session
        }
}
