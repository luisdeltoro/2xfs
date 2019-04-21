package org.fsfs.util

object ElementGenerator {

    fun createOne(id: Int): Element {
        return Element(
            id = "element$id",
            name = Name("Element $id")
        )
    }

    fun createMultiple(num: Int): Sequence<Element> {
        return (0 until num).asSequence().map {
            createOne(it)
        }
    }
}

data class Name(val value: String)
data class Element(val id: String, val name: Name)