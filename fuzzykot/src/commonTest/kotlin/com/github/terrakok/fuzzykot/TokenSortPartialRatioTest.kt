package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSortPartialRatioTest {
    @Test
    fun testExactMatchAfterSort() {
        assertEquals(100, "my myself".tokenSortPartialRatio("myself my"))
    }

    @Test
    fun testPartialMatchWithSort() {
        assertEquals(89, "order is important".tokenSortPartialRatio("important is order and more"))
    }

    @Test
    fun testOrderIsNotImportant() {
        assertEquals(77, "order is not important".tokenSortPartialRatio("important is not order and more"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(67, "myself".tokenSortPartialRatio("me self"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, "".tokenSortPartialRatio(""))
    }
}
