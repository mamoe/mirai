@file:Suppress("RemoveRedundantBackticks", "NonAsciiCharacters")

package net.mamoe.mirai.utils

import kotlinx.coroutines.*
import net.mamoe.mirai.test.shouldBeEqualTo
import net.mamoe.mirai.test.shouldBeTrue
import org.junit.Test
import kotlin.system.exitProcess
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@MiraiExperimentalAPI
internal class LockFreeLinkedListTest {
    init {
        GlobalScope.launch {
            delay(30 * 1000)
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

        list.size shouldBeEqualTo 4
    }

    @Test
    fun addAndGetConcurrent() = runBlocking {
        //withContext(Dispatchers.Default){
        val list = LockFreeLinkedList<Int>()

        list.concurrentAdd(1000, 10, 1)
        list.size shouldBeEqualTo 1000 * 10

        list.concurrentDo(100, 10) {
            remove(1).shouldBeTrue()
        }

        list.size shouldBeEqualTo 1000 * 10 - 100 * 10
        //}
    }

    @Test
    fun addAndGetMassConcurrentAccess() = runBlocking {
        val list = LockFreeLinkedList<Int>()

        val addJob = async { list.concurrentAdd(5000, 10, 1) }

        delay(10) // let addJob fly
        if (addJob.isCompleted) {
            error("Number of elements are not enough")
        }
        list.concurrentDo(1000, 10) {
            remove(1).shouldBeTrue()
        }
        addJob.join()

        list.size shouldBeEqualTo 5000 * 10 - 1000 * 10
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
    fun addAll() {
        val list = LockFreeLinkedList<Int>()
        list.addAll(listOf(1, 2, 3, 4, 5))
        list.size shouldBeEqualTo 5
    }

    @Test
    fun clear() {
        val list = LockFreeLinkedList<Int>()
        list.addAll(listOf(1, 2, 3, 4, 5))
        list.size shouldBeEqualTo 5
        list.clear()
        list.size shouldBeEqualTo 0
    }

    @UseExperimental(ExperimentalUnsignedTypes::class)
    @Test
    fun withInlineClassElements() {
        val list = LockFreeLinkedList<UInt>()
        list.addAll(listOf(1u, 2u, 3u, 4u, 5u))
        list.size shouldBeEqualTo 5

        list.toString() shouldBeEqualTo "[1, 2, 3, 4, 5]"
    }

    /*
    @Test
    fun indexOf() {
    val list: LockFreeLinkedList<Int> = lockFreeLinkedListOf(1, 2, 3, 3)
    assertEquals(0, list.indexOf(1))
    assertEquals(2, list.indexOf(3))

    assertEquals(-1, list.indexOf(4))
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

     */
}

@MiraiExperimentalAPI
internal suspend inline fun <E> LockFreeLinkedList<E>.concurrentAdd(numberOfCoroutines: Int, timesOfAdd: Int, element: E) =
    concurrentDo(numberOfCoroutines, timesOfAdd) { add(element) }

@MiraiExperimentalAPI
internal suspend inline fun <E : LockFreeLinkedList<*>> E.concurrentDo(numberOfCoroutines: Int, timesOfAdd: Int, crossinline todo: E.() -> Unit) =
    coroutineScope {
        repeat(numberOfCoroutines) {
            launch {
                repeat(timesOfAdd) {
                    todo()
                }
            }
        }
    }