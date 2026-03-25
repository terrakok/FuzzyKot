package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtractOneTest {
    @Test
    fun testExtractOne() {
        val choices = listOf("myself", "me self", "my self", "not me")
        val result = choices.extractOne("myself")
        assertEquals("myself", result?.string)
        assertEquals(100, result?.score)
        assertEquals(0, result?.index)
    }

    @Test
    fun testNoMatchBelowCutoff() {
        val choices = listOf("abc", "def")
        val result = choices.extractOne("xyz", cutoff = 50)
        assertNull(result)
    }

    @Test
    fun testEmptyCollection() {
        val choices = emptyList<String>()
        val result = choices.extractOne("test")
        assertNull(result)
    }

    @Test
    fun testGenericExtractOne() {
        data class Item(val id: Int, val name: String)
        val choices = listOf(
            Item(1, "myself"),
            Item(2, "me self")
        )
        val result = choices.extractOne("myself", processor = { it.name })
        assertEquals(Item(1, "myself"), result?.referent)
    }
}
