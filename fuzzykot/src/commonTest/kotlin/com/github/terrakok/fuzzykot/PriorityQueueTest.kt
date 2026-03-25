package com.github.terrakok.fuzzykot

import kotlin.test.*

class PriorityQueueTest {

    @Test
    fun testBasicOperations() {
        val pq = PriorityQueue<Int>(5)
        assertTrue(pq.isEmpty())
        assertEquals(0, pq.size)

        pq.add(10)
        assertFalse(pq.isEmpty())
        assertEquals(1, pq.size)
        assertEquals(10, pq.peek())

        pq.add(5)
        assertEquals(2, pq.size)
        assertEquals(5, pq.peek()) // Min-heap: 5 < 10

        pq.add(15)
        assertEquals(3, pq.size)
        assertEquals(5, pq.peek())

        assertEquals(5, pq.poll())
        assertEquals(2, pq.size)
        assertEquals(10, pq.peek())

        assertEquals(10, pq.poll())
        assertEquals(1, pq.size)
        assertEquals(15, pq.peek())

        assertEquals(15, pq.poll())
        assertTrue(pq.isEmpty())
        assertEquals(0, pq.size)
    }

    @Test
    fun testEmptyQueueExceptions() {
        val pq = PriorityQueue<String>(5)
        assertFailsWith<NoSuchElementException> { pq.peek() }
        assertFailsWith<NoSuchElementException> { pq.poll() }
    }

    @Test
    fun testPriorityOrdering() {
        val pq = PriorityQueue<Int>(10)
        val elements = listOf(10, 20, 5, 15, 30, 25)
        elements.forEach { pq.add(it) }

        val result = mutableListOf<Int>()
        while (!pq.isEmpty()) {
            result.add(pq.poll())
        }

        assertEquals(listOf(5, 10, 15, 20, 25, 30), result)
    }

    @Test
    fun testCustomComparator() {
        // Max-heap using custom comparator
        val pq = PriorityQueue<Int>(10, Comparator { a, b -> b!! - a!! })
        val elements = listOf(10, 20, 5, 15, 30, 25)
        elements.forEach { pq.add(it) }

        val result = mutableListOf<Int>()
        while (!pq.isEmpty()) {
            result.add(pq.poll())
        }

        assertEquals(listOf(30, 25, 20, 15, 10, 5), result)
    }

    @Test
    fun testResizing() {
        val pq = PriorityQueue<Int>(2)
        for (i in 10 downTo 1) {
            pq.add(i)
        }
        assertEquals(10, pq.size)
        assertEquals(1, pq.peek())

        val result = mutableListOf<Int>()
        while (!pq.isEmpty()) {
            result.add(pq.poll())
        }
        assertEquals((1..10).toList(), result)
    }

    @Test
    fun testContainsAndContainsAll() {
        val pq = PriorityQueue<String>(5)
        pq.add("apple")
        pq.add("banana")
        pq.add("cherry")

        assertTrue(pq.contains("apple"))
        assertTrue(pq.contains("banana"))
        assertTrue(pq.contains("cherry"))
        assertFalse(pq.contains("date"))

        assertTrue(pq.containsAll(listOf("apple", "cherry")))
        assertFalse(pq.containsAll(listOf("apple", "date")))
    }

    @Test
    fun testIterator() {
        val pq = PriorityQueue<Int>(10)
        val elements = listOf(10, 20, 5)
        elements.forEach { pq.add(it) }

        val iterated = pq.iterator().asSequence().toList().sorted()
        assertEquals(listOf(5, 10, 20), iterated)
    }

    data class Item(val name: String, val priority: Int) : Comparable<Item> {
        override fun compareTo(other: Item): Int = priority.compareTo(other.priority)
    }

    @Test
    fun testCustomObjects() {
        val pq = PriorityQueue<Item>(5)
        pq.add(Item("low", 10))
        pq.add(Item("high", 1))
        pq.add(Item("medium", 5))

        assertEquals("high", pq.poll().name)
        assertEquals("medium", pq.poll().name)
        assertEquals("low", pq.poll().name)
    }

    @Test
    fun testDuplicates() {
        val pq = PriorityQueue<Int>(5)
        pq.add(10)
        pq.add(10)
        pq.add(5)

        assertEquals(3, pq.size)
        assertEquals(5, pq.poll())
        assertEquals(10, pq.poll())
        assertEquals(10, pq.poll())
        assertTrue(pq.isEmpty())
    }
}
