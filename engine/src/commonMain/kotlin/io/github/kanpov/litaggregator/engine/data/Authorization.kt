package io.github.kanpov.litaggregator.engine.data

import io.github.kanpov.litaggregator.engine.EngineRuntime
import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.GoogleClientSession
import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.UlyssAuthorizer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Authorization(
    val ulyss: UlyssAuthorizer? = null,
    val mos: MosAuthorizer? = null,
    @SerialName("google") val googleSession: GoogleClientSession? = null
) {
    @Transient private var backer: GoogleAuthorizer? = null
    val google: GoogleAuthorizer
        get() = backer ?: EngineRuntime.current.googleAuthorizerFactory(googleSession!!).also { backer = it }
}
