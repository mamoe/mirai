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
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.internal.contact.OtherClientImpl
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.StrangerInfoImpl
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.message.ForwardMessageInternal
import net.mamoe.mirai.internal.message.LongMessageInternal
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.handler.BdhSessionSyncer
import net.mamoe.mirai.internal.network.net.NetworkHandler
import net.mamoe.mirai.internal.network.net.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.net.impl.netty.NettyNetworkHandler
import net.mamoe.mirai.internal.network.net.protocol.SsoContext
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.utils.ScheduledJob
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.friendCacheFile
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RichMessage
import net.mamoe.mirai.utils.*
import java.io.File
import java.net.InetSocketAddress
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

@Suppress("INVISIBLE_MEMBER", "BooleanLiteralArgument", "OverridingDeprecatedMember")
internal class QQAndroidBot constructor(
    internal val account: BotAccount,
    configuration: BotConfiguration
) : AbstractBot(configuration, account.id), SsoContext {
    val bdhSyncer: BdhSessionSyncer = BdhSessionSyncer(this)

    ///////////////////////////////////////////////////////////////////////////
    // Account secrets cache
    ///////////////////////////////////////////////////////////////////////////

    // We cannot extract these logics until we rewrite the network framework.

    private val cacheDir: File by lazy {
        configuration.workingDir.resolve(bot.configuration.cacheDir).apply { mkdirs() }
    }
    internal val accountSecretsFile: File by lazy {
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

    override lateinit var client: QQAndroidClient


    override val bot: QQAndroidBot get() = this

    internal var firstLoginSucceed: Boolean = false

    inline val json get() = configuration.json

    override val friends: ContactList<Friend> = ContactList()

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

    override lateinit var nick: String

    override val asFriend: Friend by lazy {
        @OptIn(LowLevelApi::class)
        Mirai.newFriend(this, FriendInfoImpl(uin, nick, ""))
    }

    override val groups: ContactList<Group> = ContactList()

    // TODO: 2021/4/14         bdhSyncer.loadFromCache()  when login

    override suspend fun sendLogout() {
        network.sendWithoutExpect(StatSvc.Register.offline(client))
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): NetworkHandler {
        return NettyNetworkHandler(
            NetworkHandlerContextImpl(this, this),
            InetSocketAddress("123", 1) // TODO: 2021/4/14 address
        ) // TODO: 2021/4/14
    }

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
    override val asStranger: Stranger by lazy { Mirai.newStranger(bot, StrangerInfoImpl(bot.id, bot.nick)) }
    override val strangers: ContactList<Stranger> = ContactList()
}

internal val EMPTY_BYTE_ARRAY = ByteArray(0)

internal fun RichMessage.Key.longMessage(brief: String, resId: String, timeSeconds: Long): LongMessageInternal {
    val limited: String = if (brief.length > 30) {
        brief.take(30) + "…"
    } else {
        brief
    }

    val template = """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <msg serviceID="35" templateID="1" action="viewMultiMsg"
                     brief="$limited"
                     m_resid="$resId"
                     m_fileName="$timeSeconds" sourceMsgId="0" url=""
                     flag="3" adverSign="0" multiMsgFlag="1">
                    <item layout="1">
                        <title>$limited</title>
                        <hr hidden="false" style="0"/>
                        <summary>点击查看完整消息</summary>
                    </item>
                    <source name="聊天记录" icon="" action="" appid="-1"/>
                </msg>
            """.trimIndent().trim()

    return LongMessageInternal(template, resId)
}


internal fun RichMessage.Key.forwardMessage(
    resId: String,
    timeSeconds: Long,
    forwardMessage: ForwardMessage,
): ForwardMessageInternal = with(forwardMessage) {
    val template = """
        <?xml version="1.0" encoding="utf-8"?>
        <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="${brief.take(30)}"
             m_resid="$resId" m_fileName="$timeSeconds"
             tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">
            <item layout="1" advertiser_id="0" aid="0">
                <title size="34" maxLines="2" lineSpace="12">${title.take(50)}</title>
                ${
        when {
            preview.size > 4 -> {
                preview.take(3).joinToString("") {
                    """<title size="26" color="#777777" maxLines="2" lineSpace="12">$it</title>"""
                } + """<title size="26" color="#777777" maxLines="2" lineSpace="12">...</title>"""
            }
            else -> {
                preview.joinToString("") {
                    """<title size="26" color="#777777" maxLines="2" lineSpace="12">$it</title>"""
                }
            }
        }
    }
                <hr hidden="false" style="0"/>
                <summary size="26" color="#777777">${summary.take(50)}</summary>
            </item>
            <source name="${source.take(50)}" icon="" action="" appid="-1"/>
        </msg>
    """.trimIndent().replace("\n", " ").trim()
    return ForwardMessageInternal(template, resId, null)
}