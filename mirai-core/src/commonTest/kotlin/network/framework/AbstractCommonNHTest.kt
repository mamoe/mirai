/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.handler.*
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.ExceptionCollector

/**
 * You may need to override [createConnection]
 */
internal abstract class TestCommonNetworkHandler(
    override val bot: QQAndroidBot,
    context: NetworkHandlerContext,
    address: SocketAddress,
) : CommonNetworkHandler<PlatformConn>(context, address), ITestNetworkHandler<PlatformConn> {
    override suspend fun createConnection(): PlatformConn {
        return PlatformConn()
    }

    override fun PlatformConn.writeAndFlushOrCloseAsync(packet: OutgoingPacket) {
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override fun PlatformConn.close() {
    }

    override fun setStateClosed(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateClosed(exception) }
    }

    override fun setStateConnecting(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateConnecting(ExceptionCollector(exception)) }
    }

    override fun setStateOK(conn: PlatformConn, exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        exception?.printStackTrace()
        return setState { StateOK(conn, CompletableDeferred(Unit)) }
    }

    override fun setStateLoading(conn: PlatformConn): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateLoading(conn) }
    }

}

/**
 * Without selector. When network is closed, it will not reconnect, so that you can check for its states.
 *
 * @see AbstractCommonNHTestWithSelector
 */
internal expect abstract class AbstractCommonNHTest() :
    AbstractRealNetworkHandlerTest<TestCommonNetworkHandler> {

    val conn: PlatformConn

    override val network: TestCommonNetworkHandler

    override val factory: NetworkHandlerFactory<TestCommonNetworkHandler>

    protected fun removeOutgoingPacketEncoder()
}

internal expect class PlatformConn()