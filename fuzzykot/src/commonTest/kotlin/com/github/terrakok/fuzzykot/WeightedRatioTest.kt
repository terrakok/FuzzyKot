package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRatioTest {
    @Test
    fun testExactMatch() {
        assertEquals(100, Levenshtein.weightedRatio("myself", "myself"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(77, Levenshtein.weightedRatio("me self", "myself"))
    }

    @Test
    fun testCaseInsensitiveByDefault() {
        assertEquals(100, Levenshtein.weightedRatio("MYSELF", "myself"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(0, Levenshtein.weightedRatio("", ""))
    }

    @Test
    fun testOneEmptyString() {
        assertEquals(0, Levenshtein.weightedRatio("abc", ""))
        assertEquals(0, Levenshtein.weightedRatio("", "abc"))
    }

    @Test
    fun testLongerString() {
        assertEquals(90, Levenshtein.weightedRatio("test", "this is a test"))
    }

    @Test
    fun testTokenSortImpact() {
        assertEquals(95, Levenshtein.weightedRatio("my myself", "myself my"))
    }
}
