package com.github.terrakok.fuzzykot

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Calculates a simple Levenshtein distance ratio between two strings.
 *
 * @param other The string to compare against.
 * @param processor A function to process strings before comparison (default: no-op).
 * @return An integer ratio between 0 and 100.
 */
fun String.ratio(other: String, processor: (String) -> String = { it }): Int {
    val s1 = processor(this)
    val s2 = processor(other)
    return round(100 * getRatio(s1, s2)).toInt()
}

/**
 * Calculates a partial Levenshtein distance ratio.
 * This finds the best match of the shorter string within the longer one.
 *
 * @param other The string to compare against.
 * @param processor A function to process strings before comparison (default: no-op).
 * @return An integer ratio between 0 and 100.
 */
fun String.partialRatio(other: String, processor: (String) -> String = { it }): Int {
    val s1 = processor(this)
    val s2 = processor(other)

    val shorter: String
    val longer: String

    if (s1.length < s2.length) {
        shorter = s1
        longer = s2
    } else {
        shorter = s2
        longer = s1
    }

    if (shorter.isEmpty()) {
        return if (longer.isEmpty()) 100 else 0
    }

    val matchingBlocks = getMatchingBlocks(shorter, longer)
    val scores = mutableListOf<Double>()

    for (mb in matchingBlocks) {
        val dist = mb.dpos - mb.spos
        val longStart = if (dist > 0) dist else 0
        var longEnd = longStart + shorter.length
        if (longEnd > longer.length) longEnd = longer.length

        val longSubstr = longer.substring(longStart, longEnd)
        val ratio = getRatio(shorter, longSubstr)

        if (ratio > .995) return 100
        scores.add(ratio)
    }

    return round(100 * (scores.maxOrNull() ?: 0.0)).toInt()
}

/**
 * Calculates a token sort ratio.
 * Tokens are split, sorted alphabetically, and then joined back to compare.
 *
 * @param other The string to compare against.
 * @param processor A function to process strings before comparison (default: lowercase).
 * @return An integer ratio between 0 and 100.
 */
fun String.tokenSortRatio(other: String, processor: (String) -> String = { it.lowercase() }): Int {
    val sorted1 = processAndSort(this, processor)
    val sorted2 = processAndSort(other, processor)
    return sorted1.ratio(sorted2)
}

/**
 * Calculates a token sort partial ratio.
 * Similar to [tokenSortRatio] but uses [partialRatio] for the final comparison.
 *
 * @param other The string to compare against.
 * @param processor A function to process strings before comparison (default: lowercase).
 * @return An integer ratio between 0 and 100.
 */
fun String.tokenSortPartialRatio(other: String, processor: (String) -> String = { it.lowercase() }): Int {
    val sorted1 = processAndSort(this, processor)
    val sorted2 = processAndSort(other, processor)
    return sorted1.partialRatio(sorted2)
}

/**
 * Calculates a token set ratio.
 * This handles differences in token sets by comparing the intersection and symmetric difference.
 *
 * @param other The string to compare against.
 * @param processor A function to process strings before comparison (default: lowercase).
 * @return An integer ratio between 0 and 100.
 */
fun String.tokenSetRatio(other: String, processor: (String) -> String = { it.lowercase() }): Int {
    val s1 = processor(this)
    val s2 = processor(other)

    val tokens1 = s1.tokenize().toSet()
    val tokens2 = s2.tokenize().toSet()

    val intersection = tokens1.intersect(tokens2)
    val diff1to2 = tokens1.subtract(tokens2)
    val diff2to1 = tokens2.subtract(tokens1)

    val sortedIntersection = intersection.sorted().joinToString(" ").trim()
    val sortedDiff1to2 = (intersection + diff1to2).sorted().joinToString(" ").trim()
    val sortedDiff2to1 = (intersection + diff2to1).sorted().joinToString(" ").trim()

    return maxOf(
        sortedIntersection.ratio(sortedDiff1to2),
        sortedIntersection.ratio(sortedDiff2to1),
        sortedDiff1to2.ratio(sortedDiff2to1)
    )
}

/**
 * Calculates a token set partial ratio.
 * Similar to [tokenSetRatio] but uses [partialRatio] for comparison.
 *
 * @param other The string to compare against.
 * @param processor A function to process strings before comparison (default: lowercase).
 * @return An integer ratio between 0 and 100.
 */
fun String.tokenSetPartialRatio(other: String, processor: (String) -> String = { it.lowercase() }): Int {
    val s1 = processor(this)
    val s2 = processor(other)

    val tokens1 = s1.tokenize().toSet()
    val tokens2 = s2.tokenize().toSet()

    val intersection = tokens1.intersect(tokens2)
    val diff1to2 = tokens1.subtract(tokens2)
    val diff2to1 = tokens2.subtract(tokens1)

    val sortedIntersection = intersection.sorted().joinToString(" ").trim()
    val sortedDiff1to2 = (intersection + diff1to2).sorted().joinToString(" ").trim()
    val sortedDiff2to1 = (intersection + diff2to1).sorted().joinToString(" ").trim()

    return maxOf(
        sortedIntersection.partialRatio(sortedDiff1to2),
        sortedIntersection.partialRatio(sortedDiff2to1),
        sortedDiff1to2.partialRatio(sortedDiff2to1)
    )
}

/**
 * Calculates a weighted ratio based on several other ratio algorithms.
 * It combines [ratio], [partialRatio], and token-based ratios with specific weights.
 *
 * @param other The string to compare against.
 * @param processor A function to process strings before comparison (default: lowercase).
 * @return An integer ratio between 0 and 100.
 */
fun String.weightedRatio(other: String, processor: (String) -> String = { it.lowercase() }): Int {
    val s1 = processor(this)
    val s2 = processor(other)

    if (s1.isEmpty() || s2.isEmpty()) return 0

    val unbaseScale = 0.95
    var partialScale = 0.90
    val len1 = s1.length
    val len2 = s2.length
    val lenRatio = max(len1, len2).toDouble() / min(len1, len2)

    val base = s1.ratio(s2)

    if (lenRatio > 1.5) {
        if (lenRatio > 8) partialScale = 0.6

        val partial = s1.partialRatio(s2) * partialScale
        val partialSor = s1.tokenSortPartialRatio(s2, { it }) * unbaseScale * partialScale
        val partialSet = s1.tokenSetPartialRatio(s2, { it }) * unbaseScale * partialScale

        return round(maxOf(base.toDouble(), partial, partialSor, partialSet)).toInt()
    } else {
        val tokenSort = s1.tokenSortRatio(s2, { it }) * unbaseScale
        val tokenSet = s1.tokenSetRatio(s2, { it }) * unbaseScale

        return round(maxOf(base.toDouble(), tokenSort, tokenSet)).toInt()
    }
}

private fun processAndSort(input: String, processor: (String) -> String): String =
    processor(input).tokenize().sorted().joinToString(" ").trim()

private fun String.tokenize(): List<String> =
    split("\\s+".toRegex()).filter { it.isNotEmpty() }
