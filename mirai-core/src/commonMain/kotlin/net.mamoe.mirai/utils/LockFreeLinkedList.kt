@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.utils.Node.Companion.equals


@MiraiExperimentalAPI
inline fun <E> lockFreeLinkedListOf(vararg elements: E): LockFreeLinkedList<E> = LockFreeLinkedList<E>().apply {
    addAll(elements)
}

@MiraiExperimentalAPI
inline fun <E> lockFreeLinkedListOf(): LockFreeLinkedList<E> = LockFreeLinkedList<E>()

/**
 * Implementation of lock-free LinkedList.
 *
 * Modifying can be performed concurrently.
 * Iterating concurrency is guaranteed. // TODO: 2019/12/13 ADD MORE
 */
@MiraiExperimentalAPI
class LockFreeLinkedList<E> : MutableList<E>, RandomAccess {
    private val tail: Tail<E> = Tail()

    private val head: Head<E> = Head(tail)

    override fun add(element: E): Boolean {
        val node = element.asNode(tail)

        while (true) {
            val tail = head.iterateBeforeFirst { it === tail } // find the last node.
            if (tail.nextNodeRef.compareAndSet(this.tail, node)) { // ensure the last node is the last node
                return true
            }
        }


    }

    internal fun getLinkStucture(): String = buildString {
        head.childIterateReturnsLastSatisfying<Node<*>>({
            append(it.toString())
            append("->")
            it.nextNode
        }, { it !is Tail })
    }.let {
        if (it.lastIndex > 0) {
            it.substring(0..it.lastIndex - 2)
        } else it
    }

    override fun remove(element: E): Boolean {
        while (true) {
            val before = head.iterateBeforeNodeValue(element)
            val toRemove = before.nextNode
            val next = toRemove.nextNode
            if (toRemove === tail) {
                return false
            }

            if (before.nextNodeRef.compareAndSet(toRemove, next)) {
                return true
            }
        }
    }

    private fun removeNode(node: Node<E>): Boolean {
        if (node == tail) {
            return false
        }
        while (true) {
            val before = head.iterateBeforeFirst { it === node }
            val toRemove = before.nextNode
            val next = toRemove.nextNode
            if (toRemove == tail) { // This
                return true
            }
            toRemove.nodeValue = null // logaically remove first, then all the operations will recognize this node invalid

            if (before.nextNodeRef.compareAndSet(toRemove, next)) { // physically remove: try to fix the link
                return true
            }
        }
    }

    override val size: Int
        get() = head.countChildIterate<Node<E>>({ it.nextNodeRef.value }, { it !is Tail }) - 1 // empty head is always included

    override operator fun contains(element: E): Boolean = head.iterateBeforeNodeValue(element) !== tail

    override fun containsAll(elements: Collection<E>): Boolean = elements.all { contains(it) }

    override operator fun get(index: Int): E {
        require(index >= 0) { "Index must be >= 0" }
        var i = index + 1 // 1 for head
        return head.iterateStopOnFirst { i-- == 0 }.nodeValueRef.value ?: noSuchElement()
    }

    override fun indexOf(element: E): Int {
        var i = -1 // head
        if (!head.iterateStopOnFirst {
                i++
                it.nodeValueRef.value == element
            }.isValidElementNode()) {
            return -1
        }
        return i - 1 // iteration is stopped at the next node
    }

    override fun isEmpty(): Boolean = head.allMatching { it.nodeValueRef.value == null }

    /**
     * Create a concurrent-unsafe iterator
     */
    override operator fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
        var currentNode: Node<E>
            get() = currentNoderef.value
            set(value) {
                currentNoderef.value = value
            }

        private val currentNoderef: AtomicRef<Node<E>> = atomic(head) // concurrent compatibility

        /**
         * Check if
         *
         * **Notice That:**
         * if `hasNext` returned `true`, then the last remaining element is removed concurrently,
         * [next] will produce a [NoSuchElementException]
         */
        override fun hasNext(): Boolean = !currentNode.iterateStopOnFirst { it.isValidElementNode() }.isTail()

        /**
         * Iterate until the next node is not
         */
        override fun next(): E {
            while (true) {
                val next = currentNode.nextNode
                if (next.isTail()) noSuchElement()

                currentNode = next

                val nodeValue = next.nodeValue
                if (nodeValue != null) { // next node is not removed, that's what we want
                    return nodeValue
                } // or try again
            }
        }

