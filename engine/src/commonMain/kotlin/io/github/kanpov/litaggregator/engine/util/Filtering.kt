package io.github.kanpov.litaggregator.engine.util

import kotlinx.serialization.Serializable

@Serializable
enum class FilterPolicy {
    Disabled,
    ByInclusion,
    ByExclusion;
}

@Serializable
data class ListFilter(
    val policy: FilterPolicy = FilterPolicy.Disabled,
    val values: List<String> = emptyList()
) {
    fun match(otherValue: String): Boolean = when (policy) {
        FilterPolicy.Disabled -> true
        FilterPolicy.ByExclusion -> values.none { it.contains(otherValue) }
        FilterPolicy.ByInclusion -> values.any { it.contains(otherValue) }
    }
}

@Serializable
data class RegexFilter(
    val policy: FilterPolicy = FilterPolicy.Disabled,
    val regex: String? = null
) {
    fun match(otherValue: String): Boolean = when (policy) {
        FilterPolicy.Disabled -> true
        FilterPolicy.ByInclusion -> otherValue.matches(Regex(regex!!))
        FilterPolicy.ByExclusion -> !otherValue.matches(Regex(regex!!))
    }
}

@Serializable
data class ComparisonFilter(
    val policy: ComparisonFilterPolicy = ComparisonFilterPolicy.Disabled,
    val value: Int? = null
) {
    fun match(otherValue: Int): Boolean = when (policy) {
        ComparisonFilterPolicy.Disabled -> true
        ComparisonFilterPolicy.Above -> otherValue > value!!
        ComparisonFilterPolicy.Below -> otherValue < value!!
        ComparisonFilterPolicy.EqualsTo -> otherValue == value!!
    }
}

@Serializable
enum class ComparisonFilterPolicy {
    Disabled,
    Above,
    Below,
    EqualsTo
}
