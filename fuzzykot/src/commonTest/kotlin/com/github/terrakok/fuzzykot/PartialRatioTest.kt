package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class PartialRatioTest {
    @Test
    fun testExactMatch() {
        assertEquals(100, "myself".partialRatio("myself"))
    }

    @Test
    fun testSubstring() {
        assertEquals(100, "myself".partialRatio("my myself and I"))
    }

    @Test
    fun testSubstringReverse() {
        assertEquals(100, "my myself and I".partialRatio("myself"))
    }

    @Test
    fun testPartialSubstring() {
        assertEquals(67, "myself".partialRatio("me self"))
    }

    @Test
    fun testTotallyDifferent() {
        assertEquals(0, "abc".partialRatio("defgh"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, "".partialRatio(""))
    }

    @Test
    fun testOneEmptyString() {
        assertEquals(0, "abc".partialRatio(""))
        assertEquals(0, "".partialRatio("abc"))
    }

    @Test
    fun testLongerShorterIdentical() {
        assertEquals(100, "test".partialRatio("this is a test"))
    }

    @Test
    fun testPartialMatchInLongerString() {
        assertEquals(75, "abcd".partialRatio("abce"))
        assertEquals(100, "abcd".partialRatio("abce abcd"))
    }
}
