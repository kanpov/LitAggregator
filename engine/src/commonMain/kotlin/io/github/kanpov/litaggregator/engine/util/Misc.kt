package io.github.kanpov.litaggregator.engine.util

fun String.padLeft(until: Int, with: Char): String {
    return if (this.length < until) {
        val diff = until - this.length
        buildString {
            for (x in 0..<until) {
                if (x < diff) {
                    append(with)
                } else {
                    append(this@padLeft[x - diff])
                }
            }
        }
    } else {
        this
    }
}
