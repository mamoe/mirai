/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import kotlin.test.AfterTest

/**
 * Without selector. When network is closed, it will not reconnect, so that you can check for its states.
 *
 * @see AbstractCommonNHTestWithSelector
 */
internal actual abstract class AbstractCommonNHTest actual constructor() :
    AbstractRealNetworkHandlerTest<TestCommonNetworkHandler>() {

    private val startedDispatchers = mutableListOf<ExecutorCoroutineDispatcher>()

    @AfterTest
    fun cleanupDispatchers() {
        startedDispatchers.forEach { it.close() }
    }

    actual override val factory: NetworkHandlerFactory<TestCommonNetworkHandler> =
        NetworkHandlerFactory<TestCommonNetworkHandler> { context, address ->
            object : TestCommonNetworkHandler(bot, context, address) {
                override suspend fun createConnection(): PlatformConn {
                    return conn.apply {
                        doRegister() // restart channel
//                        setupChannelPipeline(
//                            pipeline(), PacketDecodePipeline(
//                                coroutineContext.plus(
//                                    NioEventLoopGroup().asCoroutineDispatcher().also { startedDispatchers.add(it) })
//                            )
//                        )
                    }
                }
            }
        }

    protected actual fun removeOutgoingPacketEncoder() {
        kotlin.runCatching {
            conn.pipeline().remove("outgoing-packet-encoder")
        }
    }

    actual val conn: PlatformConn = NettyNHTestChannel()
}

internal actual typealias PlatformConn = NettyNHTestChannel
