package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals

class PartialRatioTest {
    @Test
    fun testExactMatch() {
        assertEquals(100, Levenshtein.partialRatio("myself", "myself"))
    }

    @Test
    fun testSubstring() {
        assertEquals(100, Levenshtein.partialRatio("myself", "my myself and I"))
    }

    @Test
    fun testSubstringReverse() {
        assertEquals(100, Levenshtein.partialRatio("my myself and I", "myself"))
    }

    @Test
    fun testPartialSubstring() {
        assertEquals(67, Levenshtein.partialRatio("myself", "me self"))
    }

    @Test
    fun testTotallyDifferent() {
        assertEquals(0, Levenshtein.partialRatio("abc", "defgh"))
    }

    @Test
    fun testEmptyStrings() {
        assertEquals(100, Levenshtein.partialRatio("", ""))
    }

    @Test
    fun testOneEmptyString() {
        assertEquals(0, Levenshtein.partialRatio("abc", ""))
        assertEquals(0, Levenshtein.partialRatio("", "abc"))
    }

    @Test
    fun testLongerShorterIdentical() {
        assertEquals(100, Levenshtein.partialRatio("test", "this is a test"))
    }

    @Test
    fun testPartialMatchInLongerString() {
        assertEquals(75, Levenshtein.partialRatio("abcd", "abce"))
        assertEquals(100, Levenshtein.partialRatio("abcd", "abce abcd"))
    }
}
