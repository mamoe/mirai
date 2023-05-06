/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("RequestEventData")
public sealed class RequestEventData {
    public abstract val eventId: Long

    @JvmBlockingBridge
    public abstract suspend fun accept(bot: Bot)

    @JvmBlockingBridge
    public abstract suspend fun reject(bot: Bot)

    @Serializable
    @SerialName("NewFriendRequest")
    public class NewFriendRequest
    @MiraiExperimentalApi public constructor(
        override val eventId: Long,

        public val requester: Long,
        public val requesterNick: String,

        public val fromGroupId: Long,

        public val message: String,
    ) : RequestEventData() {
        @OptIn(LowLevelApi::class)
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
        @OptIn(LowLevelApi::class)
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

        override fun toString(): String {
            return "NewFriendRequest(eventId=$eventId, fromGroupId=$fromGroupId, message=$message, requester=$requester, requesterNick=$requesterNick)"
        }
    }

    @Serializable
    @SerialName("BotInvitedJoinGroupRequest")
    public class BotInvitedJoinGroupRequest
    @MiraiExperimentalApi public constructor(
        override val eventId: Long,

        public val invitor: Long,
        public val invitorNick: String,

        public val groupId: Long,
        public val groupName: String,
    ) : RequestEventData() {
        override suspend fun accept(bot: Bot) {
            @OptIn(LowLevelApi::class)
            Mirai.solveBotInvitedJoinGroupRequestEvent(
                bot,
                eventId = eventId,
                invitorId = invitor,
                groupId = groupId,
                accept = true,
            )
        }

        override suspend fun reject(bot: Bot) {
            @OptIn(LowLevelApi::class)
            Mirai.solveBotInvitedJoinGroupRequestEvent(
                bot,
                eventId = eventId,
                invitorId = invitor,
                groupId = groupId,
                accept = false,
            )
        }

        override fun toString(): String {
            return "BotInvitedJoinGroupRequest(eventId=$eventId, invitor=$invitor, invitorNick='$invitorNick', groupId=$groupId, groupName='$groupName')"
        }

    }

    @Serializable
    @SerialName("MemberJoinRequest")
    public class MemberJoinRequest
    @MiraiExperimentalApi public constructor(
        override val eventId: Long,

        public val requester: Long,
        public val requesterNick: String,

        public val groupId: Long,
        public val groupName: String,
        public val invitor: Long = 0L, // 如果不为 0 则为邀请入群

        public val message: String,
    ) : RequestEventData() {
        override suspend fun accept(bot: Bot) {
            @OptIn(LowLevelApi::class)
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
            @OptIn(LowLevelApi::class)
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

        override fun toString(): String {
            return "MemberJoinRequest(eventId=$eventId, groupId=$groupId, groupName=$groupName, invitor=$invitor, message=$message, requester=$requester, requesterNick=$requesterNick)"
        }

    }


    public companion object Factory {
        @JvmStatic
        @JvmName("from")
        public fun NewFriendRequestEvent.toRequestEventData(): NewFriendRequest {
            @OptIn(MiraiExperimentalApi::class)
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
            @OptIn(MiraiExperimentalApi::class)
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
            @OptIn(MiraiExperimentalApi::class)
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
