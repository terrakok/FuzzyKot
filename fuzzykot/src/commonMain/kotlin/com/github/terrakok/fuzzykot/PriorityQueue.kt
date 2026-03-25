package com.github.terrakok.fuzzykot

internal class PriorityQueue<T>(size: Int, private val comparator: Comparator<T?>? = null) : Collection<T> {
    override var size: Int = 0
        private set

    private var arr: Array<T?> = arrayOfNulls<Any>(size) as Array<T?>

    fun add(element: T) {
        if (size + 1 >= arr.size) {
            resize()
        }
        arr[++size] = element
        swim(size)
    }

    fun peek(): T {
        if (size == 0) throw NoSuchElementException()
        return arr[1]!!
    }

    fun poll(): T {
        if (size == 0) throw NoSuchElementException()
        val res = peek()
        arr.swap(1, size--)
        sink(1)
        arr[size + 1] = null
        return res
    }

    private fun swim(n: Int) {
        var k = n
        while (k > 1 && greater(k / 2, k)) {
            arr.swap(k, k / 2)
            k /= 2
        }
    }

    private fun sink(n: Int) {
        var k = n
        while (2 * k <= size) {
            var j = 2 * k
            if (j < size && greater(j, j + 1)) j++
            if (!greater(k, j)) break
            arr.swap(k, j)
            k = j
        }
    }

    private fun greater(i: Int, j: Int): Boolean {
        return if (comparator != null) {
            comparator.compare(arr[i], arr[j]) > 0
        } else {
            val left = arr[i]!! as Comparable<T>
            left > arr[j]!!
        }
    }

    private fun resize() {
        arr = arr.copyOf(arr.size * 2)
    }

    override fun isEmpty(): Boolean = size == 0

    override fun contains(element: T): Boolean = any { it == element }

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }

    override fun iterator(): Iterator<T> = arr.copyOfRange(1, size + 1).map { it!! }.iterator()

    private fun <T> Array<T>.swap(i: Int, j: Int) {
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}
