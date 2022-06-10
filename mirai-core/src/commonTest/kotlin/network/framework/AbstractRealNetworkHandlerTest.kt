/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.MockAccount
import net.mamoe.mirai.internal.MockConfiguration
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.setAll
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcRespRegister
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.lateinitMutableProperty
import network.framework.components.TestEventDispatcherImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals

/**
 * With real factory and components as in [QQAndroidBot.components].
 *
 * Extend [AbstractNettyNHTestWithSelector] or [AbstractNettyNHTest].
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal sealed class AbstractRealNetworkHandlerTest<H : NetworkHandler> : AbstractNetworkHandlerTest() {
    abstract val factory: NetworkHandlerFactory<H>
    abstract val network: H

    private var botInit = false
    var bot: QQAndroidBot by lateinitMutableProperty { botInit = true; createBot() }

    @AfterEach
    fun afterEach() {
        if (botInit) bot.close()
    }

    protected open fun createBot(account: BotAccount = MockAccount): QQAndroidBot {
        return object : QQAndroidBot(account, MockConfiguration.copy()) {
            override fun createBotLevelComponents(): ConcurrentComponentStorage =
                super.createBotLevelComponents().apply { setAll(overrideComponents) }

            override fun createNetworkHandler(): NetworkHandler =
                this@AbstractRealNetworkHandlerTest.createHandler()
        }
    }

    open val networkLogger = MiraiLogger.Factory.create(NetworkHandler::class, "network")

    sealed class NHEvent {
        object Login : NHEvent()
        object Logout : NHEvent()
        object DoHeartbeatNow : NHEvent()
        object Init : NHEvent()
        object SetLoginHalted : NHEvent()
    }

    val nhEvents = ConcurrentLinkedQueue<NHEvent>()

    /**
     * This overrides [QQAndroidBot.components]
     */
    val overrideComponents = ConcurrentComponentStorage().apply {
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

            override suspend fun doRegisterNow(networkHandler: NetworkHandler): StatSvc.Register.Response {
                nhEvents.add(NHEvent.DoHeartbeatNow)
                networkLogger.debug { "HeartbeatProcessor.doRegisterNow" }
                return StatSvc.Register.Response(SvcRespRegister())
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
            override fun setLoginHalted() {
                nhEvents.add(NHEvent.SetLoginHalted)
            }

            override suspend fun init() {
                nhEvents.add(NHEvent.Init)
                networkLogger.debug { "BotInitProcessor.init" }
                bot.components[SsoProcessor].firstLoginResult.value = FirstLoginResult.PASSED
            }
        })
        set(ServerList, ServerListImpl())

        set(
            EventDispatcher,
            // Note that in real we use 'bot.coroutineContext', but here we override with a new, independent job
            // to allow BotOfflineEvent.Active to be broadcast and joinBroadcast works even if bot coroutineScope is closed.
            TestEventDispatcherImpl(
                bot.coroutineContext + SupervisorJob(),
                bot.logger.subLogger("TestEventDispatcherImpl")
            )
        )
        // set(StateObserver, bot.run { stateObserverChain() })
    }

    fun <T : Any> setComponent(key: ComponentKey<in T>, instance: T): T {
        overrideComponents[key] = instance
        return instance
    }

    open fun createHandler(): NetworkHandler = factory.create(createContext(), createAddress())
    open fun createContext(): NetworkHandlerContextImpl =
        NetworkHandlerContextImpl(bot, networkLogger, bot.createNetworkLevelComponents())

    //Use overrideComponents to avoid StackOverflowError when applying components
    open fun createAddress(): InetSocketAddress =
        overrideComponents[ServerList].pollAny().let { InetSocketAddress.createUnresolved(it.host, it.port) }

    ///////////////////////////////////////////////////////////////////////////
    // Assertions
    ///////////////////////////////////////////////////////////////////////////

    fun assertState(state: State) {
        assertEquals(state, network.state)
    }

    fun assertState(vararg accepted: State) {
        val s = network.state
        if (s !in accepted) {
            throw AssertionError("Expected: ${accepted.joinToString()}, actual: $s")
        }
    }

    fun NetworkHandler.assertState(state: State) {
        assertEquals(state, this.state)
    }

    val eventDispatcher get() = bot.components[EventDispatcher]
    val firstLoginResult: FirstLoginResult? get() = bot.components[SsoProcessor].firstLoginResult.value
}

internal fun AbstractRealNetworkHandlerTest<*>.setSsoProcessor(action: suspend SsoProcessor.(handler: NetworkHandler) -> Unit) {
    overrideComponents[SsoProcessor] = object : SsoProcessor by overrideComponents[SsoProcessor] {
        override suspend fun login(handler: NetworkHandler) = action(handler)
    }
}
