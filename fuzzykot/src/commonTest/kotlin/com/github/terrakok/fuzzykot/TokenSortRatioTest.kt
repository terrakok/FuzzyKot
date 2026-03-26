package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSortRatioTest {
    @Test
    fun testExactMatchAfterSort() {
        assertEquals(100, Levenshtein.tokenSortRatio("my myself", "myself my"))
    }

    @Test
    fun testOrderIsNotImportant() {
        assertEquals(100, Levenshtein.tokenSortRatio("order is not important", "important is not order"))
    }

    @Test
    fun testCaseInsensitiveByDefault() {
        assertEquals(100, Levenshtein.tokenSortRatio("MYSELF MY", "myself my"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(77, Levenshtein.tokenSortRatio("myself", "me self"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, Levenshtein.tokenSortRatio("", ""))
    }

    @Test
    fun testMultipleSpaces() {
        assertEquals(100, Levenshtein.tokenSortRatio("my  myself", "myself   my"))
    }

    @Test
    fun testWithDifferentProcessor() {
        // use original case
        assertEquals(11, Levenshtein.tokenSortRatio("MYSELF MY", "myself my", processor = { it }))
    }
}
