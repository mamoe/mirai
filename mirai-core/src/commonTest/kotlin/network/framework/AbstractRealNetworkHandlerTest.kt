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
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.plus
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.context.SsoProcessorContext
import net.mamoe.mirai.internal.network.context.SsoProcessorContextImpl
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.state.StateObserver
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
    abstract val factory: NetworkHandlerFactory<H>
    abstract val network: NetworkHandler

    var bot: QQAndroidBot by lateinitMutableProperty {
        MockBot {
            networkHandlerProvider { createHandler() }
            componentsProvider = { network.context } // initialized in [createHandler]
        }
    }

    open val networkLogger = MiraiLogger.TopLevel

    sealed class NHEvent {
        object Login : NHEvent()
        object Logout : NHEvent()
        object DoHeartbeatNow : NHEvent()
        object Init : NHEvent()
    }

    val nhEvents = ConcurrentLinkedQueue<NHEvent>()

    /**
     * This overrides [QQAndroidBot.components]
     */
    open val defaultComponents = ConcurrentComponentStorage().apply {
        val components = this
        val configuration = bot.configuration
        set(SsoProcessorContext, SsoProcessorContextImpl(bot))
        set(SsoProcessor, object : TestSsoProcessor(bot) {
            override suspend fun login(handler: NetworkHandler) {
                nhEvents.add(NHEvent.Login)
                super.login(handler)
            }

            override suspend fun logout(handler: NetworkHandler) {
                nhEvents.add(NHEvent.Logout)
                super.logout(handler)
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
        set(ServerList, ServerListImpl())

        set(BotOfflineEventMonitor, object : BotOfflineEventMonitor {
            override fun attachJob(bot: AbstractBot, scope: CoroutineScope) {
            }
        })
        // set(StateObserver, bot.run { stateObserverChain() })
    }

    /**
     * [additionalComponents] overrides [defaultComponents] and [QQAndroidBot.components]
     */
    open fun createHandler(additionalComponents: ComponentStorage? = null): H {
        // StateObserver
        val components = additionalComponents + defaultComponents + bot.createDefaultComponents()
        val observerComponents = if (
            additionalComponents?.getOrNull(StateObserver)
            ?: defaultComponents.getOrNull(StateObserver)
            == null
        ) {
            ConcurrentComponentStorage().apply {
                set(StateObserver, bot.run { components.stateObserverChain() })
            }
        } else null
        return factory.create(
            NetworkHandlerContextImpl(
                bot,
                networkLogger,
                observerComponents + components
            ),
            InetSocketAddress.createUnresolved("localhost", 123)
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // Assertions
    ///////////////////////////////////////////////////////////////////////////

    fun assertState(state: State) {
        assertEquals(state, network.state)
    }

}