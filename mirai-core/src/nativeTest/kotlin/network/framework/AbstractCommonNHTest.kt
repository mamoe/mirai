/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.cinterop.cstr
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.SocketAddress
import net.mamoe.mirai.utils.mapToByteArray
import net.mamoe.mirai.utils.toInt
import net.mamoe.mirai.utils.toLongUnsigned

/**
 * Without selector. When network is closed, it will not reconnect, so that you can check for its states.
 *
 * @see AbstractCommonNHTestWithSelector
 */
internal actual abstract class AbstractCommonNHTest actual constructor() :
    AbstractRealNetworkHandlerTest<TestCommonNetworkHandler>() {

    actual override val factory: NetworkHandlerFactory<TestCommonNetworkHandler> =
        NetworkHandlerFactory<TestCommonNetworkHandler> { context, address ->
            object : TestCommonNetworkHandler(bot, context, address) {
                override suspend fun createConnection(): PlatformConn {
                    return conn
                }
            }
        }


    protected actual fun removeOutgoingPacketEncoder() {
    }

    actual val conn: PlatformConn get() = PlatformConn(createAddress())

}

internal actual class PlatformConn actual constructor(actual val address: SocketAddress) {
    internal actual fun getConnectedIPPlatform(): Long {
        address.host.run {
            if (isEmpty()) return 0
            val split = split('.')
            return if (split.size == 4 && split.all { it.toUByteOrNull() != null }) {
                split.reversed().mapToByteArray {
                    it.toUByte().toByte()
                }.toInt().toLongUnsigned()
            } else sockets.get_ulong_ip_by_name(this.cstr).toLong();
        }
    }


}