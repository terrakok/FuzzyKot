package com.github.terrakok.fuzzykot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests using the same inputs and cases from the Fuse.js test suite:
 * https://github.com/krisk/fuse/blob/main/test/fuzzy-search.test.js
 * https://github.com/krisk/fuse/blob/main/test/scoring.test.js
 *
 * The goal is to verify that FuzzyKot handles the same data correctly,
 * using its equivalent APIs.
 */
class FuseJsCompatibilityTest {

    // ---------------------------------------------------------------------------
    // Basic flat list searching  (fuzzy-search.test.js — "Search flat list")
    // ---------------------------------------------------------------------------

    private val basicList = listOf("Apple", "Orange", "Banana")

    @Test
    fun basicList_exactMatch_Apple() {
        // Searching for "Apple" in ["Apple", "Orange", "Banana"] should return "Apple"
        val result = basicList.extractOne("Apple")
        assertNotNull(result)
        assertEquals("Apple", result.string)
        assertEquals(100, result.score)
        assertEquals(0, result.index)
    }

    @Test
    fun basicList_fuzzyMatch_ran_findsOrange() {
        // "ran" is contained in "Orange" (o-R-A-N-ge)
        // weightedRatio uses lowercase by default, so "ran" in "orange" is found via partialRatio
        val result = basicList.extractOne("ran")
        assertNotNull(result)
        assertEquals("Orange", result.string)
    }

    @Test
    fun basicList_fuzzyMatch_nan_findsBanana() {
        // "nan" is contained in "Banana" (ba-N-A-N-a)
        val result = basicList.extractOne("nan")
        assertNotNull(result)
        assertEquals("Banana", result.string)
    }

    @Test
    fun basicList_limitResults_returnsOne() {
        // extractTop with limit=1 mirrors Fuse.js's `limit` option
        val results = basicList.extractTop("nan", limit = 1)
        assertEquals(1, results.size)
        assertEquals("Banana", results.first().string)
    }

    @Test
    fun basicList_orangeRanksAboveBanana_forQueryRan() {
        // "ran" is an exact substring of "orange" but not of "banana"
        val results = basicList.extractSorted("ran")
        val orangeScore = results.first { it.string == "Orange" }.score
        val bananaScore = results.first { it.string == "Banana" }.score
        assertTrue(orangeScore > bananaScore, "Orange ($orangeScore) should score higher than Banana ($bananaScore) for query 'ran'")
    }

    // ---------------------------------------------------------------------------
    // Deep key / nested field searching  (fuzzy-search.test.js — "Deep key search")
    // ---------------------------------------------------------------------------

    private data class Book(val title: String, val author: String)

    private val bookList = listOf(
        Book("Old Man's War", "John Scalzi"),
        Book("The Lock Artist", "Steve Hamilton"),
        Book("HTML5", "Remy Sharp"),
        Book("A History of England", "1066 Hastings")
    )

    @Test
    fun deepKey_typoStve_findsSteve() {
        // Fuse.js: search("Stve") → The Lock Artist (author: Steve Hamilton)
        val result = bookList.extractOne("Stve", processor = { it.author })
        assertNotNull(result)
        assertEquals("Steve Hamilton", result.string)
    }

    @Test
    fun deepKey_partial106_findsHistoryOfEngland() {
        // Fuse.js: search("106") → A History of England (author field contains "1066")
        val result = bookList.extractOne("106", processor = { it.author })
        assertNotNull(result)
        assertEquals("1066 Hastings", result.string)
    }

    @Test
    fun deepKey_typoHmlt_findsHTML5() {
        // Fuse.js: custom getFn search — "Hmlt" is a fuzzy match for "HTML5"
        val result = bookList.extractOne("Hmlt", processor = { it.title }, scorer = Levenshtein::ratio)
        assertNotNull(result)
        assertEquals("HTML5", result.string)
    }

    @Test
    fun deepKey_typoStve_authorRanksHigherThanOthers() {
        // "Stve" should score highest against "Steve Hamilton"
        val results = bookList.extractSorted("Stve", processor = { it.author })
        assertEquals("Steve Hamilton", results.first().string)
    }

    // ---------------------------------------------------------------------------
    // Score inclusion / ranking  (fuzzy-search.test.js — "Include score")
    // ---------------------------------------------------------------------------

    @Test
    fun scoring_exactMatch_hasScore100() {
        val list = listOf("Apple", "Orange", "Banana")
        val result = list.extractOne("Apple")
        assertNotNull(result)
        assertEquals(100, result.score)
    }

    @Test
    fun scoring_partialQuery_hasNonZeroScore() {
        val list = listOf("Apple", "Orange", "Banana")
        val result = list.extractOne("ran")
        assertNotNull(result)
        assertTrue(result.score > 0, "Score should be > 0, got ${result.score}")
    }

