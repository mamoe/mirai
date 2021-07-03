/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.internal.network.framework

import io.netty.channel.Channel
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitor
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitorImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.TestSelector
import net.mamoe.mirai.internal.network.handler.selector.NetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.utils.cast

/**
 * When network is closed, it will reconnect, so that you test for real environment,
 * but you cannot check for its states (it will never be CLOSED until some fatal error, just like in real).
 */
internal abstract class AbstractNettyNHTestWithSelector : AbstractRealNetworkHandlerTest<TestSelectorNetworkHandler>() {
    init {
        overrideComponents[BotOfflineEventMonitor] = BotOfflineEventMonitorImpl()
    }

    val channel = AbstractNettyNHTest.NettyNHTestChannel()

    val selector = TestSelector<TestNettyNH> {
        object : TestNettyNH(bot, createContext(), createAddress()) {
            override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel = channel
        }
    }

    override val factory: NetworkHandlerFactory<TestSelectorNetworkHandler> =
        NetworkHandlerFactory { _, _ -> TestSelectorNetworkHandler(selector, bot) }

    override val network: TestSelectorNetworkHandler get() = bot.network.cast()
}

internal class TestSelectorNetworkHandler(
    selector: NetworkHandlerSelector<TestNettyNH>, override val bot: QQAndroidBot,
) : ITestNetworkHandler,
    SelectorNetworkHandler<TestNettyNH>(selector) {

    fun currentInstance() = selector.getCurrentInstanceOrCreate()
    fun currentInstanceOrNull() = selector.getCurrentInstanceOrNull()

    override fun setStateClosed(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateClosed(exception)
    }

    override fun setStateConnecting(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateConnecting(exception)
    }

    override fun setStateOK(channel: Channel, exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateOK(channel, exception)
    }

    override fun setStateLoading(channel: Channel): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateLoading(channel)
    }

}