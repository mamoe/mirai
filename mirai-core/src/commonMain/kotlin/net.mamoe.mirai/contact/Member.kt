@file:Suppress("unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.*
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
     * ====以下字段会在更新后异步更新到服务器====
     */

    /**
     * 群名片
     */
    var groupCard: String

    /**
     * 群头衔
     */
    var specialTitle: String

    /**
     * ====以上字段会在更新后异步更新到服务器====
     */

    /**
     * 禁言
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 仅当机器人无权限禁言这个群成员时返回 `false`
     *
     * @see Int.minutesToSeconds
     * @see Int.hoursToSeconds
     * @see Int.daysToSeconds
     */
    suspend fun mute(durationSeconds: Int): Boolean

    /**
     * 解除禁言. 在没有权限时会返回 `false`. 否则均返回 `true`.
     */
    suspend fun unmute(): Boolean

    /**
     * 当且仅当 `[other] is [Member] && [other].id == this.id && [other].group == this.group` 时为 true
     */
    override fun equals(other: Any?): Boolean
}

@ExperimentalTime
suspend inline fun Member.mute(duration: Duration): Boolean {
    require(duration.inDays <= 30) { "duration must be at most 1 month" }
    require(duration.inSeconds > 0) { "duration must be greater than 0 second" }
    return this.mute(duration.inSeconds.toInt())
}

@ExperimentalUnsignedTypes
suspend inline fun Member.mute(durationSeconds: UInt): Boolean {
    require(durationSeconds.toInt() <= 30 * 24 * 3600) { "duration must be at most 1 month" }
    return this.mute(durationSeconds.toInt()) // same bin rep.
}

/**
 * 群成员的权限
 */
enum class MemberPermission {
    /**
     * 群主
     */
    OWNER,
    /**
     * 管理员
     */
    ADMINISTRATOR,
    /**
     * 一般群成员
     */
    MEMBER;
}

/**
 * 是群主
 */
@Suppress("NOTHING_TO_INLINE")
inline fun MemberPermission.isOwner(): Boolean = this == MemberPermission.OWNER

/**
 * 是管理员
 */
@Suppress("NOTHING_TO_INLINE")
inline fun MemberPermission.isAdministrator(): Boolean = this == MemberPermission.ADMINISTRATOR

/**
 * 是管理员或群主
 */
@Suppress("NOTHING_TO_INLINE")
inline fun MemberPermission.isOperator(): Boolean = isAdministrator() || isOwner()


/**
 * 是群主
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Member.isOwner(): Boolean = this.permission.isOwner()

/**
 * 是管理员
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Member.isAdministrator(): Boolean = this.permission.isAdministrator()

/**
 * 时管理员或群主
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Member.isOperator(): Boolean = this.permission.isOperator()