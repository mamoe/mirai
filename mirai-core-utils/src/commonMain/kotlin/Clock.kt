/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("ClockKt_common")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmName

public interface Clock {
    public fun currentTimeMillis(): Long
    public fun currentTimeSeconds(): Long = currentTimeMillis() / 1000

    public object SystemDefault : Clock {
        override fun currentTimeMillis(): Long = net.mamoe.mirai.utils.currentTimeMillis()
        override fun currentTimeSeconds(): Long = net.mamoe.mirai.utils.currentTimeSeconds()
    }
}

public fun Clock.adjusted(diffMillis: Long): Clock = AdjustedClock(this, diffMillis)

public class AdjustedClock(
    private val clock: Clock,
    private val diffMillis: Long,
) : Clock {
    override fun currentTimeMillis(): Long = clock.currentTimeMillis() + diffMillis
    override fun currentTimeSeconds(): Long = (clock.currentTimeMillis() + diffMillis) / 1000
}

public expect inline fun measureTimeMillis(block: () -> Unit): Long