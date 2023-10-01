package io.github.kanpov.litaggregator.engine.util

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.toByteArray
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

fun writeFile(path: String, content: String) {
    writeFile(File(path), content)
}

fun writeFile(file: File, content: String) {
    if (!file.exists()) {
        file.createNewFile()
        Napier.i { "Created new file: ${file.absolutePath}" }
    }

    FileOutputStream(file).use { stream ->
        stream.write(content.toByteArray())
    }

    Napier.i { "Wrote to file: ${file.absolutePath}" }
}

fun readFile(file: File): String {
    var output = ""

    if (!file.exists()) {
        Napier.e { "Attempted to read from a non-existing file: ${file.absolutePath}" }
        return output
    }

    FileInputStream(file).use { stream ->
        output = stream.bufferedReader().readText()
    }
    Napier.i { "Read from file: ${file.absolutePath}" }

    return output
}

fun readFile(path: String): String {
    return readFile(File(path))
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
