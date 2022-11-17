/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login

import kotlinx.cinterop.cstr
import net.mamoe.mirai.utils.mapToByteArray
import net.mamoe.mirai.utils.toInt
import net.mamoe.mirai.utils.toLongUnsigned
import sockets.get_ulong_ip_by_name

internal actual fun String.toIpV4Long(): Long {
    if (isEmpty()) return 0
    val split = split('.')
    return if (split.size == 4 && split.any { it.toUByteOrNull() != null }) {
        split.reversed().mapToByteArray {
            it.toUByte().toByte()
        }.toInt().toLongUnsigned()
    } else get_ulong_ip_by_name(this.cstr).toLong();
}