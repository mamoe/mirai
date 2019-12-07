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
     * 所在的群
     */
    val group: Group

    /**
     * 权限
     */
    val permission: MemberPermission

    /**
     * 禁言
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 若机器人无权限禁言这个群成员, 返回 `false`
     *
     * @see Int.minutesToSeconds
     * @see Int.hoursToSeconds
     * @see Int.daysToSeconds
     */
    suspend fun mute(durationSeconds: Int): Boolean

    /**
     * 解除禁言
     */
    suspend fun unmute()
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