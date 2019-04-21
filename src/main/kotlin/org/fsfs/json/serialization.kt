package org.fsfs.json

import arrow.core.Either
import arrow.core.Try
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import org.fsfs.core.AutoClosingSequence
import org.fsfs.core.text
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.reflect.KClass

sealed class DeserializationError : Exception()
data class SyntaxError(override val message: String) : DeserializationError()
data class UnexpectedError(val exception: Exception) : DeserializationError()

interface Serializer<A> {

    val serializeOne: (A) -> String

    val serializeMultiple: (Sequence<A>) -> InputStream

    val format: String
}

interface Deserializer<A> {

    val deserializeOne: (String) -> Either<DeserializationError, A>

    val deserializeMultiple: (InputStream) -> Either<DeserializationError, Sequence<A>>

    val format: String
}

interface Serialization<A> : Serializer<A>, Deserializer<A>

class JsonSerialization<A : kotlin.Any>(val kclass: KClass<A>, val gson: Gson): Serialization<A> {

    override val serializeOne = { a: A -> gson.toJson(a) }

    override val deserializeOne = { jsonString: String -> Try { gson.fromJson(jsonString, kclass.java) }.toEither().mapLeft { mapErrors(it) } }

    override val deserializeMultiple = { iStream: InputStream ->
        Try {
            val reader = JsonReader(InputStreamReader(iStream, text.utf8Charset))
            reader.beginArray()

            val jsonSeq = Sequence { object : Iterator<A> {
                override fun next(): A = gson.fromJson(reader, kclass.java)
                override fun hasNext(): Boolean = reader.hasNext()
            }}
            AutoClosingSequence(jsonSeq, reader)

        }.toEither().mapLeft { mapErrors(it) }

    }

    override val serializeMultiple: (Sequence<A>) -> InputStream = { seq: Sequence<A> ->
        SequenceJsonInputStream(seq, kclass.java, gson)
    }

    override val format = "json"

    private val mapErrors = { t: Throwable ->
        when(t) {
            is JsonSyntaxException -> SyntaxError(t.localizedMessage)
            is Exception ->UnexpectedError(t)
            else -> throw t
        }
    }

}