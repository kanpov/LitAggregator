package io.github.kanpov.litaggregator.engine.util

fun buildUrl(builder: UrlBuildScope.() -> Unit): String {
    val scope = UrlBuildScope()
    scope.builder()
    return scope.compile()
}

class UrlBuildScope {
    private var url: String? = null
    private val parameters: MutableMap<String, String> = mutableMapOf()

    fun set(value: String) {
        url = value
    }

    fun parameter(key: String, value: String) {
        parameters[key] = value
    }

    fun compile() = buildString {
        append(url)

        if (parameters.isNotEmpty()) {
            append('?')
            val appliedParameters = mutableMapOf<String, String>()
            for ((key, value) in parameters) {
                append("$key=$value")
                appliedParameters[key] = value
                if (parameters.size != appliedParameters.size) {
                    append('&')
                }
            }
        }
    }
}
