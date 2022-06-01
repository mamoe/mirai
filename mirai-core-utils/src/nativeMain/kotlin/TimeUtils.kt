/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("RedundantVisibilityModifier")

package net.mamoe.mirai.utils

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.*
import platform.posix.localtime
import platform.posix.strftime
import platform.posix.time
import platform.posix.time_tVar

/**
 * 时间戳
 */
@OptIn(UnsafeNumber::class)
public actual fun currentTimeMillis(): Long {
    // Do not use getTimeMillis from stdlib, it doesn't support iosSimulatorArm64
    memScoped {
        val timeT = alloc<time_tVar>()
        time(timeT.ptr)
        return timeT.value.toLongUnsigned()
    }
}


private val timeLock = ReentrantLock()

@OptIn(UnsafeNumber::class)
public actual fun currentTimeFormatted(format: String?): String = timeLock.withLock {
    memScoped {
        val timeT = alloc<time_tVar>()
        time(timeT.ptr)

        // http://www.cplusplus.com/reference/clibrary/ctime/localtime/
        // tm returns a static pointer which doesn't need to free
        val tm = localtime(timeT.ptr) // localtime is not thread-safe

        val bb = allocArray<ByteVar>(40)
        strftime(bb, 40, "%Y-%M-%d %H:%M:%S", tm);

        bb.toKString()
    }
}