```text
                                        
▗▄▄▄▖                    ▗▖ ▄▖          
▐▛▀▀▘                    ▐▌▐▛       ▐▌  
▐▌   ▐▌ ▐▌▐███▌▐███▌▝█ █▌▐▙█   ▟█▙ ▐███ 
▐███ ▐▌ ▐▌  ▗▛   ▗▛  █▖█ ▐██  ▐▛ ▜▌ ▐▌  
▐▌   ▐▌ ▐▌ ▗▛   ▗▛   ▐█▛ ▐▌▐▙ ▐▌ ▐▌ ▐▌  
▐▌   ▐▙▄█▌▗█▄▄▖▗█▄▄▖  █▌ ▐▌ █▖▝█▄█▘ ▐▙▄ 
▝▘    ▀▀▝▘▝▀▀▀▘▝▀▀▀▘  █  ▝▘ ▝▘ ▝▀▘   ▀▀ 
                     █▌                 
                                        
```

Fuzzy string matching for Kotlin Multiplatform.

![Sample App](media/screen.png)

**Web demo:** https://terrakok.github.io/FuzzyKot/

FuzzyKot is designed to bring powerful and flexible fuzzy string matching to Kotlin Multiplatform projects. Whether you
are building a search feature, data cleaning pipeline, or any application requiring robust string comparison, FuzzyKot
provides a set of algorithms to handle typos, variations in word order, and partial matches with ease.

### Why FuzzyKot?

- **Multiplatform:** Support for Android, iOS, JVM, JS, Wasm, macOS, Linux, and Windows.
- **Zero Dependencies:** Pure Kotlin implementation without any external dependencies, keeping your project lightweight.
- **Versatile Algorithms:** Includes Simple Ratio, Partial Ratio, Token Sort/Set Ratios, and Weighted Ratio.
- **Easy Extraction:** High-level API to extract the best matches from collections.
- **Performance:** Optimized for speed and low memory footprint.

## Setup

```kotlin
implementation("com.github.terrakok:fuzzykot:[todo]")
```

## Usage

FuzzyKot provides easy-to-use APIs for fuzzy matching and extraction.

### FuzzySearch

The simplest way to use FuzzyKot is through the `FuzzySearch` extension functions. These functions use `MicroFuzz` as the default scoring algorithm.

#### Basic Matching

```kotlin
import com.github.terrakok.fuzzykot.*

// Get similarity score (0-100)
val score = "Kotlin is awesome".ratio("kotlin")
println("Score: $score") // 97

// Get matching ranges for highlighting
val ranges = "Kotlin is awesome".matchingRanges("kotlin")
println("Ranges: $ranges") // [0..5]
```

#### Extraction from Collections

```kotlin
val choices = listOf("Kotlin", "Java", "Swift", "Python")

// Get the top 2 matches
val results = choices.extractTop("KOTLIN", limit = 2)

results.forEach { 
    println("${it.referent}: ${it.score}") 
}
// Output:
// Kotlin: 100
```

**Available extraction methods:**
- `extractAll(query, processor, scorer, cutoff)`
- `extractSorted(query, processor, scorer, cutoff)`
- `extractTop(query, limit, processor, scorer, cutoff)`
- `extractOne(query, processor, scorer, cutoff)`

### Levenshtein

For more traditional fuzzy matching algorithms based on Levenshtein distance, use the `Levenshtein` object.

#### Ratios

```kotlin
import com.github.terrakok.fuzzykot.Levenshtein

Levenshtein.ratio("myself", "me") // 50
Levenshtein.partialRatio("similar", "sim") // 100
Levenshtein.tokenSortRatio("order word", "word order") // 100
Levenshtein.tokenSetRatio("fuzzy fuzzy matching", "fuzzy matching") // 100
```

#### Available Algorithms

- `ratio`: Simple Levenshtein distance ratio.
- `partialRatio`: Best match of shorter string within longer string. Useful for finding substrings.
- `tokenSortRatio`: Sorts tokens alphabetically before comparing. Handles variations in word order.
- `tokenSetRatio`: Compares sets of tokens. Handles repeated words and variations in word order.
- `weightedRatio`: A weighted combination of multiple ratios for a more balanced score.
- `matchingRanges`: Finds the matching character ranges in the target string.

### MicroFuzz

You can also use the `MicroFuzz` object directly, which provides a fast and effective fuzzy matching algorithm designed for search-as-you-type interfaces.

```kotlin
import com.github.terrakok.fuzzykot.MicroFuzz

val score = MicroFuzz.ratio("FuzzyKot", "fzkot") // 88
```

## Inspiration

This library is inspired by [microfuzz](https://github.com/Nozbe/microfuzz) and the Levenshtein distance algorithm.

## License

MIT License

Copyright (c) 2026 Konstantin Tskhovrebov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

