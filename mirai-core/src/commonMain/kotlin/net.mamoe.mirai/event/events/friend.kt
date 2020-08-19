/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:Suppress("FunctionName", "unused", "DEPRECATION_ERROR")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.internal.MiraiAtomicBoolean
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.utils.SinceMirai
import net.mamoe.mirai.utils.internal.runBlocking
import kotlin.jvm.*


/**
 * 好友昵称改变事件. 目前仅支持解析 (来自 PC 端的修改).
 */
public data class FriendRemarkChangeEvent internal constructor(
    public override val friend: Friend,
    public val newName: String
) : FriendEvent, Packet, AbstractEvent()

/**
 * 成功添加了一个新好友的事件
 */
public data class FriendAddEvent internal constructor(
    /**
     * 新好友. 已经添加到 [Bot.friends]
     */
    public override val friend: Friend
) : FriendEvent, Packet, AbstractEvent()

/**
 * 好友已被删除的事件.
 */
public data class FriendDeleteEvent internal constructor(
    public override val friend: Friend
) : FriendEvent, Packet, AbstractEvent()

/**
 * 一个账号请求添加机器人为好友的事件
 */
@Suppress("DEPRECATION")
public data class NewFriendRequestEvent internal constructor(
    public override val bot: Bot,
    /**
     * 事件唯一识别号
     */
    public val eventId: Long,
    /**
     * 申请好友消息
     */
    public val message: String,
    /**
     * 请求人 [User.id]
     */
    public val fromId: Long,
    /**
     * 来自群 [Group.id], 其他途径时为 0
     */
    public val fromGroupId: Long,
    /**
     * 群名片或好友昵称
     */
    public val fromNick: String
) : BotEvent, Packet, AbstractEvent() {
    @JvmField
    internal val responded: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

    /**
     * @return 申请人来自的群. 当申请人来自其他途径申请时为 `null`
     */
    public val fromGroup: Group? = if (fromGroupId == 0L) null else bot.getGroup(fromGroupId)

    @JvmSynthetic
    public suspend fun accept(): Unit = bot.acceptNewFriendRequest(this)

    @JvmSynthetic
    public suspend fun reject(blackList: Boolean = false): Unit = bot.rejectNewFriendRequest(this, blackList)


    @JavaFriendlyAPI
    @JvmName("accept")
    public fun __acceptBlockingForJava__(): Unit = runBlocking { accept() }

    @JavaFriendlyAPI
    @JvmOverloads
    @JvmName("reject")
    public fun __rejectBlockingForJava__(blackList: Boolean = false): Unit =
        runBlocking { reject(blackList) }
}


/**
 * [Friend] 头像被修改. 在此事件广播前就已经修改完毕.
 */
public data class FriendAvatarChangedEvent internal constructor(
    public override val friend: Friend
) : FriendEvent, Packet, AbstractEvent()



/**
 * [Friend] 昵称改变事件, 在此事件广播时好友已经完成改名
 * @see BotNickChangedEvent
 */
@SinceMirai("1.2.0")
public data class FriendNickChangedEvent internal constructor(
    public override val friend: Friend,
    public val from: String,
    public val to: String
) : FriendEvent, Packet, AbstractEvent()
  
/**
 * 好友输入状态改变的事件，当开始输入文字、退出聊天窗口或清空输入框时会触发此事件
 */
@SinceMirai("1.2.0")
public data class FriendInputStatusChangedEvent internal constructor(
    public override val friend: Friend,
    public val inputting: Boolean

) : FriendEvent, Packet, AbstractEvent()
