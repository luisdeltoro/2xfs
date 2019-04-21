package org.fsfs.core

import java.io.BufferedInputStream
import java.io.File
import java.util.*

object io {
    fun linesFromResource(resource: String): Sequence<String> {
        return linesFromFile(File(this.javaClass.classLoader.getResource(resource).toURI()))
    }

    fun linesFromFile(file: File): Sequence<String> {
        val reader = file.bufferedReader()
        return AutoClosingSequence(reader.lineSequence(), reader)
    }

    fun bytesFromResource(resource: String, bufferSize: Int = DEFAULT_BUFFER_SIZE): Sequence<ByteArray> {
        return bytesFromFile(
            File(this.javaClass.classLoader.getResource(resource).toURI()),
            bufferSize
        )
    }

    fun bytesFromFile(file: File, bufferSize: Int = DEFAULT_BUFFER_SIZE): Sequence<ByteArray> {
        val iStream = BufferedInputStream(file.inputStream(), bufferSize)
        val seq = Sequence { object : Iterator<ByteArray> {
            var buffer = ByteArray(bufferSize)
            var available = iStream.read(buffer, 0, bufferSize - 1)

            override fun hasNext(): Boolean {
                return available > 0
            }

            override fun next(): ByteArray {
                val next = Arrays.copyOf(buffer, available)
                available = iStream.read(buffer, 0, bufferSize - 1)
                return next
            }
        } }
        return AutoClosingSequence(seq, iStream)
    }
}

fun Sequence<ByteArray>.toFile(file: File): Unit {
    return this.map { file.appendBytes(it) }.consume()
}

fun Sequence<ByteArray>.toFile(pathanme: String): Unit {
    val file = File(pathanme)
    file.createNewFile()
    return this.toFile(file)
}

