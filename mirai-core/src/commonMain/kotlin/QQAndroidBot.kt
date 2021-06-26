/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("EXPERIMENTAL_API_USAGE", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.internal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ComponentStorageDelegate
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.withFallback
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport.BaseStateImpl
import net.mamoe.mirai.internal.network.handler.selector.KeepAliveNetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.handler.state.CombinedStateObserver.Companion.plus
import net.mamoe.mirai.internal.network.handler.state.LoggingStateObserver
import net.mamoe.mirai.internal.network.handler.state.StateChangedObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.handler.state.safe
import net.mamoe.mirai.internal.network.impl.netty.ForceOfflineException
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandlerFactory
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.lateinitMutableProperty
import kotlin.contracts.contract

internal fun Bot.asQQAndroidBot(): QQAndroidBot {
    contract {
        returns() implies (this@asQQAndroidBot is QQAndroidBot)
    }

    return this as QQAndroidBot
}

@Suppress("INVISIBLE_MEMBER", "BooleanLiteralArgument", "OverridingDeprecatedMember")
internal open class QQAndroidBot constructor(
    internal val account: BotAccount,
    configuration: BotConfiguration,
) : AbstractBot(configuration, account.id) {
    override val bot: QQAndroidBot get() = this
    val client get() = components[SsoProcessor].client

    override fun close(cause: Throwable?) {
        if (!this.isActive) return
        runBlocking {
            try { // this may not be very good but
                components[SsoProcessor].logout(network)
            } catch (ignored: Exception) {
            }
        }
        super.close(cause)
    }

    ///////////////////////////////////////////////////////////////////////////
    // network
    ///////////////////////////////////////////////////////////////////////////

    // also called by tests.
    fun ComponentStorage.stateObserverChain(): StateObserver {
        val components = this
        val eventDispatcher = this[EventDispatcher]
        return StateObserver.chainOfNotNull(
            components[BotInitProcessor].asObserver(),
            object : StateChangedObserver(State.OK) {
                private val shouldBroadcastRelogin = atomic(false)

                override fun stateChanged0(
                    networkHandler: NetworkHandlerSupport,
                    previous: BaseStateImpl,
                    new: BaseStateImpl,
                ) {
                    eventDispatcher.broadcastAsync(BotOnlineEvent(bot)).thenBroadcast(eventDispatcher) {
                        if (!shouldBroadcastRelogin.compareAndSet(false, true)) {
                            BotReloginEvent(bot, new.getCause())
                        } else null
                    }
                }

                override fun toString(): String = "StateChangedObserver(BotOnlineEventBroadcaster)"
            },
            StateChangedObserver("LastConnectedAddressUpdater", State.OK) {
                components[ServerList].run {
                    lastConnectedIP = getLastPolledIP()
                }
            },
            StateChangedObserver("LastDisconnectedAddressUpdater", State.CLOSED) {
                components[ServerList].run {
                    lastDisconnectedIP = lastConnectedIP
                }
            },
            StateChangedObserver("BotOfflineEventBroadcaster", State.OK, State.CLOSED) { new ->
                // logging performed by BotOfflineEventMonitor
                val cause = new.getCause()
                when {
                    cause is ForceOfflineException -> {
                        eventDispatcher.broadcastAsync(BotOfflineEvent.Force(bot, cause.title, cause.message))
                    }
                    cause is StatSvc.ReqMSFOffline.MsfOfflineToken -> {
                        eventDispatcher.broadcastAsync(BotOfflineEvent.MsfOffline(bot, cause))
                    }
                    cause is NetworkException && cause.recoverable -> {
                        eventDispatcher.broadcastAsync(BotOfflineEvent.Dropped(bot, cause))
                    }
                    cause is BotClosedByEvent -> {
                    }
                    else -> {
                        // any other unexpected exceptions considered as an error
                        eventDispatcher.broadcastAsync(BotOfflineEvent.Active(bot, cause))
                    }
                }
            },
        ).safe(logger.subLogger("StateObserver")) + LoggingStateObserver.createLoggingIfEnabled()
    }


    private val networkLogger: MiraiLogger by lazy { configuration.networkLoggerSupplier(this) }
    final override val components: ComponentStorage get() = network.context

    private val defaultBotLevelComponents: ComponentStorage by lateinitMutableProperty {
        createBotLevelComponents().apply {
            set(StateObserver, stateObserverChain())
        }.also { components ->
            components[BotOfflineEventMonitor].attachJob(bot, this)
        }
    }

    open fun createBotLevelComponents(): ConcurrentComponentStorage = ConcurrentComponentStorage {
        val components = ComponentStorageDelegate { this@QQAndroidBot.components }

        // There's no need to interrupt a broadcasting event when network handler closed.
        set(EventDispatcher, EventDispatcherImpl(bot.coroutineContext, logger.subLogger("EventDispatcher")))
        set(NoticeProcessorPipeline, NoticeProcessorPipelineImpl(networkLogger.subLogger("NoticeProcessorPipeline")))

        set(SsoProcessorContext, SsoProcessorContextImpl(bot))
        set(SsoProcessor, SsoProcessorImpl(get(SsoProcessorContext)))
        set(HeartbeatProcessor, HeartbeatProcessorImpl())
        set(HeartbeatScheduler, TimeBasedHeartbeatSchedulerImpl(networkLogger.subLogger("HeartbeatScheduler")))
        set(KeyRefreshProcessor, KeyRefreshProcessorImpl(networkLogger.subLogger("KeyRefreshProcessor")))
        set(ConfigPushProcessor, ConfigPushProcessorImpl(networkLogger.subLogger("ConfigPushProcessor")))
        set(BotOfflineEventMonitor, BotOfflineEventMonitorImpl())

        set(BotInitProcessor, BotInitProcessorImpl(bot, components, networkLogger.subLogger("BotInitProcessor")))
        set(ContactCacheService, ContactCacheServiceImpl(bot, networkLogger.subLogger("ContactCacheService")))
        set(ContactUpdater, ContactUpdaterImpl(bot, components, networkLogger.subLogger("ContactUpdater")))
        set(
            BdhSessionSyncer,
            BdhSessionSyncerImpl(configuration, components, networkLogger.subLogger("BotSessionSyncer"))
        )
        set(
            MessageSvcSyncer,
            MessageSvcSyncerImpl(bot, bot.coroutineContext, networkLogger.subLogger("MessageSvcSyncer"))
        )
        set(
            EcdhInitialPublicKeyUpdater,
            EcdhInitialPublicKeyUpdaterImpl(bot, networkLogger.subLogger("ECDHInitialPublicKeyUpdater"))
        )
        set(ServerList, ServerListImpl(networkLogger.subLogger("ServerList")))
        set(PacketLoggingStrategy, PacketLoggingStrategyImpl(bot))
        set(
            PacketHandler, PacketHandlerChain(
                LoggingPacketHandlerAdapter(get(PacketLoggingStrategy), networkLogger),
                EventBroadcasterPacketHandler(components),
                CallPacketFactoryPacketHandler(bot)
            )
        )
        set(PacketCodec, PacketCodecImpl())
        set(
            OtherClientUpdater,
            OtherClientUpdaterImpl(bot, components, networkLogger.subLogger("OtherClientUpdater"))
        )
        set(ConfigPushSyncer, ConfigPushSyncerImpl())
        set(
            AccountSecretsManager,
            configuration.createAccountsSecretsManager(bot.logger.subLogger("AccountSecretsManager"))
        )
    }

    /**
     * This would overrides those from [createBotLevelComponents]
     */
    open fun createNetworkLevelComponents(): ComponentStorage {
        return ConcurrentComponentStorage {
            set(BotClientHolder, BotClientHolderImpl(bot, networkLogger.subLogger("BotClientHolder")))
        }.withFallback(defaultBotLevelComponents)
    }

    override fun createNetworkHandler(): NetworkHandler {
        return SelectorNetworkHandler(
            KeepAliveNetworkHandlerSelector(
                maxAttempts = configuration.reconnectionRetryTimes.coerceIn(1, Int.MAX_VALUE),
                logger = networkLogger.subLogger("Selector")
            ) {
                val context = NetworkHandlerContextImpl(
                    bot,
                    networkLogger,
                    createNetworkLevelComponents()
                )
                NettyNetworkHandlerFactory.create(
                    context,
                    context[ServerList].pollAny().toSocketAddress()
                )
            }
        ) // We can move the factory to configuration but this is not necessary for now.
    }

    /**
     * 获取 获取群公告 所需的 bkn 参数
     * */ // TODO: 2021/4/26 extract it after #1141 merged
    val bkn: Int
        get() = client.wLoginSigInfo.sKey.data
            .fold(5381) { acc: Int, b: Byte -> acc + acc.shl(5) + b.toInt() }
            .and(Int.MAX_VALUE)

    ///////////////////////////////////////////////////////////////////////////
    // contacts
    ///////////////////////////////////////////////////////////////////////////

    override lateinit var nick: String
}

internal fun QQAndroidBot.getGroupByUinOrFail(uin: Long) =
    getGroupByUin(uin) ?: throw NoSuchElementException("group.uin=$uin")

internal fun QQAndroidBot.getGroupByUin(uin: Long) = groups.firstOrNull { it.uin == uin }
