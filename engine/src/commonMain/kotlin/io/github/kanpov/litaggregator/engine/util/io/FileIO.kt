package io.github.kanpov.litaggregator.engine.util.io

import co.touchlab.kermit.Logger
import io.ktor.utils.io.core.toByteArray
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

fun writeFile(path: String, content: String): Boolean {
    return writeFile(path.asFile(), content)
}

fun writeFile(file: File, content: String): Boolean {
    return try {
        if (!file.exists()) {
            file.createNewFile()
            Logger.i { "Created new file: ${file.absolutePath}" }
        }

        FileOutputStream(file).use { stream ->
            stream.write(content.toByteArray())
        }

        Logger.i { "Wrote to file: ${file.absolutePath}" }
        true
    } catch (exception: Exception) {
        Logger.i { "Failed to write to file: ${file.absolutePath}. Cause:" }
        Logger.i { exception.stackTraceToString() }
        false
    }
}

fun readFile(file: File): String? {
    return try {
        var output = ""

        if (!file.exists()) {
            Logger.e { "Attempted to read from a non-existing file: ${file.absolutePath}" }
            return output
        }

        FileInputStream(file).use { stream ->
            output = stream.bufferedReader().readText()
        }
        Logger.i { "Read from file: ${file.absolutePath}" }

        output
    } catch (exception: Exception) {
        Logger.i { "Failed to read from file: ${file.absolutePath}. Cause:" }
        Logger.i { exception.stackTraceToString() }
        null
    }
}

fun readFile(path: String): String? {
    return readFile(path.asFile())
}

fun unzip(zipFilePath: File, destDirectory: String) {
    File(destDirectory).run {
        if (!exists()) {
            mkdirs()
        }
    }

    ZipFile(zipFilePath).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                val filePath = destDirectory + File.separator + entry.name
                if (!entry.isDirectory) {
                    extractFile(input, filePath)
                } else {
                    val dir = File(filePath)
                    dir.mkdir()
                }
            }
        }
    }
}

private fun extractFile(inputStream: InputStream, destFilePath: String) {
    val bos = BufferedOutputStream(FileOutputStream(destFilePath))
    val bytesIn = ByteArray(BUFFER_SIZE)
    var read: Int
    while (inputStream.read(bytesIn).also { read = it } != -1) {
        bos.write(bytesIn, 0, read)
    }
    bos.close()
}

private const val BUFFER_SIZE = 1024 * 10

fun String.asFile() = File(this)
