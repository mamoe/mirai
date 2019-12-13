@file:Suppress("RemoveRedundantBackticks", "NonAsciiCharacters")

package net.mamoe.mirai.utils

import kotlinx.coroutines.*
import net.mamoe.mirai.test.shouldBeEqualTo
import net.mamoe.mirai.test.shouldBeFalse
import net.mamoe.mirai.test.shouldBeTrue
import org.junit.Test
import kotlin.system.exitProcess
import kotlin.test.*

internal class LockFreeLinkedListTest {
    init {
        GlobalScope.launch {
            delay(5000)
            exitProcess(-100)
        }
    }

    @Test
    fun addAndGetSingleThreaded() {
        val list = LockFreeLinkedList<Int>()
        list.add(1)
        list.add(2)
        list.add(3)
        list.add(4)

        assertEquals(list[0], 1, "Failed on list[0]")
        assertEquals(list[1], 2, "Failed on list[1]")
        assertEquals(list[2], 3, "Failed on list[2]")
        assertEquals(list[3], 4, "Failed on list[3]")
    }

    @Test
    fun addAndGetSingleConcurrent() {
        val list = LockFreeLinkedList<Int>()
        val add = GlobalScope.async { list.concurrentAdd(1000, 10, 1) }
        val remove = GlobalScope.async {
            add.join()
            list.concurrentDo(100, 10) {
                remove(1)
            }
        }
        runBlocking {
            joinAll(add, remove)
        }
        assertEquals(1000 * 10 - 100 * 10, list.size)
    }

    @Test
    fun remove() {
        val list = LockFreeLinkedList<Int>()

        assertFalse { list.remove(1) }
        assertEquals(0, list.size)

        list.add(1)
        assertTrue { list.remove(1) }
        assertEquals(0, list.size)

        list.add(2)
        assertFalse { list.remove(1) }
        assertEquals(1, list.size)
    }

    @Test
    fun getSize() {
        val list = lockFreeLinkedListOf(1, 2, 3, 4, 5)
        assertEquals(5, list.size)

        val list2 = lockFreeLinkedListOf<Unit>()
        assertEquals(0, list2.size)
    }

    @Test
    fun contains() {
        val list = lockFreeLinkedListOf<Int>()
        assertFalse { list.contains(0) }

        list.add(0)
        assertTrue { list.contains(0) }
    }

    @Test
    fun containsAll() {
        var list = lockFreeLinkedListOf(1, 2, 3)
        assertTrue { list.containsAll(listOf(1, 2, 3)) }
        assertTrue { list.containsAll(listOf()) }

        list = lockFreeLinkedListOf(1, 2)
        assertFalse { list.containsAll(listOf(1, 2, 3)) }

        list = lockFreeLinkedListOf()
        assertTrue { list.containsAll(listOf()) }
        assertFalse { list.containsAll(listOf(1)) }
    }

    @Test
    fun indexOf() {
        val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(1, 2, 3, 3)
        assertEquals(0, list.indexOf(1))
        assertEquals(2, list.indexOf(3))

        assertEquals(-1, list.indexOf(4))
    }

    @Test
    fun isEmpty() {
        val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf()
        list.isEmpty().shouldBeTrue()

        list.add(1)
        list.isEmpty().shouldBeFalse()
    }

    @Test
    fun iterator() {
        var list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(2)
        list.forEach {
            it shouldBeEqualTo 2
        }

        list = lockFreeLinkedListOf(1, 2)
        list.joinToString { it.toString() } shouldBeEqualTo "1, 2"


        list = lockFreeLinkedListOf(1, 2)
        val iterator = list.iterator()
        iterator.remove()
        var reached = false
        for (i in iterator) {
            i shouldBeEqualTo 2
            reached = true
        }
        reached shouldBeEqualTo true

        list.joinToString { it.toString() } shouldBeEqualTo "2"
        iterator.remove()
        assertFailsWith<NoSuchElementException> { iterator.remove() }
    }

    @Test
    fun `lastIndexOf of exact 1 match at first`() {
        val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(2, 1)
        list.lastIndexOf(2) shouldBeEqualTo 0
    }

    @Test
    fun `lastIndexOf of exact 1 match`() {
        val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(1, 2)
        list.lastIndexOf(2) shouldBeEqualTo 1
    }

    @Test
    fun `lastIndexOf of multiply matches`() {
        val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(1, 2, 2)
        list.lastIndexOf(2) shouldBeEqualTo 2
    }

    @Test
    fun `lastIndexOf of no match`() {
        val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(2)
        list.lastIndexOf(3) shouldBeEqualTo -1
    }

    @Test
    fun `lastIndexOf of many elements`() {
        val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(1, 4, 2, 3, 4, 5)
        list.lastIndexOf(4) shouldBeEqualTo 4
    }


/*
    companion object{
        @JvmStatic
        fun main(vararg args: String) {
            LockFreeLinkedListTest().`lastIndexOf of many elements`()
        }
    }*/

    @Test
    fun listIterator() {
    }

    @Test
    fun testListIterator() {
    }

    @Test
    fun subList() {
    }

    @Test
    fun testAdd() {
    }

    @Test
    fun addAll() {
    }

    @Test
    fun testAddAll() {
    }

    @Test
    fun clear() {
    }

    @Test
    fun removeAll() {
    }

    @Test
    fun removeAt() {
    }

    @Test
    fun retainAll() {
    }

    @Test
    fun set() {
    }
}

internal fun withTimeoutBlocking(timeout: Long = 500L, block: suspend () -> Unit) = runBlocking { withTimeout(timeout) { block() } }

internal suspend fun <E> LockFreeLinkedList<E>.concurrentAdd(numberOfCoroutines: Int, timesOfAdd: Int, element: E) =
    concurrentDo(numberOfCoroutines, timesOfAdd) { add(element) }

internal suspend fun <E : LockFreeLinkedList<*>> E.concurrentDo(numberOfCoroutines: Int, timesOfAdd: Int, todo: E.() -> Unit) = coroutineScope {
    repeat(numberOfCoroutines) {
        launch {
            repeat(timesOfAdd) {
                todo()
            }
        }
    }
}