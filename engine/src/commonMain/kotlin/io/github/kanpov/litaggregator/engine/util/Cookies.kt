package io.github.kanpov.litaggregator.engine.util

import java.time.Instant

data class BasicCookie(
    val name: String,
    val value: String,
    val expiry: Instant?
)