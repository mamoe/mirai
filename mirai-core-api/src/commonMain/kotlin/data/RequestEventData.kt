/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
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
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 用于中转各类 `RequestEvent` 的数据体,
 * 未来可能会添加新字段
 *
 * @since 2.7
 */
@MiraiExperimentalApi
@Serializable
@JvmBlockingBridge
// Don't add `data` modifier to class, for future fields
public /*data*/ class RequestEventData(
    public val eventId: Long,
    public val kind: Kind,

    /** 如果为 `0` 则不存在 */
    public val invitorId: Long = 0L,
    public val invitorNick: String? = null,

    /**
     * ```
     * [this: MemberJoinRequestEvent         ] -> fromId
     * [this: NewFriendRequestEvent          ] -> fromId
     * ```
     */
    public val requester: Long = 0L,
    /**
     * ```
     * [this: MemberJoinRequestEvent         ] -> fromNick
     * [this: NewFriendRequestEvent          ] -> fromNick
     * ```
     */
    public val requesterNick: String? = null,
    /**
     * ```
     * [this: MemberJoinRequestEvent         ] -> message
     * [this: NewFriendRequestEvent          ] -> message
     * ```
     */
    public val message: String? = null,


    /**
     * ```
     * [this: BotInvitedJoinGroupRequestEvent] -> groupId
     * [this: MemberJoinRequestEvent         ] -> groupId
     * [this: NewFriendRequestEvent          ] -> fromGroupId
     * ```
     */
    public val subjectId: Long = 0L,
    /**
     * ```
     * [this: BotInvitedJoinGroupRequestEvent] -> groupName
     * [this: MemberJoinRequestEvent         ] -> groupName
     * ```
     */
    public val subjectName: String? = null,
) {
    public enum class Kind {
        BotInvitedJoinGroupRequestEvent,
        MemberJoinRequestEvent,
        NewFriendRequestEvent,
    }

    public companion object {
        @JvmStatic
        @JvmName("from")
        public fun BotInvitedJoinGroupRequestEvent.toRequestEventData(): RequestEventData {
            return RequestEventData(
                eventId = eventId,
                kind = Kind.BotInvitedJoinGroupRequestEvent,
                invitorId = invitorId,
                invitorNick = invitorNick,
                subjectId = groupId,
                subjectName = groupName,
            )
        }

        @JvmStatic
        @JvmName("from")
        public fun MemberJoinRequestEvent.toRequestEventData(): RequestEventData {
            return RequestEventData(
                eventId = eventId,
                kind = Kind.MemberJoinRequestEvent,
                message = message,
                requester = fromId,
                requesterNick = fromNick,
                subjectId = groupId,
                subjectName = groupName,
            )
        }

        @JvmStatic
        @JvmName("from")
        public fun NewFriendRequestEvent.toRequestEventData(): RequestEventData {
            return RequestEventData(
                eventId = eventId,
                kind = Kind.NewFriendRequestEvent,
                requester = fromId,
                requesterNick = fromNick,
                message = message,
                subjectId = fromGroupId,
            )
        }
    }

    // Helper functions

    public suspend fun accept(bot: Bot) {
        when (kind) {
            Kind.BotInvitedJoinGroupRequestEvent -> {
                Mirai.solveBotInvitedJoinGroupRequestEvent(
                    bot,
                    eventId = eventId,
                    invitorId = invitorId,
                    groupId = subjectId,
                    accept = true
                )
            }
            Kind.MemberJoinRequestEvent -> {
                Mirai.solveMemberJoinRequestEvent(
                    bot,
                    eventId = eventId,
                    fromId = requester,
                    fromNick = requesterNick ?: "",
                    groupId = subjectId,
                    accept = true,
                    blackList = false,
                )
            }
            Kind.NewFriendRequestEvent -> {
                Mirai.solveNewFriendRequestEvent(
                    bot,
                    eventId = eventId,
                    fromId = requester,
                    fromNick = requesterNick ?: "",
                    accept = true,
                    blackList = false,
                )
            }
        }
    }

    public suspend fun reject(
        bot: Bot,
        /** Used only `MemberJoinRequestEvent`, `NewFriendRequestEvent` */
        blackList: Boolean = false,
        /** Used only `MemberJoinRequestEvent` */
        message: String = "",
    ) {
        when (kind) {
            Kind.BotInvitedJoinGroupRequestEvent -> {
                Mirai.solveBotInvitedJoinGroupRequestEvent(
                    bot,
                    eventId = eventId,
                    invitorId = invitorId,
                    groupId = subjectId,
                    accept = false,
                )
            }
            Kind.MemberJoinRequestEvent -> {
                Mirai.solveMemberJoinRequestEvent(
                    bot,
                    eventId = eventId,
                    fromId = requester,
                    fromNick = requesterNick ?: "",
                    groupId = subjectId,
                    accept = false,
                    blackList = blackList,
                    message = message,
                )
            }
            Kind.NewFriendRequestEvent -> {
                Mirai.solveNewFriendRequestEvent(
                    bot,
                    eventId = eventId,
                    fromId = requester,
                    fromNick = requesterNick ?: "",
                    accept = false,
                    blackList = blackList,
                )
            }
        }
    }

    /**
     * 仅在为 `MemberJoinRequestEvent` 时可调用
     */
    public suspend fun ignore(
        bot: Bot,
        blackList: Boolean = false,
    ) {
        when (kind) {
            Kind.BotInvitedJoinGroupRequestEvent -> {
                error("BotInvitedJoinGroupRequestEvent.ignore() not implemented")
            }
            Kind.MemberJoinRequestEvent -> {
                Mirai.solveMemberJoinRequestEvent(
                    bot,
                    eventId = eventId,
                    fromId = requester,
                    fromNick = requesterNick ?: "",
                    groupId = subjectId,
                    accept = null,
                    blackList = blackList,
                )
            }
            Kind.NewFriendRequestEvent -> {
                error("NewFriendRequestEvent.ignore() not implemented")
            }
        }
    }
}
