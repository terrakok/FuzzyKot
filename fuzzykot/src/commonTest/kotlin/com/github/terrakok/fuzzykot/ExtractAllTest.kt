package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtractAllTest {
    @Test
    fun testExtractAll() {
        val choices = listOf("myself", "me self", "my self", "not me")
        val results = choices.extractAll("myself")
        assertEquals(4, results.size)
        assertEquals(100, results.find { it.string == "myself" }?.score)
        assertEquals(77, results.find { it.string == "me self" }?.score)
        assertEquals(92, results.find { it.string == "my self" }?.score)
        assertEquals(33, results.find { it.string == "not me" }?.score)
    }

    @Test
    fun testExtractAllWithCutoff() {
        val choices = listOf("myself", "me self", "my self", "not me")
        val results = choices.extractAll("myself", cutoff = 80)
        assertEquals(2, results.size)
        assertTrue(results.all { it.score >= 80 })
    }

    @Test
    fun testEmptyCollection() {
        val choices = emptyList<String>()
        val results = choices.extractAll("test")
        assertTrue(results.isEmpty())
    }

    @Test
    fun testCustomScorer() {
        val choices = listOf("abc", "abd")
        val results = choices.extractAll("abc", scorer = { s1, s2 -> if (s1 == s2) 100 else 0 })
        assertEquals(100, results[0].score)
        assertEquals(0, results[1].score)
    }
}
