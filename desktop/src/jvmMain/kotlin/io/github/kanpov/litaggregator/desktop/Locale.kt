package io.github.kanpov.litaggregator.desktop

import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

data class LocaleRegister(
    val id: String,
    val relativePath: String,
    val name: String
)

private const val LOCALE_REGISTRY_PATH = "locale/locales.txt"
private const val LOCALE_REGISTER_SEPARATOR = ';'
private const val LOCALE_COMMENT_SYMBOL = '#'
private const val LOCALE_EQUALITY_SYMBOL = '='
private const val LOCALE_ERROR_PLACEHOLDER = "!ERROR!"

object Locale {
    operator fun get(key: String, vararg fills: Any): String {
        if (localeValues[key] == null) {
            Logger.e { "Tried to access non-existent locale key: \"$key\". Default to: \"$LOCALE_ERROR_PLACEHOLDER\"" }
            return LOCALE_ERROR_PLACEHOLDER
        }

        var output = localeValues[key]!!
        fills.forEachIndexed { index, fill ->
            output = output.replace("|${index + 1}|", fill.toString())
        }

        return output
    }

    private lateinit var localeValues: Map<String, String>

    @OptIn(ExperimentalResourceApi::class)
    private val localeRegisters: Set<LocaleRegister> by lazy {
        buildSet {
            val registersData: String
            runBlocking {
                registersData = resource(LOCALE_REGISTRY_PATH).readBytes().decodeToString()
            }

            for (registerStr in registersData.lines()) {
                val parts = registerStr.split(LOCALE_REGISTER_SEPARATOR)
                if (parts.size != 3) continue
                this += LocaleRegister(parts[0], parts[1], parts[2])
            }
        }
    }

    val localeNames: List<String>
        get() = localeRegisters.map { it.name }

    @OptIn(ExperimentalResourceApi::class)
    private fun loadFromRegister(register: LocaleRegister) {
        val localeData: String
        runBlocking {
            localeData = resource("locale/${register.relativePath}").readBytes().decodeToString()
        }

        localeValues = buildMap {
            for (localeStr in localeData.lines()) {
                if (localeStr.isBlank() || localeStr.trim().startsWith(LOCALE_COMMENT_SYMBOL)) continue

                val strParts = localeStr.split(LOCALE_EQUALITY_SYMBOL)
                if (strParts.size != 2) continue

                this += strParts[0] to strParts[1]
            }
        }
    }

    fun loadById(id: String) {
        loadFromRegister(localeRegisters.first { it.id == id })
    }

    fun loadByName(name: String) {
        loadFromRegister(localeRegisters.first { it.name == name })
    }

    fun nameToId(name: String) = localeRegisters.first { it.name == name }.id
}