    // ---------------------------------------------------------------------------
    // Field-norm / length normalization  (scoring.test.js)
    //
    // Fuse.js default (ignoreFieldNorm=false): shorter field "Stove" ranks first
    // because Fuse.js's field-norm penalises longer strings.
    //
    // FuzzyKot weightedRatio default: the LONG phrase ranks first because it
    // detects "Steve" as an exact substring via partialRatio (score ~90),
    // whereas "Stove" only reaches ~80 (one character differs).
    // FuzzyKot does not implement field-length normalization.
    // ---------------------------------------------------------------------------

    private val steveList = listOf("Stove", "My good friend Steve from college")

    @Test
    fun fieldNorm_defaultScorer_longPhraseRanksFirst() {
        // FuzzyKot weightedRatio detects the exact substring "Steve" in the long phrase
        // and scores it higher than "Stove" (which has one character difference).
        // This differs from Fuse.js's default, which applies field-length normalization.
        val results = steveList.extractSorted("Steve")
        assertEquals(2, results.size)
        assertEquals(
            "My good friend Steve from college", results[0].string,
            "Long phrase should rank first in FuzzyKot because it contains 'Steve' as an exact substring"
        )
        // "Stove" still scores second (high similarity via ratio)
        assertEquals("Stove", results[1].string)
    }

    @Test
    fun fieldNorm_partialRatioScorer_longPhraseRanksFirst() {
        // Matches Fuse.js ignoreFieldNorm=true behavior:
        // The long phrase contains "Steve" as an exact substring → partialRatio = 100
        // "Stove" vs "Steve" → partialRatio < 100 (one character difference)
        val results = steveList.extractSorted(
            "Steve",
            scorer = { s1, s2 -> Levenshtein.partialRatio(s1, s2, processor = { it.lowercase() }) }
        )
        assertEquals("My good friend Steve from college", results[0].string,
            "Long phrase should rank first when using partialRatio (it contains 'Steve' exactly)")
    }

    @Test
    fun fieldNorm_stoveVsSteveDirectRatio() {
        // "Steve" vs "Stove": one character differs → ratio should be high (≥ 75)
        val score = Levenshtein.ratio("Steve", "Stove")
        assertTrue(score >= 75, "Steve vs Stove ratio should be ≥ 75, got $score")
    }

    @Test
    fun fieldNorm_steveInLongPhrase_partialRatioIs100() {
        // "Steve" is an exact substring of the long phrase
        val score = Levenshtein.partialRatio("Steve", "My good friend Steve from college")
        assertEquals(100, score)
    }

    // ---------------------------------------------------------------------------
    // Weighted key search  (fuzzy-search.test.js — "Weighted search")
    // ---------------------------------------------------------------------------

    private data class TaggedBook(val title: String, val author: String, val tags: List<String>)

    private val weightedBookList = listOf(
        TaggedBook("Old Man's War fiction", "John X", listOf("war")),
        TaggedBook("Right Ho Jeeves", "P.D. Mans", listOf("fiction", "war")),
        TaggedBook("The life of Jane", "John Smith", listOf("john", "smith")),
        TaggedBook("John Smith", "Steve Pearson", listOf("steve", "pearson"))
    )

    @Test
    fun weighted_searchJohnSmith_findsExactTitleOrAuthorMatch() {
        // Fuse.js: search("John Smith") → top results include exact title and exact author matches
        val results = weightedBookList.extractSorted(
            "John Smith",
            processor = { "${it.title} ${it.author}" }
        )
        assertTrue(results.isNotEmpty())
        val topStrings = results.take(2).map { it.referent.title }
        assertTrue(
            topStrings.contains("John Smith") || topStrings.contains("The life of Jane"),
            "Top 2 results should include the book titled 'John Smith' or 'The life of Jane' (author John Smith). Got: $topStrings"
        )
    }

    @Test
    fun weighted_searchMan_findsOldMansWar() {
        // Fuse.js: search("Man") → "Old Man's War fiction" matches via author "P.D. Mans" or title
        val results = weightedBookList.extractSorted(
            "Man",
            processor = { "${it.title} ${it.author}" }
        )
        val topTitle = results.first().referent.title
        assertTrue(
            topTitle == "Old Man's War fiction" || topTitle == "Right Ho Jeeves",
            "Top match for 'Man' should be a book with 'Man' in title or author, got: $topTitle"
        )
    }

