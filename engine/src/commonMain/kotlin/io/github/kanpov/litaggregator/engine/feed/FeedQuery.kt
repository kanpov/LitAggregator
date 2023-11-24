package io.github.kanpov.litaggregator.engine.feed

data class FeedQuery(
    val sortOrder: FeedSortOrder = FeedSortOrder.Ascending,
    val sortParameter: FeedSortParameter = FeedSortParameter.None,
    val filterText: String = ""
)

enum class FeedSortOrder(val id: String) {
    Ascending("ascending_order"),
    Descending("descending_order");

    val opposite: FeedSortOrder
        get() = if (this == Ascending) Descending else Ascending
}

enum class FeedSortParameter(val element: (FeedEntry) -> Any, val id: String) {
    CreationTime(element = { it.metadata.creationTime?.epochSecond ?: 0L }, "creation_time"),
    IsStarred(element = { it.metadata.starred }, "is_starred"),
    IsPinned(element = { it.metadata.pinned }, "is_pinned"),
    IsMarked(element = { it.metadata.markers.isNotEmpty() }, "is_marked"),
    None(element = { 0 }, "none");

    companion object {
        fun fromId(id: String) = entries.first { it.id == id }
    }
}
