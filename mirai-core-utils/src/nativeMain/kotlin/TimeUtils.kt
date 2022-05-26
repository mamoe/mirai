/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import platform.posix.CLOCK_REALTIME
import platform.posix.clock_gettime
import platform.posix.timespec

/**
 * 时间戳
 *
 * @see System.currentTimeMillis
 */
actual fun currentTimeMillis(): Long {
    memScoped {
        val s = cValue<timespec>()
        clock_gettime(CLOCK_REALTIME, s.ptr)
        return s.ptr.pointed.tv_nsec / 1000
    }
}

actual fun currentTimeFormatted(format: String?): String {
    TODO("Not yet implemented")
}