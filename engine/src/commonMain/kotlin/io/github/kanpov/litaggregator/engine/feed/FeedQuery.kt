package io.github.kanpov.litaggregator.engine.feed

import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import java.time.Instant

data class FeedQuery(
    val sortOrder: FeedSortOrder = FeedSortOrder.Descending,
    val sortParameter: FeedSortParameter = FeedSortParameter.Relevancy,
    val filterText: String = "",
    val filterPools: Set<String> = Feed.poolIds
)

enum class FeedSortOrder(val id: String) {
    Ascending("ascending_order"),
    Descending("descending_order");

    val opposite: FeedSortOrder
        get() = if (this == Ascending) Descending else Ascending
}

enum class FeedSortParameter(val element: (FeedEntry) -> Any, val id: String) {
    Relevancy(element = {
        if (it is HomeworkFeedEntry && it.assignedTime != null) {
            it.assignedTime.epochSecond - Instant.MIN.epochSecond
        } else {
            it.metadata.creationTime?.epochSecond?.minus(Instant.MIN.epochSecond) ?: Instant.MIN.epochSecond
        }
   }, "relevancy"),
    IsStarred(element = { it.metadata.starred }, "is_starred"),
    None(element = { 0 }, "none");

    companion object {
        fun fromId(id: String) = entries.first { it.id == id }
    }
}
