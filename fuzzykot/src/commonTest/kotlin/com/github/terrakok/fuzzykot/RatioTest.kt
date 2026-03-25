package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class RatioTest {
    @Test
    fun testExactMatch() {
        assertEquals(100, "myself".ratio("myself"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, "".ratio(""))
    }

    @Test
    fun testOneEmptyString() {
        assertEquals(0, "abc".ratio(""))
        assertEquals(0, "".ratio("abc"))
    }

    @Test
    fun testTotallyDifferent() {
        assertEquals(0, "abc".ratio("def"))
    }

    @Test
    fun testPartialMatch() {
        assertEquals(77, "myself".ratio("me self"))
    }

    @Test
    fun testCaseSensitivity() {
        // ratio is case sensitive by default
        assertEquals(0, "MYSELF".ratio("myself"))
    }

    @Test
    fun testWithProcessor() {
        assertEquals(100, "MYSELF".ratio("myself", processor = { it.lowercase() }))
    }

    @Test
    fun testLengthDifference() {
        assertEquals(75, "abc".ratio("abcde"))
    }
}
