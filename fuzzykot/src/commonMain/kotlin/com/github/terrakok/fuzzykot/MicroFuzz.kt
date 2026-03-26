package com.github.terrakok.fuzzykot

import com.github.terrakok.fuzzykot.MicroFuzz.Strategy
import kotlin.math.min

/**
 * Port of the microfuzz library logic (https://github.com/Nozbe/microfuzz).
 */
object MicroFuzz {
    enum class Strategy {
        /**
         * Finds the first occurrence of each character in order.
         */
        AGGRESSIVE,

        /**
         * Only matches beginnings of words or chunks of 3+ letters.
         */
        SMART
    }

    fun ratio(
        query: String,
        target: String,
        strategy: Strategy = Strategy.SMART
    ): Int {
        val result = matchesFuzzily(target, query, strategy) ?: return 0
        val score = result.first
        // Mapping lower-is-better score to 0-100
        // A score of 0 is perfect (100).
        // A score of 20 or more is essentially a no-match (0).
        return (100.0 - (score * 5.0)).coerceIn(0.0, 100.0).toInt()
    }

    /**
     * Finds matching ranges between the query and target using microfuzz logic.
     */
    fun matchingRanges(
        query: String,
        target: String,
        strategy: Strategy = Strategy.SMART
    ): List<IntRange> {
        val result = matchesFuzzily(target, query, strategy) ?: return emptyList()
        return result.second
    }
}

private val validWordBoundaries = "  []()-–—'\"“”".toSet()

private fun isValidWordBoundary(character: Char): Boolean =
    character in validWordBoundaries

private fun normalizeText(text: String): String =
    text.lowercase().trim()

private fun matchesFuzzily(
    item: String,
    query: String,
    strategy: Strategy
): Pair<Double, List<IntRange>>? {
    if (query.isEmpty()) return 0.0 to emptyList()
    if (item.isEmpty()) return null

    if (item == query) {
        return 0.0 to listOf(item.indices)
    }

    val normalizedItem = normalizeText(item)
    val normalizedQuery = normalizeText(query)

    if (normalizedItem == normalizedQuery) {
        return 0.1 to listOf(normalizedItem.indices)
    } else if (normalizedItem.startsWith(normalizedQuery)) {
        return 0.5 to listOf(normalizedQuery.indices)
    }

    val exactContainsIdx = item.indexOf(query)
    if (exactContainsIdx > -1 && (exactContainsIdx == 0 || isValidWordBoundary(item[exactContainsIdx - 1]))) {
        return 0.9 to listOf(exactContainsIdx until (exactContainsIdx + query.length))
    }

    val containsIdx = normalizedItem.indexOf(normalizedQuery)
    if (containsIdx > -1 && (containsIdx == 0 || isValidWordBoundary(normalizedItem[containsIdx - 1]))) {
        return 1.0 to listOf(containsIdx until (containsIdx + normalizedQuery.length))
    }

    val queryWords = normalizedQuery.split(" ").filter { it.isNotEmpty() }
    val itemWords = normalizedItem.split(" ").filter { it.isNotEmpty() }.toSet()

    if (queryWords.size > 1) {
        if (queryWords.all { it in itemWords }) {
            val score = 1.5 + queryWords.size * 0.2
            val ranges = queryWords.map { word ->
                val wordIndex = normalizedItem.indexOf(word)
                wordIndex until (wordIndex + word.length)
            }.sortedBy { it.first }
            return score to ranges
        }
    }

    if (containsIdx > -1) {
        return 2.0 to listOf(containsIdx until (containsIdx + normalizedQuery.length))
    }

    return when (strategy) {
        Strategy.AGGRESSIVE -> aggressiveFuzzyMatch(normalizedItem, normalizedQuery)
        Strategy.SMART -> experimentalSmartFuzzyMatch(normalizedItem, normalizedQuery)
    }
}

private fun aggressiveFuzzyMatch(
    normalizedItem: String,
    normalizedQuery: String
): Pair<Double, List<IntRange>>? {
    val indices = mutableListOf<IntRange>()
    var queryIdx = 0
    var queryChar = normalizedQuery[queryIdx]
    var chunkFirstIdx = -1
    var chunkLastIdx = -2

    for (itemIdx in normalizedItem.indices) {
        if (normalizedItem[itemIdx] == queryChar) {
            if (itemIdx != chunkLastIdx + 1) {
                if (chunkFirstIdx >= 0) {
                    indices.add(chunkFirstIdx..chunkLastIdx)
                }
                chunkFirstIdx = itemIdx
            }
            chunkLastIdx = itemIdx
            queryIdx++
            if (queryIdx == normalizedQuery.length) {
                indices.add(chunkFirstIdx..chunkLastIdx)
                return scoreConsecutiveLetters(indices, normalizedItem)
            }
            queryChar = normalizedQuery[queryIdx]
        }
    }
    return null
}

private fun experimentalSmartFuzzyMatch(
    normalizedItem: String,
    normalizedQuery: String
): Pair<Double, List<IntRange>>? {
    val indices = mutableListOf<IntRange>()
    var queryIdx = 0
    var queryChar = normalizedQuery[queryIdx]
    var chunkFirstIdx: Int
    var chunkLastIdx = -1

    while (true) {
        val idx = normalizedItem.indexOf(queryChar, chunkLastIdx + 1)
        if (idx == -1) break

        if (idx == 0 || isValidWordBoundary(normalizedItem[idx - 1])) {
            chunkFirstIdx = idx
        } else {
            val queryCharsLeft = normalizedQuery.length - queryIdx
            val itemCharsLeft = normalizedItem.length - idx
            val minimumChunkLen = minOf(3, queryCharsLeft, itemCharsLeft)
            val minimumQueryChunk = normalizedQuery.substring(queryIdx, queryIdx + minimumChunkLen)
            if (normalizedItem.substring(idx, idx + minimumChunkLen) == minimumQueryChunk) {
                chunkFirstIdx = idx
            } else {
                chunkLastIdx++
                continue
            }
        }

        chunkLastIdx = chunkFirstIdx
        while (chunkLastIdx < normalizedItem.length && queryIdx < normalizedQuery.length && normalizedItem[chunkLastIdx] == normalizedQuery[queryIdx]) {
            chunkLastIdx++
            queryIdx++
        }
        indices.add(chunkFirstIdx until chunkLastIdx)

        if (queryIdx == normalizedQuery.length) {
            return scoreConsecutiveLetters(indices, normalizedItem)
        }
        queryChar = normalizedQuery[queryIdx]
        chunkLastIdx--
    }
    return null
}

private fun scoreConsecutiveLetters(
    indices: List<IntRange>,
    normalizedItem: String
): Pair<Double, List<IntRange>> {
    var score = 2.0
    for (range in indices) {
        val firstIdx = range.first
        val lastIdx = range.last
        val chunkLength = lastIdx - firstIdx + 1

        val isStartOfWord = firstIdx == 0 || normalizedItem[firstIdx] == ' ' || normalizedItem[firstIdx - 1] == ' '
        val isEndOfWord = lastIdx == normalizedItem.length - 1 || normalizedItem[lastIdx] == ' ' || (lastIdx + 1 < normalizedItem.length && normalizedItem[lastIdx + 1] == ' ')
        val isFullWord = isStartOfWord && isEndOfWord

        if (isFullWord) {
            score += 0.2
        } else if (isStartOfWord) {
            score += 0.4
        } else if (chunkLength >= 3) {
            score += 0.8
        } else {
            score += 1.6
        }
    }
    return score to indices
}
