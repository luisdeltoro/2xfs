package org.fsfs.core

import java.nio.charset.Charset

object text {
    val utf8Charset = Charset.forName("UTF-8")

    fun utf8Encode(): (Sequence<String>) -> Sequence<ByteArray> = { seq -> seq.map { it.toByteArray(utf8Charset) } }

    fun utf8Decode(): (Sequence<ByteArray>) -> Sequence<String> = { seq -> seq.map { it.toString(utf8Charset) } }

}