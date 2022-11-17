/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login

import net.mamoe.mirai.utils.toInt
import net.mamoe.mirai.utils.toLongUnsigned
import java.net.Inet4Address
import java.net.UnknownHostException

internal actual fun String.toIpV4Long(): Long {
    return if (isEmpty()) {
        0
    } else {
        try {
            Inet4Address.getByName(this).address.toInt().toLongUnsigned()
        } catch (e: UnknownHostException) {
            -2
        }
    }
}