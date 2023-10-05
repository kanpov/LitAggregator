package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

private const val CLASS_AMOUNT_LIMIT = 15

@Serializable
data class IdentitySettings(
    val profileName: String,
    val parallel: Int,
    val group: Int
) {
    val studiesOnSaturdays: Boolean
        get() = parallel in 7..10

    val classNames: List<String>
        get() = listOf("$parallel.$group", "$parallel-$group")

    val otherClassNames: List<String>
        get() = buildList {
            for (i in 1..CLASS_AMOUNT_LIMIT) {
                if (i != group) {
                    this += "$parallel.$i"
                    this += "$parallel-$i"
                }
            }
        }
}
