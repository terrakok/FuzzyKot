package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSortRatioTest {
    @Test
    fun testExactMatchAfterSort() {
        assertEquals(100, "my myself".tokenSortRatio("myself my"))
    }

    @Test
    fun testOrderIsNotImportant() {
        assertEquals(100, "order is not important".tokenSortRatio("important is not order"))
    }

    @Test
    fun testCaseInsensitiveByDefault() {
        assertEquals(100, "MYSELF MY".tokenSortRatio("myself my"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(77, "myself".tokenSortRatio("me self"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, "".tokenSortRatio(""))
    }

    @Test
    fun testMultipleSpaces() {
        assertEquals(100, "my  myself".tokenSortRatio("myself   my"))
    }

    @Test
    fun testWithDifferentProcessor() {
        // use original case
        assertEquals(11, "MYSELF MY".tokenSortRatio("myself my", processor = { it }))
    }
}