        override fun remove() {
            if (!removeNode(currentNode)) { // search from head onto the node, concurrent compatibility
                noSuchElement()
            }
        }
    }

    /**
     * Find the last index of the element in the list that is [equals] to [element], with concurrent compatibility.
     *
     * For a typical list, say `head <- Node#1(1) <- Node#2(2) <- Node#3(3) <- Node#4(4) <- Node#5(2) <- tail`,
     * the procedures of `lastIndexOf(2)` is:
     *
     * 1. Iterate each element, until 2 is found, accumulate the index found, which is 1
     * 2. Search again from the first matching element, which is Node#2
     * 3. Accumulate the index found.
     * 4. Repeat 2,3 until the `tail` is reached.
     *
     * Concurrent changes may influence the result.
     * While searching,
     *
     */
    override fun lastIndexOf(element: E): Int {
        var lastMatching: Node<E> = head
        var searchStartingFrom: Node<E> = lastMatching
        var index = 0 // accumulated index from each search

        findTheLastMatchingElement@ while (true) { // iterate to find the last matching element.
            var timesOnThisTurn = if (searchStartingFrom === head) -1 else 0 // ignore the head
            val got = searchStartingFrom.nextNode.iterateBeforeFirst { timesOnThisTurn++; it.nodeValue == element }
            // find the first match starting from `searchStartingFrom`

            if (got.isTail()) break@findTheLastMatchingElement // no further elements
            check(timesOnThisTurn >= 0) { "Internal check failed: too many times ran: $timesOnThisTurn" }

            searchStartingFrom = got.nextNode
            index += timesOnThisTurn

            if (!got.isRemoved()) lastMatching = got //record the lastMatching only if got is not removed.
        }

        if (!lastMatching.isValidElementNode()) {
            // found is invalid means not found
            return -1
        }

        return index
    }

    override fun listIterator(): MutableListIterator<E> = listIterator0(0)
    override fun listIterator(index: Int): MutableListIterator<E> = listIterator0(index)

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun listIterator0(index: Int): MutableListIterator<E> {
        TODO()
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun add(index: Int, element: E) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { add(it) }
        return true
    }

    override fun clear() {
        head.nextNode = tail

        // TODO: 2019/12/13 check ?
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAt(index: Int): E {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override operator fun set(index: Int, element: E): E {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    // NO INLINE: currently exceptions thrown in a inline function cannot be traced
    private fun noSuchElement(): Nothing = throw NoSuchElementException()

}

@Suppress("NOTHING_TO_INLINE")
private inline fun <E> E.asNode(nextNode: Node<E>): Node<E> = Node(nextNode).apply { nodeValueRef.value = this@asNode }

/**
 * Self-iterate using the [iterator], until [mustBeTrue] returns `false`.
 * Returns the element at the last time when the [mustBeTrue] returns `true`
 */
private inline fun <N : Node<*>> N.childIterateReturnsLastSatisfying(iterator: (N) -> N, mustBeTrue: (N) -> Boolean): N {
    if (!mustBeTrue(this)) return this
    var value: N = this

    while (true) {
        val newValue = iterator(value)
        if (mustBeTrue(newValue)) {
            value = newValue
        } else {
            return value
        }

        if (newValue is Tail<*>) return newValue
    }
}

/**
 * Self-iterate using the [iterator], until [mustBeTrue] returns `false`.
 * Returns the element at the first time when the [mustBeTrue] returns `false`
 */
private inline fun <E> E.childIterateReturnFirstUnsitisfying(iterator: (E) -> E, mustBeTrue: (E) -> Boolean): E {
    if (!mustBeTrue(this)) return this
    var value: E = this

    while (true) {
        val newValue = iterator(value)
        if (mustBeTrue(newValue)) {
            value = newValue
        } else {
            return newValue
        }

        if (newValue is Tail<*>) return newValue
    }
}

/**
 * Self-iterate using the [iterator], until [mustBeTrue] returns `false`.
 * Returns the count of elements being iterated.
 */
private inline fun <E> E.countChildIterate(iterator: (E) -> E, mustBeTrue: (E) -> Boolean): Int {
    var count = 0
    var value: E = this
    if (!mustBeTrue(value)) return count

    while (true) {
        count++
        val newValue = iterator(value)
        if (mustBeTrue(newValue)) {
            value = newValue
        } else {
            return count
        }
    }
}

private class Head<E>(
    nextNode: Node<E>
) : Node<E>(nextNode) {

}

private open class Node<E>(
    nextNode: Node<E>?
) {
    internal val id: Int = nextId();

    companion object {
        private val idCount = atomic(0)
        internal fun nextId() = idCount.getAndIncrement()
    }

    override fun toString(): String = "Node#$id(${nodeValueRef.value})"


    val nodeValueRef: AtomicRef<E?> = atomic(null)

    /**
     * Short cut for accessing [nodeValueRef]
     */
    inline var nodeValue: E?
        get() = nodeValueRef.value
        set(value) {
            nodeValueRef.value = value
        }

    @Suppress("LeakingThis")
    val nextNodeRef: AtomicRef<Node<E>> = atomic(nextNode ?: this)

    /**
     * Short cut for accessing [nextNodeRef]
     */
    inline var nextNode: Node<E>
        get() = nextNodeRef.value
        set(value) {
            nextNodeRef.value = value
        }


    /**
     * Returns the former node of the last node whence [filter] returnes true
     */
    inline fun iterateBeforeFirst(filter: (Node<E>) -> Boolean): Node<E> =
        this.childIterateReturnsLastSatisfying<Node<E>>({ it.nextNode }, { !filter(it) })

    /**
     * Returns the last node whence [filter] returnes true.
     */
    inline fun iterateStopOnFirst(filter: (Node<E>) -> Boolean): Node<E> =
        iterateBeforeFirst(filter).nextNode


    /**
     * Returns the former node of next node whose value is not null.
     * [Tail] is returned if no node is found
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun iterateBeforeNotnull(): Node<E> = iterateBeforeFirst { it.nodeValue != null }

    /**
     * Returns the next node which is not [Tail] or [Head] and with a notnull value.
     * [Tail] is returned if no node is found
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun nextValidElement(): Node<E> = this.iterateBeforeFirst { !it.isValidElementNode() }

    /**
     * Returns the next node whose value is not null.
     * [Tail] is returned if no node is found
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun nextNotnull(): Node<E> = this.iterateBeforeFirst { it.nodeValueRef.value == null }

    /**
     * Check if all the node which is not [Tail] matches the [condition]
     *
     * Head, which is this, is also being tested.
     * [Tail], is not being tested.
     */
    inline fun allMatching(condition: (Node<E>) -> Boolean): Boolean = this.childIterateReturnsLastSatisfying<Node<E>>({ it.nextNode }, condition) !is Tail

    /**
     * Stop on and returns the former element of the element that is [equals] to the [element]
     *
     * E.g.: for `head <- 1 <- 2 <- 3 <- tail`, `iterateStopOnNodeValue(2)` returns the node whose value is 1
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun iterateBeforeNodeValue(element: E): Node<E> = this.iterateBeforeFirst { it.nodeValueRef.value == element }

    /**
     * Stop on and returns the element that is [equals] to the [element]
     *
     * E.g.: for `head <- 1 <- 2 <- 3 <- tail`, `iterateStopOnNodeValue(2)` returns the node whose value is 2
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun iterateStopOnNodeValue(element: E): Node<E> = this.iterateBeforeNodeValue(element).nextNode
}

private open class Tail<E> : Node<E>(null)

@Suppress("unused")
private fun <E> AtomicRef<out Node<out E>>.getNodeValue(): E? = if (this.value is Tail) null else this.value.nodeValueRef.value

@Suppress("NOTHING_TO_INLINE")
private inline fun Node<*>.isValidElementNode(): Boolean = !isHead() && !isTail() && !isRemoved()

@Suppress("NOTHING_TO_INLINE")
private inline fun Node<*>.isHead(): Boolean = this is Head

@Suppress("NOTHING_TO_INLINE")
private inline fun Node<*>.isTail(): Boolean = this is Tail

@Suppress("NOTHING_TO_INLINE")
private inline fun Node<*>.isRemoved(): Boolean = this.nodeValue == null