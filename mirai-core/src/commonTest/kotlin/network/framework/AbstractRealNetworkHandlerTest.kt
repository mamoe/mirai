/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.framework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.internal.*
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.KeyWithCreationTime
import net.mamoe.mirai.internal.network.KeyWithExpiry
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.WLoginSimpleInfo
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.setAll
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.*
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcRespRegister
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.internal.utils.crypto.QQEcdh
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.*
import network.framework.components.TestEventDispatcherImpl
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.assertEquals

/**
 * With real factory and components as in [QQAndroidBot.components].
 *
 * Extend [AbstractCommonNHTestWithSelector] or [AbstractCommonNHTest].
 */
internal abstract class AbstractRealNetworkHandlerTest<H : NetworkHandler> : AbstractNetworkHandlerTest() {
    abstract val factory: NetworkHandlerFactory<H>

    /**
     * This is shared for all [createBot] by default. `network === bot.network`, unless you change it.
     */
    open val network: H by lateinitMutableProperty {
        factory.create(createContext(), createAddress())
    }

    private var botInit = false
    var bot: QQAndroidBot by lateinitMutableProperty {
        botInit = true
        createBot()
    }

    @AfterTest
    fun afterEach() {
        println("Test finished, closing Bot")
        if (botInit) {
            bot.close()
            println("joining Bot")
            runBlockingUnit { bot.join() }
            println("cleanup ok")
        }
    }

    protected open fun createBot(account: BotAccount = MockAccount): QQAndroidBot {
        return object : QQAndroidBot(account, MockConfiguration.copy()) {
            override fun createBotLevelComponents(): ConcurrentComponentStorage =
                super.createBotLevelComponents().apply {
                    setAll(overrideComponents)
                    get(AccountSecretsManager).getSecretsOrCreate(
                        account,
                        DeviceInfo.random(Random(1))
                    ).wLoginSigInfo = createWLoginSigInfo(uin)
                }

            override fun createNetworkHandler(): NetworkHandler =
                this@AbstractRealNetworkHandlerTest.network
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

    private val eventDispatcherJob = SupervisorJob()

    @AfterTest
    private fun cancelJob() {
        eventDispatcherJob.cancel()
        println("Test finished, joining eventDispatcherJob")
        runBlockingUnit { eventDispatcherJob.join() }
    }

    /**
     * This overrides [QQAndroidBot.components]
     */
    @OptIn(TestOnly::class)
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
                bot.components[SsoProcessor].setFirstLoginResult(FirstLoginResult.PASSED)
            }
        })
        set(ServerList, ServerListImpl())

        set(
            EventDispatcher,
            // Note that in real we use 'bot.coroutineContext', but here we override with a new, independent job
            // to allow BotOfflineEvent.Active to be broadcast and joinBroadcast works even if bot coroutineScope is closed.
            TestEventDispatcherImpl(
                bot.coroutineContext + eventDispatcherJob,
                bot.logger.subLogger("TestEventDispatcherImpl")
            )
        )

        set(BotOfflineEventMonitor, object : BotOfflineEventMonitor {
            override fun attachJob(bot: AbstractBot, scope: CoroutineScope) {
            }
        })

        set(EcdhInitialPublicKeyUpdater, object : EcdhInitialPublicKeyUpdater {
            override suspend fun refreshInitialPublicKeyAndApplyEcdh() {
            }

            override suspend fun initializeSsoSecureEcdh() {
            }

            override fun getQQEcdh(): QQEcdh = QQEcdh()
        })

        set(AccountSecretsManager, MemoryAccountSecretsManager())
        // set(StateObserver, bot.run { stateObserverChain() })
    }


    fun <T : Any> setComponent(key: ComponentKey<in T>, instance: T): T {
        overrideComponents[key] = instance
        return instance
    }

    open fun createContext(): NetworkHandlerContextImpl =
        NetworkHandlerContextImpl(bot, networkLogger, bot.createNetworkLevelComponents())

    //Use overrideComponents to avoid StackOverflowError when applying components
    open fun createAddress(): SocketAddress =
        overrideComponents[ServerList].pollAny().let { createSocketAddress(it.host, it.port) }

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
    var firstLoginResult: FirstLoginResult?
        get() = bot.components[SsoProcessor].firstLoginResult
        set(value) {
            bot.components[SsoProcessor].setFirstLoginResult(value)
        }
}

