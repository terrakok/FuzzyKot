package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.terrakok.fuzzykot.ExtractedResult
import com.github.terrakok.fuzzykot.extractSorted
import com.github.terrakok.fuzzykot.matchingRanges

private val quotes = listOf(
    "I'm going to make him an offer he can't refuse.",
    "May the Force be with you.",
    "You talking to me?",
    "I'll be back.",
    "Here's looking at you, kid.",
    "There's no place like home.",
    "I feel the need... the need for speed!",
    "Houston, we have a problem.",
    "You can't handle the truth!",
    "I'll have what she's having.",
    "Say 'hello' to my little friend!",
    "Bond. James Bond.",
    "Why so serious?",
    "I am your father.",
    "Elementary, my dear Watson.",
    "To be or not to be, that is the question.",
    "Life is like a box of chocolates.",
    "My precious.",
    "Hasta la vista, baby.",
    "I see dead people.",
    "Keep your friends close, but your enemies closer.",
    "A martini. Shaken, not stirred.",
    "Fasten your seatbelts. It's going to be a bumpy night.",
    "I love the smell of napalm in the morning.",
    "Show me the money!",
    "You're gonna need a bigger boat.",
    "Go ahead, make my day.",
    "Frankly, my dear, I don't give a damn.",
    "Toto, I've a feeling we're not in Kansas anymore.",
    "Here's Johnny!",
    "The first rule of Fight Club is: You do not talk about Fight Club.",
    "Wax on, wax off.",
    "That'll do, pig. That'll do.",
    "Just keep swimming.",
    "I'm king of the world!",
    "Snap out of it!",
    "My name is Maximus Decimus Meridius, commander of the Armies of the North.",
    "You're a wizard, Harry.",
    "With great power comes great responsibility.",
    "Magic Mirror on the wall, who is the fairest one of all?",
    "My name is Inigo Montoya. You killed my father. Prepare to die.",
    "Gentlemen, you can't fight in here! This is the War Room!",
    "I'm walking here! I'm walking here!",
    "Inconceivable!",
    "Rosebud.",
    "Play it again, Sam.",
    "Greed, for lack of a better word, is good.",
    "E.T. phone home.",
    "Listen to them. Children of the night. What music they make.",
    "Look at me. I'm the captain now.",
    "There is no spoon.",
    "The winter is coming.",
    "I drink your milkshake!",
    "I'm having a friend for dinner.",
    "I'll get you, my pretty, and your little dog too!",
    "You complete me.",
    "Every time a bell rings, an angel gets his wings.",
    "You had me at hello.",
    "What we've got here is failure to communicate.",
    "Love means never having to say you're sorry.",
    "They call me Mister Tibbs!",
    "It's alive! It's alive!",
    "I am Iron Man.",
    "Avengers, assemble!",
    "Live long and prosper.",
    "To infinity and beyond!",
    "Great Scott!",
    "Roads? Where we're going we don't need roads.",
    "Nobody puts Baby in a corner.",
    "Carpe diem. Seize the day, boys.",
    "Help me, Obi-Wan Kenobi. You're my only hope.",
    "One ring to rule them all.",
    "I am Groot.",
    "I'm the one who knocks!",
    "Life moves pretty fast.",
    "If you build it, he will come.",
    "After all, tomorrow is another day!",
    "As God is my witness, I'll never be hungry again.",
    "Of all the gin joints in all the towns in all the world, she walks into mine.",
    "Louis, I think this is the beginning of a beautiful friendship.",
    "Round up the usual suspects.",
    "We'll always have Paris.",
    "I am big! It's the pictures that got small.",
    "All right, Mr. DeMille, I'm ready for my close-up.",
    "Goldfinger: Do you expect me to talk? Bond: No, Mr. Bond, I expect you to die!",
    "You're terminal!",
    "D'oh!",
    "Bazinga!",
    "Stay hungry, stay foolish.",
    "Be the change you wish to see in the world.",
    "Knowledge is power.",
    "Fortune favors the bold.",
    "I think, therefore I am.",
    "That's one small step for man, one giant leap for mankind.",
    "Give me liberty, or give me death!",
    "The only thing we have to fear is fear itself.",
    "Ask not what your country can do for you; ask what you can do for your country.",
    "I have a dream.",
    "Veni, vidi, vici.",
    "Eureka!",
    "Game over, man! Game over!",
    "Get away from her, you bitch!"
)

@Composable
fun App() {
    var query by remember { mutableStateOf("") }
    val results by remember(query) {
        derivedStateOf {
            if (query.isBlank()) {
                quotes.mapIndexed { index, s ->
                    ExtractedResult(s, s, 100, index)
                }
            } else {
                quotes.extractSorted(query = query, cutoff = 25)
            }
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .windowInsetsPadding(WindowInsets.systemBars),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp)
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    placeholder = {
                        Text(
                            "Search quotes...",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )

                if (results.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE0E0E0))
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(results) { res ->
                            QuoteItem(res.string, query, res.score)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteItem(text: String, query: String, score: Int) {
    val annotatedString = remember(text, query) {
        if (query.isBlank()) {
            AnnotatedString(text)
        } else {
            buildAnnotatedString {
                val tokens = query.lowercase().split("\\s+".toRegex()).filter { it.length >= 2 }
                val lowerText = text.lowercase()
                val lowerQuery = query.lowercase()

                val matchIndices = mutableListOf<IntRange>()

                // Find query as a whole
                var startIndex = lowerText.indexOf(lowerQuery)
                while (startIndex != -1) {
                    matchIndices.add(startIndex until (startIndex + lowerQuery.length))
                    startIndex = lowerText.indexOf(lowerQuery, startIndex + 1)
                }

                // Also find tokens
                for (token in tokens) {
                    var tIndex = lowerText.indexOf(token)
                    while (tIndex != -1) {
                        matchIndices.add(tIndex until (tIndex + token.length))
                        tIndex = lowerText.indexOf(token, tIndex + 1)
                    }
                }

                // Find fuzzy matches
                matchIndices.addAll(lowerQuery.matchingRanges(lowerText))
                for (token in tokens) {
                    matchIndices.addAll(token.matchingRanges(lowerText))
                }

                // Sort and merge overlapping ranges
                val mergedMatches = matchIndices.mergeRanges()

                var lastIndex = 0
                for (range in mergedMatches) {
                    if (range.first > lastIndex) {
                        append(text.substring(lastIndex, range.first))
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5),
                            background = Color.Yellow
                        )
                    ) {
                        append(text.substring(range.first, range.last + 1))
                    }
                    lastIndex = range.last + 1
                }
                if (lastIndex < text.length) {
                    append(text.substring(lastIndex))
                }
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (query.isNotBlank()) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.5f),
            )
        }
    }
}

private fun List<IntRange>.mergeRanges(): List<IntRange> {
    if (isEmpty()) return emptyList()
    val sorted = sortedBy { it.first }
    val result = mutableListOf<IntRange>()
    var current = sorted[0]
    for (i in 1 until sorted.size) {
        val next = sorted[i]
        if (next.first <= current.last + 1) {
            current = current.first..maxOf(current.last, next.last)
        } else {
            result.add(current)
            current = next
        }
    }
    result.add(current)
    return result
}