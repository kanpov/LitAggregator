package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

@Serializable
data class IdentitySettings(
    val profileName: String,
    val parallel: Int,
    val group: Int
) {
    val studiesOnSaturdays: Boolean
        get() = parallel in 7..10
}