internal fun AbstractRealNetworkHandlerTest<*>.setSsoProcessor(action: suspend SsoProcessor.(handler: NetworkHandler) -> Unit) {
    overrideComponents[SsoProcessor] = object : SsoProcessor by overrideComponents[SsoProcessor] {
        override suspend fun login(handler: NetworkHandler) = action(handler)
    }
}

internal fun createWLoginSigInfo(
    uin: Long,
    creationTime: Long = currentTimeSeconds(),
    random: Random = Random(1)
): WLoginSigInfo {
    return WLoginSigInfo(
        uin = uin,
        encryptA1 = null,
        noPicSig = null,
        simpleInfo = WLoginSimpleInfo(
            uin = uin,
            imgType = EMPTY_BYTE_ARRAY,
            imgFormat = EMPTY_BYTE_ARRAY,
            imgUrl = EMPTY_BYTE_ARRAY,
            mainDisplayName = EMPTY_BYTE_ARRAY
        ), // defaults {}, from asyncContext._G
        appPri = 4294967295L, // defaults {}, from asyncContext._G
        a2ExpiryTime = creationTime + 2160000L, // or from asyncContext._t403.get_body_data()
        loginBitmap = 0,
        tgt = getRandomByteArray(16, random),
        a2CreationTime = creationTime,
        tgtKey = getRandomByteArray(16, random), // from asyncContext._login_bitmap
        userStSig = KeyWithCreationTime(getRandomByteArray(16, random), creationTime),
        userStKey = EMPTY_BYTE_ARRAY,
        userStWebSig = KeyWithExpiry(
            EMPTY_BYTE_ARRAY,
            creationTime,
            creationTime + 6000
        ),
        userA5 = KeyWithCreationTime(getRandomByteArray(16, random), creationTime),
        userA8 = KeyWithExpiry(
            EMPTY_BYTE_ARRAY,
            creationTime,
            creationTime + 72000L
        ),
        lsKey = KeyWithExpiry(
            EMPTY_BYTE_ARRAY,
            creationTime,
            creationTime + 1641600L
        ),
        sKey = KeyWithExpiry(
            EMPTY_BYTE_ARRAY,
            creationTime,
            creationTime + 86400L
        ),
        userSig64 = KeyWithCreationTime(EMPTY_BYTE_ARRAY, creationTime),
        openId = EMPTY_BYTE_ARRAY,
        openKey = KeyWithCreationTime(EMPTY_BYTE_ARRAY, creationTime),
        vKey = KeyWithExpiry(
            EMPTY_BYTE_ARRAY,
            creationTime,
            creationTime + 1728000L
        ),
        accessToken = KeyWithCreationTime(EMPTY_BYTE_ARRAY, creationTime),
        d2 = KeyWithExpiry(
            getRandomByteArray(16, random),
            creationTime,
            creationTime + 1728000L
        ),
        d2Key = getRandomByteArray(16, random),
        sid = KeyWithExpiry(
            EMPTY_BYTE_ARRAY,
            creationTime,
            creationTime + 1728000L
        ),
        aqSig = KeyWithCreationTime(EMPTY_BYTE_ARRAY, creationTime),
        psKeyMap = mutableMapOf(),
        pt4TokenMap = mutableMapOf(),
        superKey = EMPTY_BYTE_ARRAY,
        payToken = EMPTY_BYTE_ARRAY,
        pf = EMPTY_BYTE_ARRAY,
        pfKey = EMPTY_BYTE_ARRAY,
        da2 = EMPTY_BYTE_ARRAY,
        wtSessionTicket = KeyWithCreationTime(EMPTY_BYTE_ARRAY, creationTime),
        wtSessionTicketKey = EMPTY_BYTE_ARRAY,
        deviceToken = EMPTY_BYTE_ARRAY,
        encryptedDownloadSession = null
    )
}
