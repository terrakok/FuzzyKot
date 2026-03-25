package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSetRatioTest {
    @Test
    fun testExactMatchWithDuplicates() {
        assertEquals(100, "my myself".tokenSetRatio("myself my my"))
    }

    @Test
    fun testOrderAndDuplicates() {
        assertEquals(100, "order is not important".tokenSetRatio("important is not order order"))
    }

    @Test
    fun testPartialSetMatch() {
        // "mariners" vs "mariners vs angels"
        assertEquals(100, "mariners".tokenSetRatio("mariners vs angels"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, "".tokenSetRatio(""))
    }

    @Test
    fun testTotallyDifferent() {
        assertEquals(0, "abc".tokenSetRatio("def"))
    }
}
