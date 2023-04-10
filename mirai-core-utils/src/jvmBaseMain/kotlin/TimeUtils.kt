/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

public actual fun currentTimeMillis(): Long = System.currentTimeMillis()


private val timeFormat: SimpleDateFormat by threadLocal {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
}

public actual fun formatTime(epochTimeMillis: Long, format: String?): String {
    return if (format == null) {
        timeFormat.format(Date(epochTimeMillis))
    } else {
        SimpleDateFormat(format, Locale.getDefault()).format(Date(epochTimeMillis))
    }
}