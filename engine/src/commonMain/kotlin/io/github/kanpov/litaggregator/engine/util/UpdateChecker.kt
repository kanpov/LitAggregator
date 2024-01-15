package io.github.kanpov.litaggregator.engine.util

import io.github.kanpov.litaggregator.engine.util.io.decodeJsonRootList
import io.github.kanpov.litaggregator.engine.util.io.jString
import io.github.kanpov.litaggregator.engine.util.io.ktorClient
import io.github.z4kn4fein.semver.toVersion
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject

const val APP_VERSION = "1.2.0"
const val APP_REPOSITORY = "kanpov/LitAggregator"

suspend fun checkForUpdates(): AppUpdate? {
    val releases = decodeJsonRootList<JsonObject>(
        ktorClient.get("https://api.github.com/repos/$APP_REPOSITORY/releases").bodyAsText()
    )
    var latestVersion = APP_VERSION.toVersion()
    var latestVersionUrl: String? = null

    for (release in releases) {
        val proposedVersion = release.jString("tag_name").toVersion()

        if (proposedVersion > latestVersion) {
            latestVersion = proposedVersion
            latestVersionUrl = release.jString("html_url")
        }
    }

    return if (latestVersionUrl != null) AppUpdate(latestVersionUrl, latestVersion.toString()) else null
}

data class AppUpdate(
    val url: String,
    val version: String
)
