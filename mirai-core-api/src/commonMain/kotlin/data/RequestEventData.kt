/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.data

import kotlinx.serialization.Serializable
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent

@Serializable
public sealed class RequestEventData {
    public abstract val eventId: Long

    @JvmBlockingBridge
    public abstract suspend fun accept(bot: Bot)

    @JvmBlockingBridge
    public abstract suspend fun reject(bot: Bot)

    @Serializable
    public data class NewFriendRequest(
        override val eventId: Long,

        val requester: Long,
        val requesterNick: String,

        val fromGroupId: Long,

        val message: String,
    ) : RequestEventData() {
        override suspend fun accept(bot: Bot) {
            Mirai.solveNewFriendRequestEvent(
                bot,
                eventId = eventId,
                fromId = requester,
                fromNick = requesterNick,
                accept = true,
                blackList = false,
            )
        }

        override suspend fun reject(bot: Bot) {
            reject(bot, false)
        }

        @JvmBlockingBridge
        public suspend fun reject(bot: Bot, blackList: Boolean) {
            Mirai.solveNewFriendRequestEvent(
                bot,
                eventId = eventId,
                fromId = requester,
                fromNick = requesterNick,
                accept = false,
                blackList = blackList,
            )
        }

    }

    @Serializable
    public data class BotInvitedJoinGroupRequest(
        override val eventId: Long,

        val invitor: Long,
        val invitorNick: String,

        val groupId: Long,
        val groupName: String,
    ) : RequestEventData() {
        override suspend fun accept(bot: Bot) {
            Mirai.solveBotInvitedJoinGroupRequestEvent(
                bot,
                eventId = eventId,
                invitorId = invitor,
                groupId = groupId,
                accept = true,
            )
        }

        override suspend fun reject(bot: Bot) {
            Mirai.solveBotInvitedJoinGroupRequestEvent(
                bot,
                eventId = eventId,
                invitorId = invitor,
                groupId = groupId,
                accept = false,
            )
        }
    }

    @Serializable
    public data class MemberJoinRequest(
        override val eventId: Long,

        val requester: Long,
        val requesterNick: String,

        val groupId: Long,
        val groupName: String,
        val invitor: Long = 0L, // 如果不为 0 则为邀请入群

        val message: String,
    ) : RequestEventData() {
        override suspend fun accept(bot: Bot) {
            Mirai.solveMemberJoinRequestEvent(
                bot,
                eventId = eventId,
                fromId = requester,
                fromNick = requesterNick,
                groupId = groupId,
                accept = true,
                blackList = false,
                message = "",
            )
        }

        override suspend fun reject(bot: Bot) {
            reject(bot, false)
        }

        @JvmBlockingBridge
        public suspend fun reject(bot: Bot, message: String) {
            reject(bot, false, message)
        }

        @JvmBlockingBridge
        @JvmOverloads
        public suspend fun reject(bot: Bot, blackList: Boolean, message: String = "") {
            Mirai.solveMemberJoinRequestEvent(
                bot,
                eventId = eventId,
                fromId = requester,
                fromNick = requesterNick,
                groupId = groupId,
                accept = false,
                blackList = blackList,
                message = message,
            )
        }
    }


    public companion object Factory {
        @JvmStatic
        @JvmName("from")
        public fun NewFriendRequestEvent.toReqeustEventData(): NewFriendRequest {
            return NewFriendRequest(
                eventId = eventId,
                message = message,
                requester = fromId,
                requesterNick = fromNick,
                fromGroupId = fromGroupId,
            )
        }

        @JvmStatic
        @JvmName("from")
        public fun BotInvitedJoinGroupRequestEvent.toRequestEventData(): BotInvitedJoinGroupRequest {
            return BotInvitedJoinGroupRequest(
                eventId = eventId,
                invitor = invitorId,
                invitorNick = invitorNick,
                groupId = groupId,
                groupName = groupName,
            )
        }

        @JvmStatic
        @JvmName("from")
        public fun MemberJoinRequestEvent.toRequestEventData(): MemberJoinRequest {
            return MemberJoinRequest(
                eventId = eventId,
                requester = fromId,
                requesterNick = fromNick,
                groupId = groupId,
                groupName = groupName,
                invitor = invitorId ?: 0L,
                message = message,
            )
        }
    }
}
