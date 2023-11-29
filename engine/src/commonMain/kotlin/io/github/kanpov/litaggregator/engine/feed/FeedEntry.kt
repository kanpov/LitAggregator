package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.util.io.JsonInstant
import kotlinx.serialization.Serializable

// Fingerprints: S (source), C (content) and M (metadata)
interface FeedEntry {
    val metadata: FeedEntryMetadata
    val contentParams: List<*>

    // Identification
    val sourceFingerprint: String
    val contentFingerprint: String // hashCode is inflexible as it cannot exclude the metadata, which is necessary in this case
        get() = fingerprintFrom(contentParams)
    val metadataFingerprint: String
        get() = metadata.hashCode().toString()

    companion object {
        fun fingerprintFrom(vararg params: Any): String {
            return params.joinToString(separator = ";") { it.toString() }
        }
    }
}

@Serializable
data class FeedEntryMetadata(
    val creationTime: JsonInstant?,
    var comment: String = "",
    var starred: Boolean = false,
    var seenBefore: Boolean = false,
    val sourceName: String
) {
    override fun hashCode(): Int {
        // don't include creation time
        var result = comment.hashCode()
        result = 31 * result + starred.hashCode()
        result = 31 * result + seenBefore.hashCode()
        result = 31 * result + sourceName.hashCode()
        return result
    }

    fun merge(other: FeedEntryMetadata) {
        if (comment.trim() != other.comment.trim()) {
            comment += "\n" + other.comment
        }
    }
}

@Serializable
data class FeedEntryAttachment(
    val downloadUrl: String,
    val title: String?,
    val thumbnailUrl: String?
)

@Serializable
data class FeedEntryTask(
    val name: String,
    val completed: Boolean
)
