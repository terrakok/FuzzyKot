package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSortPartialRatioTest {
    @Test
    fun testExactMatchAfterSort() {
        assertEquals(100, Levenshtein.tokenSortPartialRatio("my myself", "myself my"))
    }

    @Test
    fun testPartialMatchWithSort() {
        assertEquals(89, Levenshtein.tokenSortPartialRatio("order is important", "important is order and more"))
    }

    @Test
    fun testOrderIsNotImportant() {
        assertEquals(77, Levenshtein.tokenSortPartialRatio("order is not important", "important is not order and more"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(67, Levenshtein.tokenSortPartialRatio("myself", "me self"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, Levenshtein.tokenSortPartialRatio("", ""))
    }
}
