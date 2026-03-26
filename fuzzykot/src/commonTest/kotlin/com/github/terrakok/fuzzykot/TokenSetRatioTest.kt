package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSetRatioTest {
    @Test
    fun testExactMatchWithDuplicates() {
        assertEquals(100, Levenshtein.tokenSetRatio("my myself", "myself my my"))
    }

    @Test
    fun testOrderAndDuplicates() {
        assertEquals(100, Levenshtein.tokenSetRatio("order is not important", "important is not order order"))
    }

    @Test
    fun testPartialSetMatch() {
        // "mariners" vs "mariners vs angels"
        assertEquals(100, Levenshtein.tokenSetRatio("mariners", "mariners vs angels"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, Levenshtein.tokenSetRatio("", ""))
    }

    @Test
    fun testTotallyDifferent() {
        assertEquals(0, Levenshtein.tokenSetRatio("abc", "def"))
    }
}
