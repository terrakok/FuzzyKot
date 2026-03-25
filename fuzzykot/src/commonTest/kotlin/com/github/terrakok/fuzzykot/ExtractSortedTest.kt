package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtractSortedTest {
    @Test
    fun testExtractSorted() {
        val choices = listOf("not me", "me self", "my self", "myself")
        val results = choices.extractSorted("myself")
        assertEquals(4, results.size)
        assertEquals("myself", results[0].string)
        assertEquals(100, results[0].score)
        assertEquals("my self", results[1].string)
        assertEquals(92, results[1].score)
        assertEquals("me self", results[2].string)
        assertEquals(77, results[2].score)
        assertEquals("not me", results[3].string)
        assertEquals(33, results[3].score)
    }

    @Test
    fun testSortingIsDescending() {
        val choices = listOf("a", "abc", "ab")
        val results = choices.extractSorted("abc")
        for (i in 0 until results.size - 1) {
            assertTrue(results[i].score >= results[i + 1].score)
        }
    }

    @Test
    fun testWithCutoff() {
        val choices = listOf("myself", "me self", "my self", "not me")
        val results = choices.extractSorted("myself", cutoff = 90)
        assertEquals(2, results.size)
        assertEquals("myself", results[0].string)
        assertEquals("my self", results[1].string)
    }
}
