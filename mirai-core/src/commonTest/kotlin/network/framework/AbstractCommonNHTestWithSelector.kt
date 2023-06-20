/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitor
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitorImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.TestSelector
import net.mamoe.mirai.internal.network.handler.selector.NetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler

/**
 * When network is closed, it will reconnect, so that you test for real environment,
 * but you cannot check for its states (it will never be CLOSED until some fatal error, just like in real).
 */
internal abstract class AbstractCommonNHTestWithSelector :
    AbstractRealNetworkHandlerTest<TestSelectorNetworkHandler>() {
    init {
        overrideComponents[BotOfflineEventMonitor] = BotOfflineEventMonitorImpl()
    }

    val conn = PlatformConn()

    val selector = TestSelector<TestCommonNetworkHandler> {
        object : TestCommonNetworkHandler(bot, createContext(), createAddress()) {
            //            override suspend fun createConnection(decodePipeline: PacketDecodePipeline): PlatformConn = channel
            override suspend fun createConnection(): PlatformConn {
                return conn
            }
        }.apply {
            applyToInstances.forEach { it.invoke(this) }
        }
    }

    override val factory: NetworkHandlerFactory<TestSelectorNetworkHandler> =
        NetworkHandlerFactory { _, _ -> TestSelectorNetworkHandler(selector, bot) }


    private val applyToInstances = mutableListOf<TestCommonNetworkHandler.() -> Unit>()
    fun onEachNetworkInstance(action: TestCommonNetworkHandler.() -> Unit) {
        applyToInstances.add(action)
    }
}

internal class TestSelectorNetworkHandler(
    selector: NetworkHandlerSelector<TestCommonNetworkHandler>, override val bot: QQAndroidBot,
) : ITestNetworkHandler<PlatformConn>,
    SelectorNetworkHandler<TestCommonNetworkHandler>(selector) {

    fun currentInstance() = selector.getCurrentInstanceOrCreate()
    fun currentInstanceOrNull() = selector.getCurrentInstanceOrNull()

    private val applyToInstances = mutableListOf<TestCommonNetworkHandler.() -> Unit>()
    fun onEachInstance(action: TestCommonNetworkHandler.() -> Unit) {
        applyToInstances.add(action)
    }

    override fun setStateClosed(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateClosed(exception)
    }

    override fun setStateConnecting(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateConnecting(exception)
    }

    override fun setStateOK(conn: PlatformConn, exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateOK(conn, exception)
    }

    override fun setStateLoading(conn: PlatformConn): NetworkHandlerSupport.BaseStateImpl? {
        return selector.getCurrentInstanceOrCreate().setStateLoading(conn)
    }

}