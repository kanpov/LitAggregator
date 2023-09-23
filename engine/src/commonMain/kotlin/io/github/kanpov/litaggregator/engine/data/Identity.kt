package io.github.kanpov.litaggregator.engine.data

import kotlinx.serialization.Serializable

@Serializable
data class Identity(
    val profileName: String,
    val parallel: Int,
    val group: Int
)
