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
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.internal.contact.OtherClientImpl
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.handler.*
import net.mamoe.mirai.internal.network.handler.impl.LoggingStateObserver
import net.mamoe.mirai.internal.network.handler.impl.SafeStateObserver
import net.mamoe.mirai.internal.network.handler.impl.StateObserver
import net.mamoe.mirai.internal.network.handler.impl.netty.NettyNetworkHandlerFactory
import net.mamoe.mirai.internal.network.net.protocol.SsoProcessor
import net.mamoe.mirai.internal.network.net.protocol.SsoProcessorContextImpl
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.utils.ScheduledJob
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.friendCacheFile
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.*
import java.io.File
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal fun Bot.asQQAndroidBot(): QQAndroidBot {
    contract {
        returns() implies (this@asQQAndroidBot is QQAndroidBot)
    }

    return this as QQAndroidBot
}

internal fun QQAndroidBot.createOtherClient(
    info: OtherClientInfo,
): OtherClientImpl {
    return OtherClientImpl(this, coroutineContext, info)
}

internal class BotDebugConfiguration(
    var stateObserver: StateObserver? = when {
        systemProp("mirai.debug.network.state.observer.logging", false) ->
            SafeStateObserver(
                LoggingStateObserver(MiraiLogger.create("States")),
                MiraiLogger.create("StateObserver errors")
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

    val bdhSyncer: BdhSessionSyncer = BdhSessionSyncer(this)
    internal var firstLoginSucceed: Boolean = false

    ///////////////////////////////////////////////////////////////////////////
    // network
    ///////////////////////////////////////////////////////////////////////////

    // TODO: 2021/4/14         bdhSyncer.loadFromCache()  when login

    private val ssoProcessor: SsoProcessor by lazy { SsoProcessor(SsoProcessorContextImpl(this)) }

    val client get() = ssoProcessor.client

    override suspend fun sendLogout() {
        network.sendWithoutExpect(StatSvc.Register.offline(client))
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): NetworkHandler {
        val context = NetworkHandlerContextImpl(
            this,
            ssoProcessor,
            configuration.networkLoggerSupplier(this),
            debugConfiguration.stateObserver
        )
        return SelectorNetworkHandler(
            context,
            FactoryKeepAliveNetworkHandlerSelector(NettyNetworkHandlerFactory, serverListNew, context)
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


    ///////////////////////////////////////////////////////////////////////////
    // contact cache
    ///////////////////////////////////////////////////////////////////////////

    inline val json get() = configuration.json

    val friendListCache: FriendListCache? by lazy {
        if (!configuration.contactListCache.friendListCacheEnabled) return@lazy null
        val file = configuration.friendCacheFile()
        val ret = file.loadNotBlankAs(FriendListCache.serializer(), JsonForCache) ?: FriendListCache()

        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        bot.eventChannel.parentScope(this@QQAndroidBot)
            .subscribeAlways<net.mamoe.mirai.event.events.FriendInfoChangeEvent> {
                friendListSaver?.notice()
            }
        ret
    }

    val groupMemberListCaches: GroupMemberListCaches? by lazy {
        if (!configuration.contactListCache.groupMemberListCacheEnabled) {
            return@lazy null
        }
        GroupMemberListCaches(this)
    }

    private val friendListSaver: ScheduledJob? by lazy {
        if (!configuration.contactListCache.friendListCacheEnabled) return@lazy null
        ScheduledJob(coroutineContext, configuration.contactListCache.saveIntervalMillis) {
            runBIO { saveFriendCache() }
        }
    }

    fun saveFriendCache() {
        val friendListCache = friendListCache ?: return

        configuration.friendCacheFile().run {
            createFileIfNotExists()
            writeText(JsonForCache.encodeToString(FriendListCache.serializer(), friendListCache))
            bot.network.context.logger.info { "Saved ${friendListCache.list.size} friends to local cache." }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Account secrets cache
    ///////////////////////////////////////////////////////////////////////////

    // We cannot extract these logics until we rewrite the network framework.

    private val cacheDir: File by lazy {
        configuration.workingDir.resolve(bot.configuration.cacheDir).apply { mkdirs() }
    }
    private val accountSecretsFile: File by lazy {
        cacheDir.resolve("account.secrets")
    }

    private fun saveSecrets(secrets: AccountSecretsImpl) {
        if (secrets.wLoginSigInfoField == null) return

        accountSecretsFile.writeBytes(
            TEA.encrypt(
                secrets.toByteArray(AccountSecretsImpl.serializer()),
                account.passwordMd5
            )
        )

        network.context.logger.info { "Saved account secrets to local cache for fast login." }
    }

    init {
        if (configuration.loginCacheEnabled) {
            eventChannel.parentScope(this).subscribeAlways<WtLogin.Login.LoginPacketResponse> { event ->
                if (event is WtLogin.Login.LoginPacketResponse.Success) {
                    if (client.wLoginSigInfoInitialized) {
                        saveSecrets(AccountSecretsImpl(client))
                    }
                }
            }
        }
    }

    /////////////////////////// accounts secrets end

}
