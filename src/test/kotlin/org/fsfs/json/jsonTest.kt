package org.fsfs.json

import io.kotlintest.IsolationMode
import io.kotlintest.specs.StringSpec
import org.fsfs.core.*
import org.fsfs.util.Element
import org.fsfs.util.Name
import org.skyscreamer.jsonassert.JSONAssert
import java.io.File
import java.util.*

class jsonTest : StringSpec() {

    override fun isolationMode() = IsolationMode.InstancePerTest

    init {
        "decode json from file, enhance and write json to file" {
            val targetFileName = "/tmp/elements-after-${UUID.randomUUID()}.txt"
            val elementsToAdd = sequenceOf(Element("element3", Name("Element 3")))
            val elementsFromFile =
                json.unsafeDecodeFromResource("elements-before.json", Element::class)
            val all = elementsFromFile + elementsToAdd
            all.through(json.encode())
                .log("test1")
                .through(text.utf8Encode())
                .toFile(targetFileName)

            val result = File(targetFileName).readText(text.utf8Charset)
            val expected = File(this.javaClass.classLoader.getResource("elements-after.json").toURI()).readText(text.utf8Charset)
            JSONAssert.assertEquals(expected, result, true)
        }

        "decode json from sequence of byte arrays, enhance and write json to file" {
            val targetFileName = "/tmp/elements-after-${UUID.randomUUID()}.txt"
            val elementsToAdd = sequenceOf(Element("element3", Name("Element 3")))
            val elementsFromFile = io.bytesFromResource("elements-before.json")
                .through(json.unsafeDecode(Element::class))
            val all = elementsFromFile + elementsToAdd
            all.through(json.encode())
                .log("test2")
                .through(text.utf8Encode())
                .toFile(targetFileName)

            val result = File(targetFileName).readText(text.utf8Charset)
            val expected = File(this.javaClass.classLoader.getResource("elements-after.json").toURI()).readText(text.utf8Charset)
            JSONAssert.assertEquals(expected, result, true)
        }

        "read byte arrays, decode to ut8, encode ut8 and write to file" {
            val targetFileName = "/tmp/elements-before-${UUID.randomUUID()}.txt"
            io.bytesFromResource("elements-before.json", 5)
                .through(text.utf8Decode())
                .log("test3")
                .through(text.utf8Encode())
                .toFile(targetFileName)

            val result = File(targetFileName).readText(text.utf8Charset)
            val expected = File(this.javaClass.classLoader.getResource("elements-before.json").toURI()).readText(text.utf8Charset)
            JSONAssert.assertEquals(expected, result, true)
        }

    }
}