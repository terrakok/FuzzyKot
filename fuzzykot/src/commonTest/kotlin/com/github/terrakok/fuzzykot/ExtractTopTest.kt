package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class ExtractTopTest {
    @Test
    fun testExtractTop() {
        val choices = listOf("myself", "me self", "my self", "not me")
        val results = choices.extractTop("myself", limit = 2)
        assertEquals(2, results.size)
        assertEquals("myself", results[0].string)
        assertEquals(100, results[0].score)
        assertEquals("my self", results[1].string)
        assertEquals(88, results[1].score)
    }

    @Test
    fun testLimitGreaterThanSize() {
        val choices = listOf("a", "b")
        val results = choices.extractTop("a", limit = 10)
        assertEquals(2, results.size)
    }

    @Test
    fun testLimitZero() {
        val choices = listOf("a", "b")
        val results = choices.extractTop("a", limit = 0)
        assertEquals(0, results.size)
    }

    @Test
    fun testGenericExtractTop() {
        data class Item(val id: Int, val name: String)
        val choices = listOf(
            Item(1, "myself"),
            Item(2, "me self"),
            Item(3, "my self"),
            Item(4, "not me")
        )
        val results = choices.extractTop("myself", limit = 2, processor = { it.name })
        assertEquals(2, results.size)
        assertEquals(Item(1, "myself"), results[0].referent)
        assertEquals(Item(3, "my self"), results[1].referent)
    }

    @Test
    fun testWithCutoff() {
        val choices = listOf("myself", "me self", "my self", "not me")
        val results = choices.extractTop("myself", limit = 10, cutoff = 70)
        assertEquals(2, results.size)
    }
}
