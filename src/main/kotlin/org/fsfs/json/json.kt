package org.fsfs.json

import arrow.core.Either
import arrow.core.identity
import com.google.gson.Gson
import java.io.File
import java.io.InputStream
import kotlin.reflect.KClass

object json {
    fun <E : kotlin.Any> unsafeDecodeFromResource(resource: String, ofClass: KClass<E>): Sequence<E> {
        return decodeFromResource(resource, ofClass).fold( { throw it }, ::identity)
    }

    fun <E : kotlin.Any> decodeFromResource(resource: String, ofClass: KClass<E>): Either<DeserializationError, Sequence<E>> {
        return decodeFromFile(
            File(this.javaClass.classLoader.getResource(resource).toURI()),
            ofClass
        )
    }

    fun  <E : kotlin.Any> decode(ofClass: KClass<E>): (Sequence<ByteArray>) -> Either<DeserializationError, Sequence<E>> {
        val deserializer = JsonSerialization(ofClass, Gson())
        return { input ->  deserializer.deserializeMultiple(SequenceByteArrayInputStream(input)) }
    }

    fun  <E : kotlin.Any> unsafeDecode(ofClass: KClass<E>): (Sequence<ByteArray>) -> Sequence<E> {
        return { input -> decode(ofClass)(input).fold( { throw it }, ::identity) }
    }

    fun  <E : kotlin.Any> decodeFromFile(file: File, ofClass: KClass<E>): Either<DeserializationError, Sequence<E>> {
        val deserializer = JsonSerialization(ofClass, Gson())
        return deserializer.deserializeMultiple(file.inputStream())
    }

    inline fun <reified E : kotlin.Any> encode(): (Sequence<E>) ->  Sequence<String> = { seq ->
        encode(seq)
    }

    inline fun <reified E : kotlin.Any> encode(seq: Sequence<E>) : Sequence<String> {
        val serializer = JsonSerialization(E::class, Gson())
        return object : Sequence<String> {
            var original = seq.iterator()
            var state = 0 // 0=beginning, 1=element, 2=after element(, or ]), 3=end

            override fun iterator(): Iterator<String> = object : Iterator<String> {

                override fun next(): String {
                    return when(state) {
                        0 -> {
                            state++
                            "["
                        }
                        1 -> {
                            state++
                            serializer.serializeOne(original.next())
                        }
                        else -> if (original.hasNext()) {
                            state--
                            ","
                        } else {
                            state++
                            "]"
                        }
                    }
                }

                override fun hasNext(): Boolean {
                    return state == 0 || state == 1 || state == 2
                }
            }

        }

    }
}

class SequenceJsonInputStream<T>(sequence: Sequence<T>, private val ofClass: Class<T>, private val gson: Gson) : InputStream() {
    val arrayOpen = "[".toByteArray()
    val arrayClose = "]".toByteArray()
    val sequenceIter = sequence.iterator()
    var buffer = "".toByteArray()
    var arrayOpenPos = 0
    var arrayClosePos = 0
    var bufferPos = 0

    override fun read(): Int {
        return if (arrayOpenPos < arrayOpen.size) {
            arrayOpen.get(arrayOpenPos++).toInt()
        } else if (bufferPos < buffer.size) {
            buffer.get(bufferPos++).toInt()
        } else if (sequenceIter.hasNext()) {
            buffer = if (buffer.isEmpty()) {
                gson.toJson(sequenceIter.next(), ofClass).toByteArray()
            } else {
                ",".toByteArray() + gson.toJson(sequenceIter.next(), ofClass).toByteArray()
            }
            bufferPos = 0
            buffer.get(bufferPos++).toInt()
        } else if (arrayClosePos < arrayClose.size) {
            arrayClose.get(arrayClosePos++).toInt()
        } else {
            -1
        }
    }

}

class SequenceByteArrayInputStream(sequence: Sequence<ByteArray>) : InputStream() {
    val sequenceIter = sequence.iterator()
    var buffer = "".toByteArray()
    var bufferPos = 0

    override fun read(): Int {
        return if (bufferPos < buffer.size) {
            buffer.get(bufferPos++).toInt()
        } else if (sequenceIter.hasNext()) {
            buffer = sequenceIter.next()
            bufferPos = 0
            buffer.get(bufferPos++).toInt()
        } else {
            -1
        }
    }

}