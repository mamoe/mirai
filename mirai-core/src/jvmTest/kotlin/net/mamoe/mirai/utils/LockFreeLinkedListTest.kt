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
        list.addLast(1)
        list.addLast(2)
        list.addLast(3)
        list.addLast(4)

        list.size shouldBeEqualTo 4
    }

    @Test
    fun addAndGetConcurrent() = runBlocking {
        //withContext(Dispatchers.Default){
        val list = LockFreeLinkedList<Int>()

        list.concurrentDo(1000, 10) { addLast(1) }
        list.size shouldBeEqualTo 1000 * 10

        list.concurrentDo(100, 10) {
            remove(1).shouldBeTrue()
        }

        list.size shouldBeEqualTo 1000 * 10 - 100 * 10
        //}
    }

    @Test
    fun `so many concurrent add remove and foreach`() = runBlocking {
        val list = LockFreeLinkedList<Int>()

        val addJob = async { list.concurrentDo(2, 30000) { addLast(1) } }

        //delay(1) // let addJob fly
        if (addJob.isCompleted) {
            println("Number of elements are not enough")
        }
        val foreachJob = async {
            list.concurrentDo(1, 10000) {
                forEach { it + it }
            }
        }
        val removeLastJob = async {
            list.concurrentDo(1, 15000) {
                removeLast() shouldBeEqualTo 1
            }
        }
        val removeFirstJob = async {
            list.concurrentDo(1, 10000) {
                removeFirst() shouldBeEqualTo 1
            }
        }
        val addJob2 = async {
            list.concurrentDo(1, 5000) {
                addLast(1)
            }
        }
        val removeExactJob = launch {
            list.concurrentDo(3, 1000) {
                remove(1).shouldBeTrue()
            }
        }
        val filteringGetOrAddJob = launch {
            list.concurrentDo(1, 10000) {
                filteringGetOrAdd({ it == 2 }, { 1 })
            }
        }
        joinAll(addJob, addJob2, foreachJob, removeLastJob, removeFirstJob, removeExactJob, filteringGetOrAddJob)

        list.size shouldBeEqualTo 2 * 30000 - 1 * 15000 - 1 * 10000 + 1 * 5000 - 3 * 1000 + 1 * 10000
    }

    @Test
    fun removeWhileForeach() {
        val list = LockFreeLinkedList<Int>()
        repeat(10) { list.addLast(it) }
        list.forEach {
            list.remove(it + 1)
        }
        list.peekFirst() shouldBeEqualTo 0
    }

    @Test
    fun remove() {
        val list = LockFreeLinkedList<Int>()

        assertFalse { list.remove(1) }
        assertEquals(0, list.size)

        list.addLast(1)
        assertTrue { list.remove(1) }
        assertEquals(0, list.size)

        list.addLast(2)
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

    @Test
    fun `filteringGetOrAdd when add`() {
        val list = LockFreeLinkedList<Int>()
        list.addAll(listOf(1, 2, 3, 4, 5))
        val value = list.filteringGetOrAdd({ it == 6 }, { 6 })

        println("Check value")
        value shouldBeEqualTo 6
        println("Check size")
        println(list.getLinkStructure())
        list.size shouldBeEqualTo 6
    }

    @Test
    fun `filteringGetOrAdd when get`() {
        val list = LockFreeLinkedList<Int>()
        list.addAll(listOf(1, 2, 3, 4, 5))
        val value = list.filteringGetOrAdd({ it == 2 }, { 2 })

        println("Check value")
        value shouldBeEqualTo 2
        println("Check size")
        println(list.getLinkStructure())
        list.size shouldBeEqualTo 5
    }

    @Test
    fun `remove while foreach`() {
        val list = LockFreeLinkedList<Int>()
        list.addAll(listOf(1, 2, 3, 4, 5))

        list.forEach {
            list.remove(3)
        }

        list.toString() shouldBeEqualTo "[1, 2, 4, 5]"
    }

    @Test
    fun `filteringGetOrAdd when empty`() {
        val list = LockFreeLinkedList<Int>()
        val value = list.filteringGetOrAdd({ it == 2 }, { 2 })

        println("Check value")
        value shouldBeEqualTo 2
        println("Check size")
        println(list.getLinkStructure())
        list.size shouldBeEqualTo 1
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

@UseExperimental(ExperimentalCoroutinesApi::class)
@MiraiExperimentalAPI
internal suspend inline fun <E : LockFreeLinkedList<*>> E.concurrentDo(numberOfCoroutines: Int, times: Int, crossinline todo: E.() -> Unit) =
    coroutineScope {
        repeat(numberOfCoroutines) {
            launch(start = CoroutineStart.UNDISPATCHED) {
                repeat(times) {
                    todo()
                }
            }
        }
    }