package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRatioTest {
    @Test
    fun testExactMatch() {
        assertEquals(100, "myself".weightedRatio("myself"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(77, "myself".weightedRatio("me self"))
    }

    @Test
    fun testCaseInsensitiveByDefault() {
        assertEquals(100, "MYSELF".weightedRatio("myself"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(0, "".weightedRatio(""))
    }

    @Test
    fun testOneEmptyString() {
        assertEquals(0, "abc".weightedRatio(""))
        assertEquals(0, "".weightedRatio("abc"))
    }

    @Test
    fun testLongerString() {
        assertEquals(90, "test".weightedRatio("this is a test"))
    }

    @Test
    fun testTokenSortImpact() {
        assertEquals(95, "my myself".weightedRatio("myself my"))
    }
}
