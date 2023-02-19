/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.impl.common

import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.MockConfiguration
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.setAll
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitor
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitorImpl
import net.mamoe.mirai.internal.network.components.FirstLoginResult
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.framework.TestCommonNetworkHandler
import net.mamoe.mirai.internal.network.framework.setSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.selector.KeepAliveNetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.selector.NetworkChannelException
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.handler.selectorLogger
import net.mamoe.mirai.internal.network.impl.HeartbeatFailedException
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.network.CustomLoginFailedException
import net.mamoe.mirai.utils.TestOnly
import net.mamoe.mirai.utils.cast
import kotlin.test.*

internal class CommonNHBotNormalLoginTest : AbstractCommonNHTest() {
    init {
        overrideComponents[BotOfflineEventMonitor] = BotOfflineEventMonitorImpl()
    }

    val selector = KeepAliveNetworkHandlerSelector(selectorLogger) {
        factory.create(createContext(), createAddress())
    }

    override val network: TestCommonNetworkHandler
        get() = selector.getCurrentInstanceOrCreate().cast()

    override fun createBot(account: BotAccount): QQAndroidBot {
        return object : QQAndroidBot(account, MockConfiguration.copy()) {
            override fun createBotLevelComponents(): ConcurrentComponentStorage =
                super.createBotLevelComponents().apply { setAll(overrideComponents) }

            override fun createNetworkHandler(): NetworkHandler =
                SelectorNetworkHandler(selector)
        }
    }


    class CusLoginException(message: String?) : CustomLoginFailedException(true, message)

    @AfterTest
    fun `close bot`() = runBlockingUnit {
        bot.logger.info("[TEST UNIT] Releasing bot....")
        bot.closeAndJoin()
    }

    @Test
    fun `test login fail`() = runBlockingUnit {
        setSsoProcessor { throw CusLoginException("A") }
        assertFailsWith<CusLoginException>("A") { bot.login() }
        assertFalse(bot.isActive)
    }

    // #1963
    @Test
    fun `test first login failure with internally handled exceptions`() = runBlockingUnit {
        setSsoProcessor { throw IOException("test Connection reset by peer") }
        assertFailsWith<IOException>("test Connection reset by peer") { bot.login() }
        assertState(NetworkHandler.State.CLOSED)
    }

    // #1963
    @Test
    fun `test first login failure with internally handled exceptions2`() = runBlockingUnit {
        setSsoProcessor { throw NetworkChannelException("test Connection reset by peer") }
        assertFailsWith<NetworkChannelException>("test Connection reset by peer") { bot.login() }
        assertState(NetworkHandler.State.CLOSED)
    }

    // #1963
    @Test
    fun `test first login failure with internally handled exceptions3`() = runBlockingUnit {
        setSsoProcessor {
            //Throw TimeoutCancellationException
            withTimeout(1) {
                delay(1000)
            }
        }
        assertFailsWith<TimeoutCancellationException>("test TimeoutCancellationException") { bot.login() }
        assertState(NetworkHandler.State.CLOSED)
    }

    // 经过 #1963 考虑后在初次登录遇到任何错误都终止并传递异常
//    @Test
//    fun `test network broken`() = runBlockingUnit {
//        var retryCounter = 0
//        setSsoProcessor {
//            eventDispatcher.joinBroadcast()
//            if (retryCounter++ >= 15) {
//                return@setSsoProcessor
//            }
//            channel.pipeline().fireExceptionCaught(IOException("TestNetworkBroken"))
//            awaitCancellation() // receive exception from "network"
//        }
//        bot.login()
//    }

    @Test
    fun `test resume after MsfOffline received after first login`() = runBlockingUnit {
        bot.login()
        assertEquals(FirstLoginResult.PASSED, firstLoginResult)
        bot.network.close(StatSvc.ReqMSFOffline.MsfOfflineToken(0, 0, 0))

        eventDispatcher.joinBroadcast()
        yield() // auto resume in BotOfflineEventMonitor
        eventDispatcher.joinBroadcast()

        assertState(NetworkHandler.State.OK)
    }

    // #2504, #2488
    @Test
    fun `test resume failed with TimeoutCancellationException`() = runBlockingUnit {
        var first = true
        var failCount = 3
        setSsoProcessor {
            if (first) {
                first = false
            } else {
                if (failCount > 0) {
                    failCount--
                    //Throw TimeoutCancellationException
                    withTimeout(1) {
                        delay(1000)
                    }
                }
            }
        }
        bot.login()
        bot.network.close(HeartbeatFailedException("Heartbeat Timeout", RuntimeException("Timeout stub"), true))
        eventDispatcher.joinBroadcast()
        yield() // auto resume in BotOfflineEventMonitor
        eventDispatcher.joinBroadcast()
        assertState(NetworkHandler.State.OK)
    }

}
