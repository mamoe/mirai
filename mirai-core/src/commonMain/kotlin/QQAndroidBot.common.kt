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
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.message.*
import net.mamoe.mirai.internal.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.packet.chat.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmField
import net.mamoe.mirai.internal.network.protocol.data.jce.FriendInfo as JceFriendInfo

internal fun Bot.asQQAndroidBot(): QQAndroidBot {
    contract {
        returns() implies (this@asQQAndroidBot is QQAndroidBot)
    }

    return this as QQAndroidBot
}

@Suppress("INVISIBLE_MEMBER", "BooleanLiteralArgument", "OverridingDeprecatedMember")
internal class QQAndroidBot constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase(context, account, configuration)


internal abstract class QQAndroidBotBase constructor(
    context: Context,
    private val account: BotAccount,
    configuration: BotConfiguration
) : BotImpl<QQAndroidBotNetworkHandler>(context, configuration) {
    val client: QQAndroidClient =
        QQAndroidClient(
            context,
            account,
            bot = @Suppress("LeakingThis") this as QQAndroidBot,
            device = configuration.deviceInfo?.invoke(context) ?: DeviceInfo.random()
        )
    internal var firstLoginSucceed: Boolean = false

    override val id: Long
        get() = account.id

    inline val json get() = configuration.json

    override val friends: ContactList<Friend> = ContactList(LockFreeLinkedList())

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
        Mirai._lowLevelNewFriend(this, object : FriendInfo {
            override val uin: Long get() = this@QQAndroidBotBase.id
            override val nick: String get() = this@QQAndroidBotBase.nick
            override val remark: String get() = ""
        })
    }

    /**
     * Final process for 'login'
     */
    @ThisApiMustBeUsedInWithConnectionLockBlock
    @Throws(LoginFailedException::class) // only
    override suspend fun relogin(cause: Throwable?) {
        client.useNextServers { host, port ->
            network.closeEverythingAndRelogin(host, port, cause)
        }
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(coroutineContext, this as QQAndroidBot)
    }

    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    @JvmField
    val groupListModifyLock = Mutex()

    // internally visible only
    fun getGroupByUin(uin: Long): Group {
        return getGroupByUinOrNull(uin)
            ?: throw NoSuchElementException("Group ${Group.calculateGroupCodeByGroupUin(uin)} not found")
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
}

internal val EMPTY_BYTE_ARRAY = ByteArray(0)

private fun RichMessage.Templates.longMessage(brief: String, resId: String, timeSeconds: Long): RichMessage {
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


private fun RichMessage.Templates.forwardMessage(
    resId: String,
    timeSeconds: Long,
    preview: String,
    title: String,
    brief: String,
    source: String,
    summary: String
): ForwardMessageInternal {
    val template = """
        <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
        <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="$brief"
             m_resid="$resId" m_fileName="$timeSeconds"
             tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">
            <item layout="1" advertiser_id="0" aid="0">
                <title size="34" maxLines="2" lineSpace="12">$title</title>
                $preview
                <hr hidden="false" style="0"/>
                <summary size="26" color="#777777">$summary</summary>
            </item>
            <source name="$source" icon="" action="" appid="-1"/>
        </msg>
    """.trimIndent().replace("\n", " ")
    return ForwardMessageInternal(template)
}