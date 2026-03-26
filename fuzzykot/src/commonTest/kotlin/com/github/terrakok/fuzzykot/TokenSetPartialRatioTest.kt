package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSetPartialRatioTest {
    @Test
    fun testExactMatchWithDuplicatesAndMore() {
        assertEquals(100, Levenshtein.tokenSetPartialRatio("my myself", "myself my my and more"))
    }

    @Test
    fun testOrderAndDuplicatesAndMore() {
        assertEquals(100, Levenshtein.tokenSetPartialRatio("order is not important", "important is not order order and more"))
    }

    @Test
    fun testPartialSetMatch() {
        assertEquals(100, Levenshtein.tokenSetPartialRatio("mariners", "mariners vs angels"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, Levenshtein.tokenSetPartialRatio("", ""))
    }
}
