/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.plus
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.context.SsoProcessorContext
import net.mamoe.mirai.internal.network.context.SsoProcessorContextImpl
import net.mamoe.mirai.internal.network.context.SsoSession
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import java.net.InetSocketAddress

/**
 * With real factory and components as in [QQAndroidBot.components].
 */
internal abstract class AbstractRealNetworkHandlerTest<H : NetworkHandler>(
    private val factory: NetworkHandlerFactory<H>,
) : AbstractTest() {
    val bot = MockBot()
    val networkLogger = MiraiLogger.TopLevel

    protected open val defaultComponents = ConcurrentComponentStorage().apply {
        val components = this
        val configuration = bot.configuration
        set(SsoProcessorContext, SsoProcessorContextImpl(bot))
        set(SsoProcessor, object : SsoProcessor {
            override val client: QQAndroidClient get() = bot.client
            override val ssoSession: SsoSession get() = bot.client
            override fun createObserverChain(): StateObserver = StateObserver.NOP
            override suspend fun login(handler: NetworkHandler) {
                networkLogger.debug { "SsoProcessor.login" }
            }
        })
        set(HeartbeatProcessor, object : HeartbeatProcessor {
            override suspend fun doHeartbeatNow(networkHandler: NetworkHandler) {
                networkLogger.debug { "HeartbeatProcessor.doHeartbeatNow" }
            }
        })
        set(KeyRefreshProcessor, KeyRefreshProcessorImpl(networkLogger))
        set(ConfigPushProcessor, ConfigPushProcessorImpl(networkLogger))

        set(BotInitProcessor, object : BotInitProcessor {
            override suspend fun init() {
                networkLogger.debug { "BotInitProcessor.init" }
            }
        })
        set(ContactCacheService, ContactCacheServiceImpl(bot))
        set(ContactUpdater, ContactUpdaterImpl(bot, components, networkLogger))
        set(BdhSessionSyncer, BdhSessionSyncerImpl(configuration, networkLogger, components))
        set(ServerList, ServerListImpl())
        set(PacketHandler, LoggingPacketHandler(bot, components, networkLogger))
        set(PacketCodec, PacketCodecImpl())
        set(OtherClientUpdater, OtherClientUpdaterImpl(bot, components, bot.logger))
        set(ConfigPushSyncer, ConfigPushSyncerImpl())

        set(StateObserver, StateObserver.NOP)

    }

    protected open fun createHandler(additionalComponents: ComponentStorage? = null): NetworkHandler {
        return factory.create(
            NetworkHandlerContextImpl(
                bot,
                networkLogger,
                additionalComponents?.plus(defaultComponents) ?: defaultComponents
            ),
            InetSocketAddress.createUnresolved("localhost", 123)
        )
    }
}