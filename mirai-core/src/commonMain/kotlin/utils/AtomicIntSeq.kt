/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import net.mamoe.mirai.utils.getRandomUnsignedInt
import net.mamoe.mirai.utils.toLongUnsigned

// We probably can reduce duplicates by using value classes, but atomicFU compiler might not be able to compile it.

// TODO: 2021/6/27 tests
internal class AtomicIntSeq private constructor(
    initial: Int,
    private val maxExclusive: Int,
) {
    private val value = atomic(initial)

    /**
     * Increment [value] within the range from 0 (inclusive) to [maxExclusive] (exclusive).
     */
    fun next(): Int = value.incrementAndGet().mod(maxExclusive) // positive

    /**
     * Atomically update [value] if it is smaller than [new].
     */
    fun updateIfSmallerThan(new: Int): Boolean {
        value.update { instant ->
            if (instant < new) new else return false
        }
        return true
    }

    fun updateIfDifferentWith(new: Int): Boolean {
        value.update { instant ->
            if (instant == new) return false
            new
        }
        return true
    }

    companion object {
        @JvmStatic
        fun forMessageSeq() = AtomicIntSeq(0, Int.MAX_VALUE)

        @JvmStatic
        fun forPrivateSync() = AtomicIntSeq(getRandomUnsignedInt(), 65535)
    }
}

// TODO: 2021/6/27 tests
internal class AtomicLongSeq(
    initial: Long = getRandomUnsignedInt().toLongUnsigned(),
    private val maxExclusive: Long = 65535,
) {
    private val value = atomic(initial)

    /**
     * Increment [value] within the range from 0 (inclusive) to [maxExclusive] (exclusive).
     */
    fun next(): Long = value.incrementAndGet().mod(maxExclusive) // positive

    /**
     * Atomically update [value] if it is smaller than [new].
     */
    fun updateIfSmallerThan(new: Long) {
        value.update { instant ->
            if (instant < new) new else return
        }
    }
}