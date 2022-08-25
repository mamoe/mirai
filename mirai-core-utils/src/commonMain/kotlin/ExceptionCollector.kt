/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("ExceptionCollectorKt_common")

package net.mamoe.mirai.utils

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

public open class ExceptionCollector {

    public constructor()
    public constructor(initial: Throwable?) {
        collect(initial)
    }

    public constructor(vararg initials: Throwable?) {
        for (initial in initials) {
            collect(initial)
        }
    }

    protected open fun beforeCollect(throwable: Throwable) {
    }

    @Volatile
    private var last: Throwable? = null
    private val hashCodes = mutableSetOf<Long>()
    private val suppressedList = mutableListOf<Throwable>()

    /**
     * @return `true` if [e] is new.
     */
    @Synchronized
    public fun collect(e: Throwable?): Boolean {
        if (e == null) return false
        if (!hashCodes.add(hash(e))) return false // filter out duplications
        // we can also check suppressed exceptions of [e] but actual influence would be slight.
        beforeCollect(e)
        this.last?.let { addSuppressed(e, it) }
        this.last = e
        return true
    }

    protected open fun addSuppressed(receiver: Throwable, e: Throwable) {
        suppressedList.add(e)
//        receiver.addSuppressed(e)
    }

    public fun collectGet(e: Throwable?): Throwable {
        this.collect(e)
        return getLast()!!
    }

    /**
     * Alias to [collect] to be used inside [withExceptionCollector]
     * @return `true` if [e] is new.
     */
    public fun collectException(e: Throwable?): Boolean = collect(e)

    /**
     * Adds [suppressedList] to suppressed exceptions of [last]
     */
    @Synchronized
    private fun bake() {
        last?.let { last ->
            for (suppressed in suppressedList.asReversed()) {
                last.addSuppressed(suppressed)
            }
        }
        suppressedList.clear()
    }

    public fun getLast(): Throwable? {
        bake()
        return last
    }

    @TerminalOperation // to give it a color for a clearer control flow
    public fun collectThrow(exception: Throwable): Nothing {
        collect(exception)
        throw getLast()!!
    }

    @TerminalOperation
    public fun throwLast(): Nothing {
        throw getLast() ?: error("Internal error: expected at least one exception collected.")
    }

    @DslMarker
    private annotation class TerminalOperation

    @TestOnly // very slow
    public fun asSequence(): Sequence<Throwable> {
        fun Throwable.itr(): Iterator<Throwable> {
            return (sequenceOf(this) + this.suppressedExceptions.asSequence()
                .flatMap { it.itr().asSequence() }).iterator()
        }

        val last = getLast() ?: return emptySequence()
        return Sequence { last.itr() }
    }

    @Synchronized
    public fun dispose() { // help gc
        this.last = null
        this.hashCodes.clear()
        this.suppressedList.clear()
    }

    public companion object {
        public fun compressExceptions(exceptions: Array<Throwable>): Throwable? {
            return ExceptionCollector(*exceptions).getLast()
        }

        public fun compressExceptions(exception: Throwable, vararg exceptions: Throwable): Throwable {
            return ExceptionCollector(exception, *exceptions).getLast()!!
        }
    }
}

/**
 * Run with a coverage of `throw`. All thrown exceptions will be caught and rethrown with [ExceptionCollector.collectThrow]
 */
public inline fun <R> withExceptionCollector(action: ExceptionCollector.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return ExceptionCollector().run {
        withExceptionCollector(action).also { dispose() }
    }
}

/**
 * Run with a coverage of `throw`. All thrown exceptions will be caught and rethrown with [ExceptionCollector.collectThrow]
 */
public inline fun <R> ExceptionCollector.withExceptionCollector(action: ExceptionCollector.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    this.run {
        try {
            return action()
        } catch (e: Throwable) {
            collectThrow(e)
        } finally {
            dispose()
        }
    }
}

internal expect fun hash(e: Throwable): Long