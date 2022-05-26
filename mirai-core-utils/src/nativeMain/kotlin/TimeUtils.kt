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

/**
 * 时间戳
 *
 * @see System.currentTimeMillis
 */
public actual fun currentTimeMillis(): Long {
    memScoped {
        val timeT = alloc<time_tVar>()
        time(timeT.ptr)
        return timeT.value
    }
}

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