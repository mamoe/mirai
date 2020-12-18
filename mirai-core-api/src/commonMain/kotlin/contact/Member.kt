/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "UnusedImport")

package net.mamoe.mirai.contact

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.event.events.MemberMuteEvent
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.MemberDeprecatedApi
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.WeakRefProperty

/**
 * 代表一位群成员.
 *
 * 群成员可能是匿名群成员 [AnonymousMember]
 * 群成员可能也是好友, 但他们在对象类型上不同.
 * 群成员可以通过 [asFriend] 得到相关好友对象.
 *
 * ## 相关的操作
 * [Member.isFriend] 判断此成员是否为好友
 * [Member.isAnonymous] 判断此成员是否为匿名群成员
 * [Member.isNormal] 判断此成员是否为正常群成员
 */
@Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_SUPER_CLASS")
@OptIn(JavaFriendlyAPI::class)
public interface Member : User {
    /**
     * 所在的群.
     */
    @WeakRefProperty
    public val group: Group

    /**
     * 成员的权限, 动态更新.
     *
     * @see MemberPermissionChangeEvent 权限变更事件. 由群主或机器人的操作触发.
     */
    public val permission: MemberPermission

    /**
     * 群名片. 可能为空.
     *
     * @see [NormalMember.nameCard]
     * @see [AnonymousMember.nameCard]
     */
    public val nameCard: String

    /**
     * 群头衔.
     *
     * 为 [AnonymousMember] 时一定是 `"匿名"`
     *
     * @see [NormalMember.specialTitle]
     */
    public val specialTitle: String

    @MemberDeprecatedApi("仅 NormalMember 支持 muteTimeRemaining")
    @PlannedRemoval("2.0-M2")
    public val muteTimeRemaining: Int

    /**
     * 禁言.
     *
     * QQ 中最小操作和显示的时间都是一分钟.
     * 机器人可以实现精确到秒, 会被客户端显示为 1 分钟但不影响实际禁言时间.
     *
     * 管理员可禁言成员, 群主可禁言管理员和群员.
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 机器人无权限时返回 `false`
     *
     * @see NormalMember.isMuted 判断此成员是否正处于禁言状态中
     * @see NormalMember.unmute 取消禁言此成员
     *
     * @see Int.minutesToSeconds
     * @see Int.hoursToSeconds
     * @see Int.daysToSeconds
     *
     * @see MemberMuteEvent 成员被禁言事件
     *
     * @throws PermissionDeniedException 无权限修改时抛出
     */
    @JvmBlockingBridge
    public suspend fun mute(durationSeconds: Int)

    @MemberDeprecatedApi("仅 NormalMember 支持 unmute")
    @PlannedRemoval("2.0-M2")
    public suspend fun unmute()

    @MemberDeprecatedApi("仅 NormalMember 支持 kick")
    @PlannedRemoval("2.0-M2")
    public suspend fun kick(message: String = "")

    @MemberDeprecatedApi("仅 NormalMember 支持 sendMessage")
    public override suspend fun sendMessage(message: Message): MessageReceipt<Member>

    @MemberDeprecatedApi("仅 NormalMember 支持 sendMessage")
    public override suspend fun sendMessage(message: String): MessageReceipt<Member>

    @MemberDeprecatedApi("仅 NormalMember 支持 nudge")
    @PlannedRemoval("2.0-M2")
    @MiraiExperimentalApi
    public override fun nudge(): MemberNudge
}

/**
 * 得到此成员作为好友的对象.
 *
 * @throws IllegalStateException 当此成员不是好友时抛出
 */
public fun Member.asFriend(): Friend = this.bot.getFriend(this.id) ?: error("$this is not a friend")

/**
 * 得到此成员作为好友的对象, 当此成员不是好友时返回 `null`
 */
public fun Member.asFriendOrNull(): Friend? = this.bot.getFriend(this.id)

/**
 * 判断此成员是否为好友
 */
public inline val Member.isFriend: Boolean
    get() = this.bot.friends.contains(this.id)

/**
 * 如果此成员是好友, 则执行 [block] 并返回其返回值. 否则返回 `null`
 */
@Deprecated(
    "Ambiguous function name and its behaviour. Use asFriendOrNull and let manually.",
    ReplaceWith("this.asFriendOrNull()?.let(block)")
)
@PlannedRemoval("2.0-M2")
public inline fun <R> Member.takeIfIsFriend(block: (Friend) -> R): R? {
    return this.asFriendOrNull()?.let(block)
}

/**
 * 获取非空群名片或昵称.
 *
 * 若 [群名片][Member.nameCard] 不为空则返回群名片, 为空则返回 [User.nick]
 */
public val Member.nameCardOrNick: String get() = this.nameCard.takeIf { it.isNotEmpty() } ?: this.nick
public val Member.isNormal: Boolean get() = this is NormalMember
public val Member.isAnonymous: Boolean get() = this is AnonymousMember
