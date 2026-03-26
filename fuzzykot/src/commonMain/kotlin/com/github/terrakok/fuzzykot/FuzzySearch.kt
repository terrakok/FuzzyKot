package com.github.terrakok.fuzzykot

/**
 * Calculates the similarity score between this string (as target) and the provided [query].
 *
 * @param query The query string to search for.
 * @param processor A function to pre-process strings before comparison.
 * @param scorer A function to calculate the similarity score between two strings (query and target). Defaults to [MicroFuzz.ratio].
 * @return The similarity score (0-100).
 */
fun String.ratio(
    query: String,
    processor: (String) -> String = { it },
    scorer: (String, String) -> Int = { s1, s2 -> MicroFuzz.ratio(s1, s2) },
): Int = scorer(processor(query), processor(this))

/**
 * Finds the matching ranges between this string (as target) and the provided [query].
 *
 * @param query The query string to search for.
 * @param processor A function to pre-process strings before comparison.
 * @param scorer A function to calculate the matching ranges between two strings (query and target). Defaults to [MicroFuzz.matchingRanges].
 * @return A list of [IntRange]s representing the matches in this string.
 */
fun String.matchingRanges(
    query: String,
    processor: (String) -> String = { it },
    scorer: (String, String) -> List<IntRange> = { s1, s2 -> MicroFuzz.matchingRanges(s1, s2) },
): List<IntRange> = scorer(processor(query), processor(this))

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
 * @param scorer A function to calculate the similarity score between two strings. Defaults to [MicroFuzz.ratio].
 * @param cutoff The minimum score to include in the results.
 * @return A list of [ExtractedResult]s.
 */
fun <T> Collection<T>.extractAll(
    query: String,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> MicroFuzz.ratio(s1, s2) },
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
 * @param scorer A function to calculate the similarity score between two strings. Defaults to [MicroFuzz.ratio].
 * @param cutoff The minimum score to include in the results.
 * @return A sorted list of [ExtractedResult]s.
 */
fun <T> Collection<T>.extractSorted(
    query: String,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> MicroFuzz.ratio(s1, s2) },
    cutoff: Int = 0
): List<ExtractedResult<T>> = extractAll(query, processor, scorer, cutoff).sortedDescending()

/**
 * Extracts the top [limit] items from the collection based on their similarity score.
 * Uses a [PriorityQueue] for efficient extraction of top elements.
 *
 * @param query The string to search for.
 * @param limit The maximum number of items to return.
 * @param processor A function to convert items to strings for comparison.
 * @param scorer A function to calculate the similarity score between two strings. Defaults to [MicroFuzz.ratio].
 * @param cutoff The minimum score to include in the results.
 * @return A list of the top [ExtractedResult]s.
 */
fun <T> Collection<T>.extractTop(
    query: String,
    limit: Int = Int.MAX_VALUE,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> MicroFuzz.ratio(s1, s2) },
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
 * @param scorer A function to calculate the similarity score between two strings. Defaults to [MicroFuzz.ratio].
 * @param cutoff The minimum score to include in the results.
 * @return The best [ExtractedResult], or null if no match meets the cutoff.
 */
fun <T> Collection<T>.extractOne(
    query: String,
    processor: (T) -> String = { it.toString() },
    scorer: (String, String) -> Int = { s1, s2 -> MicroFuzz.ratio(s1, s2) },
    cutoff: Int = 0
): ExtractedResult<T>? = extractAll(query, processor, scorer, cutoff).maxOrNull()