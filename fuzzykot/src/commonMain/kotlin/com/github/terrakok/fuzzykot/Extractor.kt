package com.github.terrakok.fuzzykot

/**
 * Holds the result of a fuzzy search extraction.
 *
 * @param T The type of the original item.
 * @property referent The original item.
 * @property string The string representation of the item used for comparison.
 * @property score The similarity score (0-100).
 * @property index The index of the item in the original collection.
 */
data class ExtractedResult<T>(
    val referent: T,
    val string: String,
    val score: Int,
    val index: Int
) : Comparable<ExtractedResult<T>> {
    override fun compareTo(other: ExtractedResult<T>): Int = score.compareTo(other.score)
    override fun toString(): String = "(string: $string, score: $score, index: $index)"
}

/**
 * Extracts all items from the collection and calculates their similarity score to the query.
 *
 * @param query The string to search for.
 * @param processor A function to convert items to strings for comparison.
 * @param scorer A function to calculate the similarity score between two strings.
 * @param cutoff The minimum score to include in the results.
 * @return A list of [ExtractedResult]s.
 */
fun <T> Collection<T>.extractAll(
    query: String,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> s1.weightedRatio(s2) },
    cutoff: Int = 0
): List<ExtractedResult<T>> = this.mapIndexedNotNull { index, item ->
    val s = processor(item)
    val score = scorer(query, s)
    if (score >= cutoff) ExtractedResult(item, s, score, index) else null
}

/**
 * Extracts all items from the collection, calculates their similarity score, and returns them sorted by score descending.
 *
 * @param query The string to search for.
 * @param processor A function to convert items to strings for comparison.
 * @param scorer A function to calculate the similarity score between two strings.
 * @param cutoff The minimum score to include in the results.
 * @return A sorted list of [ExtractedResult]s.
 */
fun <T> Collection<T>.extractSorted(
    query: String,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> s1.weightedRatio(s2) },
    cutoff: Int = 0
): List<ExtractedResult<T>> = extractAll(query, processor, scorer, cutoff).sortedDescending()

/**
 * Extracts the top [limit] items from the collection based on their similarity score.
 * Uses a [PriorityQueue] for efficient extraction of top elements.
 *
 * @param query The string to search for.
 * @param limit The maximum number of items to return.
 * @param processor A function to convert items to strings for comparison.
 * @param scorer A function to calculate the similarity score between two strings.
 * @param cutoff The minimum score to include in the results.
 * @return A list of the top [ExtractedResult]s.
 */
fun <T> Collection<T>.extractTop(
    query: String,
    limit: Int = Int.MAX_VALUE,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> s1.weightedRatio(s2) },
    cutoff: Int = 0
): List<ExtractedResult<T>> {
    val all = extractAll(query, processor, scorer, cutoff)
    if (limit >= all.size) return all.sortedDescending()

    val pq = PriorityQueue<ExtractedResult<T>>(limit)
    for (res in all) {
        if (pq.size < limit) {
            pq.add(res)
        } else if (limit > 0 && res.score > pq.peek().score) {
            pq.poll()
            pq.add(res)
        }
    }
    return pq.sortedDescending()
}

/**
 * Extracts the single best match from the collection.
 *
 * @param query The string to search for.
 * @param processor A function to convert items to strings for comparison.
 * @param scorer A function to calculate the similarity score between two strings.
 * @param cutoff The minimum score to include in the results.
 * @return The best [ExtractedResult], or null if no match meets the cutoff.
 */
fun <T> Collection<T>.extractOne(
    query: String,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> s1.weightedRatio(s2) },
    cutoff: Int = 0
): ExtractedResult<T>? = extractAll(query, processor, scorer, cutoff).maxOrNull()
