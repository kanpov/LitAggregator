package io.github.kanpov.litaggregator.engine.settings

import kotlinx.serialization.Serializable

private const val CLASS_AMOUNT_LIMIT = 15

const val MIN_VALID_PARALLEL = 5
const val MAX_VALID_PARALLEL = 11
const val MIN_VALID_GROUP = 1
const val MAX_VALID_GROUP = 6

@Serializable
data class IdentitySettings(
    var profileName: String = "",
    var parallel: Int = 0,
    var group: Int = 0
) {
    val studiesOnSaturdays: Boolean
        get() = parallel in MIN_VALID_PARALLEL..<MAX_VALID_PARALLEL

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
