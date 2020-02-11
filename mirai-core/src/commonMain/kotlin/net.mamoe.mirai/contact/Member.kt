/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.MemberCardChangeEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.event.events.MemberMuteEvent
import net.mamoe.mirai.event.events.MemberSpecialTitleChangeEvent
import net.mamoe.mirai.utils.WeakRefProperty
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * 群成员.
 */
interface Member : QQ, Contact {
    /**
     * 所在的群.
     */
    @WeakRefProperty
    val group: Group

    /**
     * 成员的权限, 动态更新.
     */
    val permission: MemberPermission

    /**
     * 群名片. 可能为空. 修改时将会触发事件
     *
     * 在修改时将会异步上传至服务器. 无权限修改时将会抛出异常 [PermissionDeniedException]
     *
     * @see [groupCardOrNick] 获取非空群名片或昵称
     *
     * @see MemberCardChangeEvent 群名片被管理员, 自己或 [Bot] 改动事件
     */
    var nameCard: String

    /**
     * 群头衔
     *
     * 在修改时将会异步上传至服务器. 无权限修改时将会抛出异常 [PermissionDeniedException]
     *
     * @see MemberSpecialTitleChangeEvent 群名片被管理员, 自己或 [Bot] 改动事件
     */
    var specialTitle: String

    /**
     * 禁言
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 机器人无权限时返回 `false`
     *
     * @see Int.minutesToSeconds
     * @see Int.hoursToSeconds
     * @see Int.daysToSeconds
     */
    suspend fun mute(durationSeconds: Int): Boolean

    /**
     * 解除禁言. 机器人无权限时返回 `false`.
     *
     * @see MemberMuteEvent 成员被禁言事件.
     */
    suspend fun unmute(): Boolean

    /**
     * 踢出该成员. 机器人无权限时返回 `false`.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     */
    suspend fun kick(message: String = ""): Boolean

    /**
     * 当且仅当 `[other] is [Member] && [other].id == this.id && [other].group == this.group` 时为 true
     */
    override fun equals(other: Any?): Boolean
}

/**
 * 获取非空群名片或昵称.
 *
 * 若 [群名片][Member.nameCard] 不为空则返回群名片, 为空则返回 [QQ.nick]
 */
val Member.groupCardOrNick: String get() = this.nameCard.takeIf { it.isNotEmpty() } ?: this.nick

@ExperimentalTime
suspend inline fun Member.mute(duration: Duration): Boolean {
    require(duration.inDays <= 30) { "duration must be at most 1 month" }
    require(duration.inSeconds > 0) { "duration must be greater than 0 second" }
    return this.mute(duration.inSeconds.toInt())
}