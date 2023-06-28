/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.auth.AuthReason
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.contact.friendgroup.FriendGroupsImpl
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ComponentStorageDelegate
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.withFallback
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
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
import net.mamoe.mirai.internal.network.impl.ForceOfflineException
import net.mamoe.mirai.internal.network.notice.TraceLoggingNoticeProcessor
import net.mamoe.mirai.internal.network.notice.UnconsumedNoticesAlerter
import net.mamoe.mirai.internal.network.notice.decoders.GroupNotificationDecoder
import net.mamoe.mirai.internal.network.notice.decoders.MsgInfoDecoder
import net.mamoe.mirai.internal.network.notice.group.GroupMessageProcessor
import net.mamoe.mirai.internal.network.notice.group.GroupNotificationProcessor
import net.mamoe.mirai.internal.network.notice.group.GroupOrMemberListNoticeProcessor
import net.mamoe.mirai.internal.network.notice.group.GroupRecallProcessor
import net.mamoe.mirai.internal.network.notice.priv.FriendGroupNoticeProcessor
import net.mamoe.mirai.internal.network.notice.priv.FriendNoticeProcessor
import net.mamoe.mirai.internal.network.notice.priv.OtherClientNoticeProcessor
import net.mamoe.mirai.internal.network.notice.priv.PrivateMessageProcessor
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.internal.utils.ImagePatcherImpl
import net.mamoe.mirai.internal.utils.actualCacheDir
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.lateinitMutableProperty
import kotlin.contracts.contract
import kotlin.time.Duration.Companion.seconds

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
    override val friendGroups: FriendGroupsImpl by lazy { FriendGroupsImpl(this) }

    val client get() = components[SsoProcessor].client

    private val closing = atomic(false)
    override fun close(cause: Throwable?) {
        if (!this.isActive) return
        if (!closing.compareAndSet(false, true)) return

        if (networkInitialized) {
            runBlocking {
                try { // this may not be very good but
                    withTimeoutOrNull(5.seconds) {
                        components[SsoProcessor].logout(network)
                    }
                } catch (ignored: Exception) {
                }
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
            StateChangedObserver("BotOfflineEventBroadcasterAfter", State.OK, State.CLOSED) { new ->
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

                        // When bot is closed, eventDispatcher.isActive will be false.
                        // While in TestEventDispatcherImpl, eventDispatcher.isActive will always be true to enable catching the event.
                        if (eventDispatcher.isActive) {
                            eventDispatcher.broadcastAsync { BotOfflineEvent.Active(bot, cause) }
                        } else {
                            @OptIn(DelicateCoroutinesApi::class)
                            GlobalScope.launch {
                                BotOfflineEvent.Active(bot, cause).broadcast()
                            }
                        }
                    }
                }
            },
            StateChangedObserver("ReLoginCauseCatcher", State.OK, State.CLOSED) { new ->
                get(SsoProcessor).authReason = when (val cause = new.getCause()) {
                    is ForceOfflineException -> AuthReason.ForceOffline(bot, cause.message)
                    is StatSvc.ReqMSFOffline.MsfOfflineToken -> AuthReason.MsfOffline(bot, cause.message)
                    is NetworkException -> AuthReason.NetworkError(bot, cause.message)
                    else -> AuthReason.Unknown(bot, cause)
                }
            },
            StateChangedObserver("FirstLoginObserver", State.OK) {
                get(SsoProcessor).isFirstLogin = false
            }
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

        val pipelineLogger = networkLogger.subLogger("NoticeProcessor") //  shorten name
        set(
            NoticeProcessorPipeline,
            NoticeProcessorPipelineImpl.create(
                bot,
                MsgInfoDecoder(pipelineLogger.subLogger("MsgInfoDecoder")),
                GroupNotificationDecoder(),

                FriendNoticeProcessor(pipelineLogger.subLogger("FriendNoticeProcessor")),
                GroupOrMemberListNoticeProcessor(pipelineLogger.subLogger("GroupOrMemberListNoticeProcessor")),
                GroupMessageProcessor(pipelineLogger.subLogger("GroupMessageProcessor")),
                GroupNotificationProcessor(pipelineLogger.subLogger("GroupNotificationProcessor")),
                FriendGroupNoticeProcessor(pipelineLogger.subLogger("FriendGroupNoticeProcessor")),
                PrivateMessageProcessor(),
                OtherClientNoticeProcessor(),
                GroupRecallProcessor(),
                UnconsumedNoticesAlerter(pipelineLogger.subLogger("UnconsumedNoticesAlerter")),
                TraceLoggingNoticeProcessor(pipelineLogger.subLogger("TraceLoggingNoticeProcessor"))
            )
        )

        set(SsoProcessorContext, SsoProcessorContextImpl(bot))
        set(SsoProcessor, SsoProcessorImpl(get(SsoProcessorContext)))
        set(
            QRCodeLoginProcessor,
            QRCodeLoginProcessor.parse(get(SsoProcessorContext), networkLogger.subLogger("QRCodeLoginProcessor"))
        )

        val cacheValidator = CacheValidatorImpl(
            get(SsoProcessorContext),
            configuration.actualCacheDir().resolve("validator.bin"),
            networkLogger.subLogger("CacheValidator"),
        )
        set(CacheValidator, cacheValidator)

        set(HeartbeatProcessor, HeartbeatProcessorImpl())
        set(HeartbeatScheduler, TimeBasedHeartbeatSchedulerImpl(networkLogger.subLogger("HeartbeatScheduler")))
        set(HttpClientProvider, HttpClientProviderImpl())
        set(KeyRefreshProcessor, KeyRefreshProcessorImpl(networkLogger.subLogger("KeyRefreshProcessor")))
        set(ConfigPushProcessor, ConfigPushProcessorImpl(networkLogger.subLogger("ConfigPushProcessor")))
        set(BotOfflineEventMonitor, BotOfflineEventMonitorImpl())

        set(BotInitProcessor, BotInitProcessorImpl(bot, components, networkLogger.subLogger("BotInitProcessor")))
        set(ContactCacheService, ContactCacheServiceImpl(bot, networkLogger.subLogger("ContactCacheService")))
        set(ContactUpdater, ContactUpdaterImpl(bot, components, networkLogger.subLogger("ContactUpdater")))
        set(
            BdhSessionSyncer,
            BdhSessionSyncerImpl(configuration, components, networkLogger.subLogger("BotSessionSyncer")),
        )
        set(
            MessageSvcSyncer,
            MessageSvcSyncerImpl(bot, bot.coroutineContext, networkLogger.subLogger("MessageSvcSyncer")),
        )
        set(
            EcdhInitialPublicKeyUpdater,
            EcdhInitialPublicKeyUpdaterImpl(bot, networkLogger.subLogger("ECDHInitialPublicKeyUpdater")),
        )
        set(ServerList, ServerListImpl(networkLogger.subLogger("ServerList")))
        set(PacketLoggingStrategy, PacketLoggingStrategyImpl(bot))
        set(
            PacketHandler,
            PacketHandlerChain(
                EventBroadcasterPacketHandler(components),
                CallPacketFactoryPacketHandler(bot),
                LoggingPacketHandlerAdapter(get(PacketLoggingStrategy), networkLogger),
            ),
        )
        set(PacketCodec, PacketCodecImpl())
        set(
            OtherClientUpdater,
            OtherClientUpdaterImpl(bot, components, networkLogger.subLogger("OtherClientUpdater")),
        )
        set(ConfigPushSyncer, ConfigPushSyncerImpl())
        set(
            AccountSecretsManager,
            configuration.createAccountsSecretsManager(bot.logger.subLogger("AccountSecretsManager")),
        )
        set(ImagePatcher, ImagePatcherImpl())

        cacheValidator.register(get(AccountSecretsManager))
        cacheValidator.register(get(BdhSessionSyncer))

        set(
            EncryptServiceHolder, EncryptServiceHolderImpl(this@QQAndroidBot, get(SsoProcessorContext))
        )
    }

    /**
     * This would overrides those from [createBotLevelComponents]
     */
    open fun createNetworkLevelComponents(): ComponentStorage {
        return ConcurrentComponentStorage {
            set(BotClientHolder, BotClientHolderImpl(bot, networkLogger.subLogger("BotClientHolder")))
            set(SyncController, SyncControllerImpl())
            set(ClockHolder, ClockHolder())
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
                    createNetworkLevelComponents(),
                )
                NetworkHandlerFactory.getPlatformDefault().create(
                    context,
                    context[ServerList].pollAny().toSocketAddress(),
                )
            },
        ) // We can move the factory to configuration but this is not necessary for now.
    }
}

internal fun QQAndroidBot.getGroupByUinOrFail(uin: Long) =
    getGroupByUin(uin) ?: throw NoSuchElementException("group.uin=$uin")

internal fun QQAndroidBot.getGroupByUin(uin: Long) = groups.firstOrNull { it.uin == uin }

/**
 * uin first
 */
internal fun QQAndroidBot.getGroupByUinOrCode(uinOrCode: Long) =
    groups.firstOrNull { it.uin == uinOrCode } ?: groups.firstOrNull { it.id == uinOrCode }

/**
 * uin first
 */
internal fun QQAndroidBot.getGroupByUinOrCodeOrFail(uinOrCode: Long) =
    getGroupByUinOrCode(uinOrCode) ?: throw NoSuchElementException("group.code or uin=$uinOrCode")


/**
 * code first
 */
internal fun QQAndroidBot.getGroupByCodeOrUin(uinOrCode: Long) =
    groups.firstOrNull { it.id == uinOrCode } ?: groups.firstOrNull { it.uin == uinOrCode }
