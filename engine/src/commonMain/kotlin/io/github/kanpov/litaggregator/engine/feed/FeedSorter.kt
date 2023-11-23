package io.github.kanpov.litaggregator.engine.feed

data class FeedSorter(
    val order: FeedSortOrder,
    val parameter: FeedSortParameter
)

enum class FeedSortOrder {
    Ascending,
    Descending
}

enum class FeedSortParameter(val element: (FeedEntry) -> Any) {
    CreationTime(element = { it.metadata.creationTime?.epochSecond ?: 0L }),
    IsStarred(element = { it.metadata.starred }),
    IsPinned(element = { it.metadata.pinned }),
    IsMarked(element = { it.metadata.markers.isNotEmpty() })
}
