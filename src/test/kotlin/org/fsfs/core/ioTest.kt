package org.fsfs.core

import io.kotlintest.IsolationMode
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.skyscreamer.jsonassert.JSONAssert
import java.io.File
import java.text.DecimalFormat
import java.util.*

class ioTest : StringSpec() {

    override fun isolationMode() = IsolationMode.InstancePerTest

    init {

        "read from file, process and write to file" {
            val targetFileName = "/tmp/quantities-in-imperial-system-${UUID.randomUUID()}.txt"
            io.linesFromResource("quantities-in-metric-system.txt")
                .filter { s -> !s.trim().isEmpty() && !s.startsWith("#") }
                .map { kilosToPounds(it) }
                .log("test1")
                .intersperse("\n")
                .through(text.utf8Encode())
                .toFile(targetFileName) // terminal operation (consumes the sequence)

            val result = File(targetFileName).readText(text.utf8Charset)
            val expected = File(this.javaClass.classLoader.getResource("quantities-in-imperial-system.txt").toURI()).readText(
                text.utf8Charset
            )
            result shouldBe expected
        }

        "read byte arrays, decode to ut8, encode ut8 and write to file" {
            val targetFileName = "/tmp/elements-before-${UUID.randomUUID()}.txt"
            io.bytesFromResource("elements-before.json", 5)
                .through(text.utf8Decode())
                .log("test2")
                .through(text.utf8Encode())
                .toFile(targetFileName)

            val result = File(targetFileName).readText(text.utf8Charset)
            val expected = File(this.javaClass.classLoader.getResource("elements-before.json").toURI()).readText(text.utf8Charset)
            JSONAssert.assertEquals(expected, result, true)
        }

    }

    fun kilosToPounds(k: String): String {
        val df = DecimalFormat("#.#")
        val p = k.toDouble() * 2.20462
        return df.format(p)
    }

}