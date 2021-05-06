/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.internal.AbstractBot
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
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.lateinitMutableProperty
import org.junit.jupiter.api.TestInstance
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals

/**
 * With real factory and components as in [QQAndroidBot.components].
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal abstract class AbstractRealNetworkHandlerTest<H : NetworkHandler> : AbstractTest() {
    init {
        System.setProperty("mirai.debug.network.state.observer.logging", "full")
        System.setProperty("mirai.debug.network.show.all.components", "true")
    }

    protected abstract val factory: NetworkHandlerFactory<H>
    protected abstract val network: NetworkHandler

    protected open var bot: QQAndroidBot by lateinitMutableProperty {
        MockBot {
            networkHandlerProvider { createHandler() }
        }
    }

    protected open val networkLogger = MiraiLogger.TopLevel

    protected sealed class NHEvent {
        object Login : NHEvent()
        object Logout : NHEvent()
        object DoHeartbeatNow : NHEvent()
        object Init : NHEvent()
    }

    protected val nhEvents = ConcurrentLinkedQueue<NHEvent>()

    protected open val defaultComponents = ConcurrentComponentStorage().apply {
        val components = this
        val configuration = bot.configuration
        set(SsoProcessorContext, SsoProcessorContextImpl(bot))
        set(SsoProcessor, object : SsoProcessor {
            override val client: QQAndroidClient get() = bot.client
            override val ssoSession: SsoSession get() = bot.client
            override var firstLoginSucceed: Boolean = false
            override var registerResp: StatSvc.Register.Response? = null
            override fun createObserverChain(): StateObserver = get(StateObserver)
            override suspend fun login(handler: NetworkHandler) {
                nhEvents.add(NHEvent.Login)
                networkLogger.debug { "SsoProcessor.login" }
            }

            override suspend fun logout(handler: NetworkHandler) {
                nhEvents.add(NHEvent.Logout)
                networkLogger.debug { "SsoProcessor.logout" }
            }
        })
        set(HeartbeatProcessor, object : HeartbeatProcessor {
            override suspend fun doAliveHeartbeatNow(networkHandler: NetworkHandler) {
                nhEvents.add(NHEvent.DoHeartbeatNow)
                networkLogger.debug { "HeartbeatProcessor.doAliveHeartbeatNow" }
            }

            override suspend fun doStatHeartbeatNow(networkHandler: NetworkHandler) {
                nhEvents.add(NHEvent.DoHeartbeatNow)
                networkLogger.debug { "HeartbeatProcessor.doStatHeartbeatNow" }
            }
        })
        set(KeyRefreshProcessor, object : KeyRefreshProcessor {
            override suspend fun keyRefreshLoop(handler: NetworkHandler) {}
            override suspend fun refreshKeysNow(handler: NetworkHandler) {}
        })
        set(ConfigPushProcessor, object : ConfigPushProcessor {
            override suspend fun syncConfigPush(network: NetworkHandler) {}
        })

        set(BotInitProcessor, object : BotInitProcessor {
            override suspend fun init() {
                nhEvents.add(NHEvent.Init)
                networkLogger.debug { "BotInitProcessor.init" }
            }
        })
        set(ContactCacheService, ContactCacheServiceImpl(bot))
        set(ContactUpdater, ContactUpdaterImpl(bot, components, networkLogger))
        set(BdhSessionSyncer, BdhSessionSyncerImpl(configuration, components, networkLogger))
        set(ServerList, ServerListImpl())
        set(PacketLoggingStrategy, PacketLoggingStrategyImpl(bot))
        set(PacketHandler, LoggingPacketHandlerAdapter(get(PacketLoggingStrategy), networkLogger))
        set(PacketCodec, PacketCodecImpl())
        set(OtherClientUpdater, OtherClientUpdaterImpl(bot, components, bot.logger))
        set(ConfigPushSyncer, ConfigPushSyncerImpl())

        set(BotOfflineEventMonitor, object : BotOfflineEventMonitor {
            override fun attachJob(bot: AbstractBot, scope: CoroutineScope) {
            }
        })
        set(StateObserver, bot.run { stateObserverChain() })
    }

    protected open fun createHandler(additionalComponents: ComponentStorage? = null): H {
        return factory.create(
            NetworkHandlerContextImpl(
                bot,
                networkLogger,
                additionalComponents?.plus(defaultComponents) ?: defaultComponents
            ),
            InetSocketAddress.createUnresolved("localhost", 123)
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // Assertions
    ///////////////////////////////////////////////////////////////////////////

    protected fun assertState(state: State) {
        assertEquals(state, network.state)
    }

}