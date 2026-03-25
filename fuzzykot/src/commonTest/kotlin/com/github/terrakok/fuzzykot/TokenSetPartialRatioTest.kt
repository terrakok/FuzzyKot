package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSetPartialRatioTest {
    @Test
    fun testExactMatchWithDuplicatesAndMore() {
        assertEquals(100, "my myself".tokenSetPartialRatio("myself my my and more"))
    }

    @Test
    fun testOrderAndDuplicatesAndMore() {
        assertEquals(100, "order is not important".tokenSetPartialRatio("important is not order order and more"))
    }

    @Test
    fun testPartialSetMatch() {
        assertEquals(100, "mariners".tokenSetPartialRatio("mariners vs angels"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, "".tokenSetPartialRatio(""))
    }
}