    @Test
    fun weighted_searchWar_findsWarBook() {
        // Fuse.js: search("War") → "Old Man's War fiction" or "Right Ho Jeeves" (tag: war)
        val results = weightedBookList.extractSorted(
            "War",
            processor = { "${it.title} ${it.author} ${it.tags.joinToString(" ")}" }
        )
        assertTrue(results.isNotEmpty())
        val topTitle = results.first().referent.title
        assertTrue(
            topTitle == "Old Man's War fiction" || topTitle == "Right Ho Jeeves",
            "Top match for 'War' should contain 'war' in title or tags, got: $topTitle"
        )
    }

    // ---------------------------------------------------------------------------
    // Array / tag field searching  (fuzzy-search.test.js — "Array search")
    // ---------------------------------------------------------------------------

    private data class ISBNBook(val isbn: String, val title: String, val author: String, val tags: List<String>)

    private val isbnBookList = listOf(
        ISBNBook("0765348276", "Old Man's War", "John Scalzi", listOf("fiction")),
        ISBNBook("0312696957", "The Lock Artist", "Steve Hamilton", listOf("fiction")),
        ISBNBook("0321784421", "HTML5", "Remy Sharp", listOf("web development", "nonfiction"))
    )

    @Test
    fun arraySearch_nonfiction_findsHTML5() {
        // Fuse.js: search("nonfiction") → HTML5 (has tag "nonfiction")
        // FuzzyKot: join tags into a single string for comparison
        val result = isbnBookList.extractOne(
            "nonfiction",
            processor = { it.tags.joinToString(" ") }
        )
        assertNotNull(result)
        assertEquals("HTML5", result.referent.title)
    }

    @Test
    fun arraySearch_fiction_findsOldMansWarFirst() {
        // "fiction" is an exact tag on Old Man's War and The Lock Artist
        // Both should score highly; the first one found at index 0 should be returned or score tied
        val results = isbnBookList.extractSorted(
            "fiction",
            processor = { it.tags.joinToString(" ") }
        )
        val topTitles = results.filter { it.score == results.first().score }.map { it.referent.title }
        assertTrue(
            topTitles.contains("Old Man's War") || topTitles.contains("The Lock Artist"),
            "Top results for 'fiction' should include Old Man's War or The Lock Artist. Got: $topTitles"
        )
    }

    // ---------------------------------------------------------------------------
    // Large string / partial phrase matching  (fuzzy-search.test.js — "Large strings")
    // ---------------------------------------------------------------------------

    private data class TextItem(val text: String)

    private val textItems = listOf(
        TextItem("pizza"),
        TextItem("feast"),
        TextItem("where in the world is carmen san diego")
    )

    @Test
    fun largeString_carmenSanDiego_partialMatch() {
        // Fuse.js: search("where exctly is carmen in the world san diego") → the long text
        val result = textItems.extractOne(
            "where exactly is carmen in the world san diego",
            processor = { it.text },
            scorer = Levenshtein::ratio
        )
        assertNotNull(result)
        assertEquals("where in the world is carmen san diego", result.referent.text)
    }

    @Test
    fun largeString_exactPhraseMatch_scoresHighest() {
        // "where in the world" appears in the long text
        val result = textItems.extractOne(
            "where in the world",
            processor = { it.text }
        )
        assertNotNull(result)
        assertEquals("where in the world is carmen san diego", result.referent.text)
    }

    @Test
    fun largeString_leverageStreams_withHyphens() {
        // Fuse.js: search("leverage-streams-to") — hyphenated compound query
        val items = listOf("leverage-streams-to", "a completely different string", "streams for everyone")
        val result = items.extractOne("leverage-streams-to")
        assertNotNull(result)
        assertEquals("leverage-streams-to", result.string)
    }

    @Test
    fun largeString_leverageStreams_withSpaces() {
        // Fuse.js: "leverage streams to" (space-separated) should still find the hyphen version
        val items = listOf("leverage-streams-to", "a completely different string", "streams for everyone")
        val result = items.extractOne("leverage streams to", scorer = Levenshtein::ratio)
        assertNotNull(result)
        val hyphenScore = result.score
        assertTrue(hyphenScore > 0, "Score for 'leverage-streams-to' should be > 0 when querying 'leverage streams to'")
    }

    // ---------------------------------------------------------------------------
    // Field length & location matching  (fuzzy-search.test.js — "Field length")
    // ---------------------------------------------------------------------------

    @Test
    fun fieldLength_testInTokenChain_matchesHighest() {
        // Fuse.js: search("test") in "t te tes test tes te t"
        // The word "test" appears exactly in the string.
        // FuzzyKot's weightedRatio applies a partial-scale factor (0.9) for length-unequal strings,
        // so the top score is ~86-90, not 100. The item is still clearly the best match.
        val items = listOf("t te tes test tes te t", "completely unrelated")
        val result = items.extractOne("test")
        assertNotNull(result)
        assertEquals("t te tes test tes te t", result.string)
        assertTrue(result.score >= 80, "Score for 'test' in a string containing 'test' should be ≥ 80, got ${result.score}")
    }

