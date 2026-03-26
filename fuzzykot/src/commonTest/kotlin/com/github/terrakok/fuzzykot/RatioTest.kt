package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class RatioTest {
    @Test
    fun testExactMatch() {
        assertEquals(100, Levenshtein.ratio("myself", "myself"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, Levenshtein.ratio("", ""))
    }

    @Test
    fun testOneEmptyString() {
        assertEquals(0, Levenshtein.ratio("abc", ""))
        assertEquals(0, Levenshtein.ratio("", "abc"))
    }

    @Test
    fun testTotallyDifferent() {
        assertEquals(0, Levenshtein.ratio("abc", "def"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(77, Levenshtein.ratio("myself", "me self"))
    }

    @Test
    fun testCaseSensitivity() {
        // ratio is case sensitive by default
        assertEquals(0, Levenshtein.ratio("MYSELF", "myself"))
    }

    @Test
    fun testWithProcessor() {
        assertEquals(100, Levenshtein.ratio("MYSELF", "myself", processor = { it.lowercase() }))
    }

    @Test
    fun testLengthDifference() {
        assertEquals(75, Levenshtein.ratio("abc", "abcde"))
    }
}
