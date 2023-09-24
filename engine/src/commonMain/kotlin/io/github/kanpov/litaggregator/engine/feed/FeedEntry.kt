package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.profile.bufferCharset
import kotlinx.serialization.Serializable
import java.security.MessageDigest

// Fingerprints: S (source), C (content) and M (metadata)
interface FeedEntry {
    val sourceFingerprint: String
    val metadata: FeedEntryMetadata
    val signParams: List<*>

    private val contentFingerprint: String // hashCode is inflexible as it cannot exclude the metadata, which is necessary in this case
        get() {
            val plainText = signParams.joinToString { it.toString() + ";" }
            return MessageDigest
                .getInstance("SHA-256")
                .digest(plainText.toByteArray(bufferCharset))
                .toString(bufferCharset)
        }

    private val metadataFingerprint: String
        get() = metadata.hashCode().toString()

    companion object {
        fun compare(first: FeedEntry, second: FeedEntry): FeedEntryComparisonResult {
            if (first.sourceFingerprint != second.sourceFingerprint) {
                return FeedEntryComparisonResult.Different
            }

            if (first.metadataFingerprint != second.metadataFingerprint) {
                return FeedEntryComparisonResult.NeedMerge
            }

            return if (first.contentFingerprint == second.contentFingerprint) {
                FeedEntryComparisonResult.Duplicates
            } else {
                FeedEntryComparisonResult.OldIsOutdated
            }
        }
    }
}

enum class FeedEntryComparisonResult {
    Duplicates, // C(A) = C(B); S(A) = S(B); M(A) = M(B)
    OldIsOutdated, // C(A) != C(B); S(A) = S(B); M(A) = M(B)
    NeedMerge, // S(A) = S(B); M(A) != M(B)
    Different // S(A) != S(B)
}

@Serializable
data class FeedEntryMetadata(
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
    val name: String,
    val thumbnailUrl: String?
)