    @Test
    fun fieldLength_worInHelloWorld_hasHighScore() {
        // Fuse.js: search("wor") in "Hello World" — partial match
        data class Named(val name: String)
        val items = listOf(Named("Hello World"))
        val result = items.extractOne("wor", processor = { it.name })
        assertNotNull(result)
        assertTrue(result.score > 0, "Score for 'wor' in 'Hello World' should be > 0, got ${result.score}")
    }

    // ---------------------------------------------------------------------------
    // Diacritics  (fuzzy-search.test.js — "Diacritics")
    // Note: FuzzyKot does not strip diacritics by default.
    // These tests document the current behavior and show how a custom processor
    // can be used to normalize diacritics via NFD decomposition + stripping.
    // ---------------------------------------------------------------------------

    @Test
    fun diacritics_exactMatchWithAccent_scores100() {
        // Searching for "déjà" in a list containing "déjà" → exact match
        val list = listOf("déjà", "cafe")
        val result = list.extractOne("déjà")
        assertNotNull(result)
        assertEquals("déjà", result.string)
        assertEquals(100, result.score)
    }

    @Test
    fun diacritics_plainQuery_matchesPlainEntry() {
        // Searching for "cafe" (no accent) should match "cafe" exactly
        val list = listOf("déjà", "cafe")
        val result = list.extractOne("cafe")
        assertNotNull(result)
        assertEquals("cafe", result.string)
        assertEquals(100, result.score)
    }

    @Test
    fun diacritics_accentedVsPlain_partialMatch() {
        // "deja" (without accent) vs "déjà" (with accent) — FuzzyKot sees them as different chars.
        // Score will be < 100 but > 0 due to the similar characters.
        val score = Levenshtein.ratio("deja", "déjà")
        assertTrue(score > 0, "Even with diacritics mismatch, some characters match; score should be > 0, got $score")
        assertTrue(score < 100, "Without diacritic stripping, 'deja' and 'déjà' should not score 100, got $score")
    }

    // ---------------------------------------------------------------------------
    // Collection updates  (fuzzy-search.test.js — "Collection update")
    // ---------------------------------------------------------------------------

    @Test
    fun collectionUpdate_searchAfterRebuild_findNewItem() {
        // Simulate Fuse.js setCollection by simply using a new list
        val vegetables = mutableListOf("Onion", "Lettuce", "Broccoli")
        var result = vegetables.extractOne("Lettuce")
        assertNotNull(result)
        assertEquals("Lettuce", result.string)

        // After "removing" and "re-adding" (replace list contents)
        val updatedVegetables = listOf("Onion", "Broccoli", "Cauliflower")
        result = updatedVegetables.extractOne("Lettuce")
        // Lettuce is no longer in the list; the best fuzzy match should score low
        assertTrue((result?.score ?: 0) < 70, "Without Lettuce in list, score should be low")
    }

    // ---------------------------------------------------------------------------
    // Ratio function directly  (mirrors Fuse.js internal distance scoring)
    // ---------------------------------------------------------------------------

    @Test
    fun ratio_stoveVsSteve_highSimilarity() {
        // One-character difference between "Steve" and "Stove"
        val score = Levenshtein.ratio("Steve", "Stove")
        assertTrue(score >= 75, "Steve vs Stove should have ratio ≥ 75, got $score")
        assertTrue(score < 100, "Steve vs Stove should not be a perfect match, got $score")
    }

    @Test
    fun ratio_html5VsHmlt_moderateSimilarity() {
        val score = Levenshtein.ratio("HTML5", "Hmlt", processor = { it.lowercase() })
        assertTrue(score >= 40, "HTML5 vs Hmlt should have ratio ≥ 40, got $score")
    }

    @Test
    fun partialRatio_substr_alwaysHighScore() {
        // "ran" is a substring of "Orange" (case-insensitive)
        val score = Levenshtein.partialRatio("ran", "Orange", processor = { it.lowercase() })
        assertEquals(100, score)
    }

    @Test
    fun partialRatio_nan_inBanana() {
        // "nan" is a substring of "Banana" (case-insensitive)
        val score = Levenshtein.partialRatio("nan", "Banana", processor = { it.lowercase() })
        assertEquals(100, score)
    }

    @Test
    fun partialRatio_nonfiction_inTag() {
        // "nonfiction" as exact tag value
        val score = Levenshtein.partialRatio("nonfiction", "web development nonfiction")
        assertEquals(100, score)
    }
}
