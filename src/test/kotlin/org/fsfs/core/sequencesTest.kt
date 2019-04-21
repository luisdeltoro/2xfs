package org.fsfs.core

import io.kotlintest.IsolationMode
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class sequencesTest : StringSpec() {

    override fun isolationMode() = IsolationMode.InstancePerTest

    init {
        "intersperse adds an element in between elements of a sequence" {
            val expected = sequenceOf('A', '-', 'B', '-', 'C', '-', 'D', '-', 'E')

            val letters = sequenceOf('A', 'B', 'C', 'D', 'E')
            val lettersWithDashes = letters.intersperse('-').log("test1")

            lettersWithDashes.toList() shouldBe expected.toList()
        }

    }

}

