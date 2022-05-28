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

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.system.getTimeMillis

/**
 * 时间戳
 */
public actual fun currentTimeMillis(): Long {
    return getTimeMillis()
}

@OptIn(UnsafeNumber::class)
public actual fun currentTimeFormatted(format: String?): String {
    memScoped {
        val timeT = alloc<time_tVar>()
        time(timeT.ptr)
        val tm = localtime(timeT.ptr)
        try {
            val bb = allocArray<ByteVar>(40)
            strftime(bb, 40, "%Y-%M-%d %H:%M:%S", tm);
            return bb.toKString()
        } finally {
            free(tm)
        }
    }
}