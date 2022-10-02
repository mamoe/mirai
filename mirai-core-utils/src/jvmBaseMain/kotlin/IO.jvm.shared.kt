/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import java.io.InputStream

private fun dropContent0(stream: InputStream, buffer: ByteArray) {
    while (true) {
        val len = stream.read(buffer)
        if (len == -1) break
    }
}

public fun InputStream.dropContent(
    buffer: Int = 2048,
    close: Boolean = false,
) {
    if (close) {
        dropContent0(this, ByteArray(buffer))
    } else {
        this.use { dropContent0(it, ByteArray(buffer)) }
    }
}
