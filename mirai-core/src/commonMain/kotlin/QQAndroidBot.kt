/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("EXPERIMENTAL_API_USAGE", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.internal

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.internal.contact.OtherClientImpl
import net.mamoe.mirai.internal.contact.StrangerInfoImpl
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.message.*
import net.mamoe.mirai.internal.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.packet.chat.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import net.mamoe.mirai.internal.network.protocol.data.jce.FriendInfo as JceFriendInfo

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
    account: BotAccount,
    configuration: BotConfiguration
) : AbstractBot<QQAndroidBotNetworkHandler>(configuration, account.id) {
    @Suppress("LeakingThis")
    val client: QQAndroidClient =
        QQAndroidClient(
            account,
            bot = this,
            device = configuration.deviceInfo?.invoke(this) ?: DeviceInfo.random()
        )
    internal var firstLoginSucceed: Boolean = false

    inline val json get() = configuration.json

    override val friends: ContactList<Friend> = ContactList()

    override lateinit var nick: String

    internal var selfInfo: JceFriendInfo? = null
        get() = field ?: error("selfInfo is not yet initialized")
        set(it) {
            checkNotNull(it)
            field = it
            nick = it.nick
        }

    override val asFriend: Friend by lazy {
        @OptIn(LowLevelApi::class)
        Mirai._lowLevelNewFriend(this, FriendInfoImpl(uin, nick, ""))
    }

    override val groups: ContactList<Group> = ContactList()

    /**
     * Final process for 'login'
     */
    @ThisApiMustBeUsedInWithConnectionLockBlock
    @Throws(LoginFailedException::class) // only
    override suspend fun relogin(cause: Throwable?) {
        client.useNextServers { host, port ->
            network.closeEverythingAndRelogin(host, port, cause, 0)
        }
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(coroutineContext, this)
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

    /**
     * 获取 获取群公告 所需的 bkn 参数
     * */
    val bkn: Int
        get() = client.wLoginSigInfo.sKey.data
            .fold(5381) { acc: Int, b: Byte -> acc + acc.shl(5) + b.toInt() }
            .and(Int.MAX_VALUE)
    override val asStranger: Stranger by lazy { Mirai._lowLevelNewStranger(bot, StrangerInfoImpl(bot.id, bot.nick)) }
    override val strangers: ContactList<Stranger> = ContactList()
}

internal val EMPTY_BYTE_ARRAY = ByteArray(0)

internal fun RichMessage.Key.longMessage(brief: String, resId: String, timeSeconds: Long): RichMessage {
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
            """.trimIndent()

    return LongMessage(template, resId)
}


internal fun RichMessage.Key.forwardMessage(
    resId: String,
    timeSeconds: Long,
    forwardMessage: ForwardMessage,
): ForwardMessageInternal = with(forwardMessage) {
    val template = """
        <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
        <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="$brief"
             m_resid="$resId" m_fileName="$timeSeconds"
             tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">
            <item layout="1" advertiser_id="0" aid="0">
                <title size="34" maxLines="2" lineSpace="12">$title</title>
                ${
        preview.joinToString("") {
            """<title size="26" color="#777777" maxLines="2" lineSpace="12">$it</title>"""
        }
    }
                <hr hidden="false" style="0"/>
                <summary size="26" color="#777777">$summary</summary>
            </item>
            <source name="$source" icon="" action="" appid="-1"/>
        </msg>
    """.trimIndent().replace("\n", " ")
    return ForwardMessageInternal(template)
}