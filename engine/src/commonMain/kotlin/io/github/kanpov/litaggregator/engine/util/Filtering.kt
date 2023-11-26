package io.github.kanpov.litaggregator.engine.util

import kotlinx.serialization.Serializable

@Serializable
enum class FilterPolicy(val encodedForm: Char) {
    Disabled('~'),
    ByInclusion('+'),
    ByExclusion('-');
}

@Serializable
enum class ComparisonFilterPolicy(val encodedForm: Char) {
    Disabled('x'),
    Above('>'),
    Below('<'),
    EqualsTo('=');
}

interface BasicSerializer<T> {
    fun encode(value: T): String
    fun decode(encoded: String): T?
}

@Serializable
data class ListFilter(
    val policy: FilterPolicy = FilterPolicy.Disabled,
    val values: List<String> = emptyList()
) {
    fun match(otherValue: String): Boolean = when (policy) {
        FilterPolicy.Disabled -> true
        FilterPolicy.ByExclusion -> values.none { it.lowercase().contains(otherValue.lowercase()) }
        FilterPolicy.ByInclusion -> values.any { it.lowercase().contains(otherValue.lowercase()) }
    }

    fun matchList(otherValues: List<String>) = policy == FilterPolicy.Disabled || otherValues.any { match(it) }

    companion object : BasicSerializer<ListFilter> {
        override fun encode(value: ListFilter): String {
            return if (value.policy == FilterPolicy.Disabled || value.values.isEmpty()) {
                FilterPolicy.Disabled.encodedForm.toString()
            } else {
                value.policy.encodedForm + value.values.joinToString(separator = ",")
            }
        }

        override fun decode(encoded: String): ListFilter? {
            if (encoded.isBlank()) return null
            val policy = FilterPolicy.entries.firstOrNull { it.encodedForm == encoded.trim().first() } ?: return null
            if (policy == FilterPolicy.Disabled) return ListFilter()
            val values = encoded.trim().removePrefix(policy.encodedForm.toString())
                .split(',').filter { it.isNotBlank() }
            return if (values.isEmpty()) null else ListFilter(policy, values)
        }
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

    companion object : BasicSerializer<RegexFilter> {
        override fun encode(value: RegexFilter): String {
            return if (value.policy == FilterPolicy.Disabled || value.regex == null) {
                FilterPolicy.Disabled.encodedForm.toString()
            } else {
                value.regex
            }
        }

        override fun decode(encoded: String): RegexFilter? {
            if (encoded.isBlank()) return null
            val policy = FilterPolicy.entries.firstOrNull { it.encodedForm == encoded.trim().first() } ?: return null
            return if (policy == FilterPolicy.Disabled) {
                RegexFilter()
            } else {
                RegexFilter(policy, encoded.trim().removePrefix(encoded.trim().first().toString()))
            }
        }
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

    companion object : BasicSerializer<ComparisonFilter> {
        override fun encode(value: ComparisonFilter): String {
            if (value.policy == ComparisonFilterPolicy.Disabled) return "-"
            return "${value.policy.encodedForm}${value.value}"
        }

        override fun decode(encoded: String): ComparisonFilter? {
            if (encoded.isBlank()) return null
            if (encoded.trim().startsWith(ComparisonFilterPolicy.Disabled.toString())) return ComparisonFilter()
            val policy = ComparisonFilterPolicy.entries.firstOrNull { it.encodedForm == encoded.trim().first() } ?: return null
            val value = encoded.trim().removePrefix(policy.encodedForm.toString()).toIntOrNull() ?: return null
            return ComparisonFilter(policy, value)
        }
    }
}
