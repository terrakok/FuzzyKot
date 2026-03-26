package com.github.terrakok.fuzzykot

import com.github.terrakok.fuzzykot.MicroFuzz.Strategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MicrofuzzTest {

    @Test
    fun testExactMatch() {
        assertEquals(100, MicroFuzz.ratio("hello", "hello"))
    }

    @Test
    fun testNormalizedMatch() {
        assertEquals(99, MicroFuzz.ratio("HELLO", "hello"))
    }

    @Test
    fun testStartsWith() {
        assertEquals(97, MicroFuzz.ratio("hello", "hello world"))
    }

    @Test
    fun testContainsAtWordBoundary() {
        assertEquals(95, MicroFuzz.ratio("world", "hello world"))
    }

    @Test
    fun testContainsAnywhere() {
        assertEquals(90, MicroFuzz.ratio("ell", "hello"))
    }

    @Test
    fun testAggressiveMatch() {
        val r = MicroFuzz.ratio("hlo", "hello", strategy = Strategy.AGGRESSIVE)
        assertEquals(72, r)
    }

    @Test
    fun testSmartMatch() {
        val r = MicroFuzz.ratio("hlo", "hello", strategy = Strategy.SMART)
        assertEquals(80, r)
        
        val he = MicroFuzz.ratio("he", "hello", strategy = Strategy.SMART)
        assertTrue(he > 0)
    }

    @Test
    fun testMatchingRanges() {
        val ranges = MicroFuzz.matchingRanges("hlo", "hello", strategy = Strategy.AGGRESSIVE)
        assertEquals(3, ranges.size)
        assertEquals(0..0, ranges[0])
        assertEquals(2..2, ranges[1])
        assertEquals(4..4, ranges[2])
    }

    @Test
    fun testCollectionExtract() {
        val collection = listOf("Atlanta Falcons", "New York Jets", "New York Giants", "Dallas Cowboys")
        val results = collection.extractSorted("cowboys", cutoff = 1)
        assertEquals(1, results.size)
        assertEquals("Dallas Cowboys", results[0].referent)
        assertTrue(results[0].score >= 90)
    }

    @Test
    fun testMultiWordQuery() {
        // Query: "new york", Target: "New York Jets"
        // score: 1.5 + 2 * 0.2 = 1.9
        // ratio: 100 - 1.9 * 5 = 100 - 9.5 = 90.5 (rounded to 90 or 91)
        val ratio = MicroFuzz.ratio("new york", "New York Jets")
        assertTrue(ratio >= 90)
    }
}
