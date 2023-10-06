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
    var comments: MutableSet<String> = mutableSetOf(),
    var markers: MutableSet<String> = mutableSetOf(),
    var attachments: MutableSet<FeedEntryAttachment> = mutableSetOf(),
    var taskLists: MutableSet<FeedEntryTaskList> = mutableSetOf(),
    var starred: Boolean = false,
    var pinned: Boolean = false
) {
    fun merge(other: FeedEntryMetadata) {
        // Note that the merge operation is conservative, meaning that it'll keep its own value when confronted with
        // no option other than accepting the other instance's value
        other.comments.forEach { this.comments += it }
        other.markers.forEach { this.markers += it }
        other.attachments.forEach { attachment ->
            val matchingAttachment = this.attachments.firstOrNull { it.thumbnailUrl == attachment.thumbnailUrl && it.downloadUrl == attachment.downloadUrl }
            if (matchingAttachment == null) {
                this.attachments += attachment
            }
        }
        other.taskLists.forEach { taskList ->
            val matchingTaskList = this.taskLists.firstOrNull { it.name == taskList.name }

            if (matchingTaskList == null) {
                this.taskLists += taskList
            } else {
                this.taskLists -= matchingTaskList
                this.taskLists += FeedEntryTaskList(
                    matchingTaskList.name,
                    matchingTaskList.tasks.toMutableMap().also { it.putAll(taskList.tasks) },
                    matchingTaskList.definesOverallCompletion)
            }
        }
    }

    override fun hashCode(): Int {
        // don't include creation time
        var result = comments.hashCode()
        result = 31 * result + markers.hashCode()
        result = 31 * result + attachments.hashCode()
        result = 31 * result + taskLists.hashCode()
        result = 31 * result + starred.hashCode()
        result = 31 * result + pinned.hashCode()
        return result
    }
}

@Serializable
data class FeedEntryTaskList(
    val name: String,
    val tasks: Map<String, Boolean>,
    val definesOverallCompletion: Boolean
)

@Serializable
data class FeedEntryAttachment(
    val downloadUrl: String,
    val title: String?,
    val thumbnailUrl: String?
)
