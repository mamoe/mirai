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

import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.context.SsoProcessorContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.selector.FactoryKeepAliveNetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.handler.state.LoggingStateObserver
import net.mamoe.mirai.internal.network.handler.state.SafeStateObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.handler.state.safe
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandlerFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.systemProp
import kotlin.contracts.contract

internal fun Bot.asQQAndroidBot(): QQAndroidBot {
    contract {
        returns() implies (this@asQQAndroidBot is QQAndroidBot)
    }

    return this as QQAndroidBot
}

internal class BotDebugConfiguration(
    var stateObserver: StateObserver? = when {
        systemProp("mirai.debug.network.state.observer.logging", false) ->
            SafeStateObserver(
                LoggingStateObserver(MiraiLogger.create("States")),
                MiraiLogger.create("LoggingStateObserver errors")
            )
        else -> null
    }
)

@Suppress("INVISIBLE_MEMBER", "BooleanLiteralArgument", "OverridingDeprecatedMember")
internal class QQAndroidBot constructor(
    internal val account: BotAccount,
    configuration: BotConfiguration,
    private val debugConfiguration: BotDebugConfiguration = BotDebugConfiguration(),
) : AbstractBot(configuration, account.id) {
    override val bot: QQAndroidBot get() = this

    internal var firstLoginSucceed: Boolean = false

    ///////////////////////////////////////////////////////////////////////////
    // network
    ///////////////////////////////////////////////////////////////////////////

    // TODO: 2021/4/14         bdhSyncer.loadFromCache()  when login

    // IDE error, don't move into lazy
    private fun ComponentStorage.stateObserverChain(): StateObserver {
        val components = this
        return StateObserver.chainOfNotNull(
            components[BotInitProcessor].asObserver().safe(networkLogger),
            debugConfiguration.stateObserver
        )
    }


    private val networkLogger: MiraiLogger by lazy { configuration.networkLoggerSupplier(this) }
    internal val components: ConcurrentComponentStorage by lazy {
        ConcurrentComponentStorage().apply {
            val components = this // avoid mistakes
            set(SsoProcessor, SsoProcessorImpl(SsoProcessorContextImpl(bot)))
            // put sso processor at the first to make `client` faster.

            set(BotInitProcessor, BotInitProcessorImpl(bot, components, bot.logger))
            set(ContactCacheService, ContactCacheServiceImpl(bot))
            set(ContactUpdater, ContactUpdaterImpl(bot, components, networkLogger))
            set(BdhSessionSyncer, BdhSessionSyncerImpl(configuration, networkLogger, components))
            set(ServerList, ServerListImpl())
            set(
                PacketHandler, PacketHandlerChain(
                    LoggingPacketHandler(bot, components, logger),
                    EventBroadcasterPacketHandler(bot, components, logger)
                )
            )
            set(PacketCodec, PacketCodecImpl())
            set(OtherClientUpdater, OtherClientUpdaterImpl(bot, components, bot.logger))
            set(ConfigPushSyncer, ConfigPushSyncerImpl())

            set(StateObserver, stateObserverChain())

            // TODO: 2021/4/16 load server list from cache (add a provider)
            // bot.bdhSyncer.loadServerListFromCache()

        }
    }

    val client get() = components[SsoProcessor].client

    override suspend fun sendLogout() {
        network.sendWithoutExpect(StatSvc.Register.offline(client))
    }

    override fun createNetworkHandler(): NetworkHandler {
        val context = NetworkHandlerContextImpl(
            this,
            networkLogger,
            components
        )
        return SelectorNetworkHandler(
            context,
            FactoryKeepAliveNetworkHandlerSelector(NettyNetworkHandlerFactory, context)
        ) // We can move the factory to configuration but this is not necessary for now.
    }


    suspend inline fun <E : Packet> OutgoingPacketWithRespType<E>.sendAndExpect(
        timeoutMillis: Long = 5000,
        retry: Int = 2
    ): E = network.run { sendAndExpect(timeoutMillis, retry) }

    suspend inline fun <E : Packet> OutgoingPacket.sendAndExpect(timeoutMillis: Long = 5000, retry: Int = 2): E =
        network.run { sendAndExpect(timeoutMillis, retry) }

    /**
     * 获取 获取群公告 所需的 bkn 参数
     * */
    val bkn: Int
        get() = client.wLoginSigInfo.sKey.data
            .fold(5381) { acc: Int, b: Byte -> acc + acc.shl(5) + b.toInt() }
            .and(Int.MAX_VALUE)

    ///////////////////////////////////////////////////////////////////////////
    // contacts
    ///////////////////////////////////////////////////////////////////////////

    override lateinit var nick: String

    @JvmField
    val groupListModifyLock = Mutex()

    // internally visible only
    fun getGroupByUin(uin: Long): Group {
        return getGroupByUinOrNull(uin)
            ?: throw NoSuchElementException("Group ${Mirai.calculateGroupCodeByGroupUin(uin)} not found")
    }

    fun getGroupByUinOrNull(uin: Long): Group? {
        return groups.firstOrNull { it.checkIsGroupImpl(); it.uin == uin }
    }
}
