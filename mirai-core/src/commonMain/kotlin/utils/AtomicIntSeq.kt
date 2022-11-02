/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

// IDE doesn't show warnings but compiler do.
@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package net.mamoe.mirai.internal.utils

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import net.mamoe.mirai.utils.getRandomUnsignedInt
import kotlin.jvm.JvmStatic

internal object AtomicIntSeq {
    @JvmStatic
    fun forMessageSeq(): AtomicIntMaxSeq = AtomicIntMaxSeq(0)

    @JvmStatic
    fun forPrivateSync(): AtomicInt65535Seq = AtomicInt65535Seq(getRandomUnsignedInt())
}

// value classes to optimize space

internal class AtomicIntMaxSeq(
    value: Int
) {
    private val value: AtomicInt = atomic(value)

    /**
     * Increment [value] within the range from `0` (inclusive) to [Int.MAX_VALUE] (exclusive).
     */
    inline fun next(): Int = value.incrementAndGet().mod(Int.MAX_VALUE)

    /**
     * Atomically update [value] if it is smaller than [new].
     *
     * @param new should be positive
     */
    inline fun updateIfSmallerThan(new: Int): Boolean {
        value.update { instant ->
            if (instant < new) new else return false
        }
        return true
    }

    /**
     * Atomically update [value] if it different with [new].
     *
     * @param new should be positive
     */
    inline fun updateIfDifferentWith(new: Int): Boolean {
        value.update { instant ->
            if (instant == new) return false
            new
        }
        return true
    }
}

internal class AtomicInt65535Seq(
    value: Int
) {
    private val value: AtomicInt = atomic(value)

    /**
     * Increment [value] within the range from `0` (inclusive) to `65535` (exclusive).
     */
    inline fun next(): Int = value.incrementAndGet().mod(65535)

    /**
     * Atomically update [value] if it is smaller than [new].
     *
     * @param new should be positive
     */
    inline fun updateIfSmallerThan(new: Int): Boolean {
        value.update { instant ->
            if (instant < new) new else return false
        }
        return true
    }

    /**
     * Atomically update [value] if it different with [new].
     *
     * @param new should be positive
     */
    inline fun updateIfDifferentWith(new: Int): Boolean {
        value.update { instant ->
            if (instant == new) return false
            new
        }
        return true
    }
}