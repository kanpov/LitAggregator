package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.util.JsonInstant
import io.github.kanpov.litaggregator.engine.util.JsonUuid
import kotlinx.serialization.Serializable
import java.util.*

interface FeedEntry {
    val metadata: FeedEntryMetadata
}

@Serializable
data class FeedEntryMetadata(
    val uuid: JsonUuid = UUID.randomUUID(),
    val creationTime: JsonInstant,
    val updateTime: JsonInstant? = null,
    val comments: List<String> = emptyList(),
    val markers: List<String> = emptyList(),
    val attachments: List<FeedEntryAttachment> = emptyList(),
    val taskLists: List<FeedEntryTaskList> = emptyList(),
    val starred: Boolean = false,
    val pinned: Boolean = false
)

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
