package org.fsfs.json

import arrow.core.Either
import com.google.gson.Gson
import io.kotlintest.IsolationMode
import io.kotlintest.assertions.arrow.either.beLeftOfType
import io.kotlintest.assertions.arrow.either.beRight
import io.kotlintest.matchers.haveSize
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.fsfs.util.Element
import org.fsfs.util.ElementGenerator
import org.skyscreamer.jsonassert.JSONAssert
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class serializationTest : StringSpec() {

    override fun isolationMode() = IsolationMode.InstancePerTest

    init {
        "JsonSerialization.serializeOne should correctly serialize one Element" {
            val element = ElementGenerator.createOne(1)
            val result = JsonSerialization(Element::class, Gson()).serializeOne(element)

            val expected = """{"id": "element1", "name": {"value": "Element 1"}}"""
            JSONAssert.assertEquals(expected, result, true)
        }

        "JsonSerialization.deserializeOne should correctly deserialize one Element" {
            val elementJsonStr = """{"id": "element1", "name": {"value": "Element 1"}}"""
            val resultE = JsonSerialization(Element::class, Gson()).deserializeOne(elementJsonStr)

            resultE should beRight()
            val result = (resultE as Either.Right).b
            result.id shouldBe "element1"
        }

        "JsonSerialization.deserializeOne should error if the serialized representation is invalid" {
            val elementJsonStr = """{"id": "element1", "name": {"""
            val resultE = JsonSerialization(Element::class, Gson()).deserializeOne(elementJsonStr)

            resultE should beLeftOfType<SyntaxError>()
        }

        "JsonSerialization.serializeMultiple should correctly serialize multiple Elements" {
            val elements = ElementGenerator.createMultiple(10)
            val oStream = ByteArrayOutputStream()
            JsonSerialization(Element::class, Gson()).serializeMultiple(elements).copyTo(oStream)
            val result = oStream.toString()

            val expected = "[${elements.map { JsonSerialization(Element::class, Gson()).serializeOne(it) }.joinToString()}]"
            JSONAssert.assertEquals(expected, result, true)
        }

        "JsonLineItemSerialization.deserializeMultiple should correctly deserialize Elements" {
            val lineItems = """[${(0 until 5).map{ """{"id": "element$it", "name": {"value": "Element $it"}}""" }.joinToString()}]"""
            val lineItemsStream = ByteArrayInputStream( lineItems.toByteArray() )
            val resultE = JsonSerialization(Element::class, Gson()).deserializeMultiple(lineItemsStream)

            resultE should beRight ()
            val result = (resultE as Either.Right).b.toList()
            result should haveSize(5)
        }
    }
}
