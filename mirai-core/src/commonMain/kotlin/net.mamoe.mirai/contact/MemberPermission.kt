/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import kotlin.internal.InlineOnly

/**
 * 群成员的权限.
 *
 * 可通过 [compareTo] 判断是否有更高的权限.
 *
 * @see isOwner 判断权限是否为群主
 * @see isOperator 判断权限是否为管理员或群主
 *
 * @see Member.isOwner 对 [Member] 的扩展函数, 判断此成员是否为群主
 * @see Member.isOperator 对 [Member] 的扩展函数, 判断此成员是否为管理员或群主
 * @see Member.isAdministrator 对 [Member] 的扩展函数, 判断此成员是否为管理员
 */
enum class MemberPermission : Comparable<MemberPermission> {
    /**
     * 一般群成员
     */
    MEMBER, // ordinal = 0

    /**
     * 管理员
     */
    ADMINISTRATOR, // ordinal = 1

    /**
     * 群主
     */
    OWNER; // ordinal = 2

    /**
     * 权限等级. [OWNER] 为 2, [ADMINISTRATOR] 为 1, [MEMBER] 为 0
     */
    val level: Int
        get() = ordinal
}

/**
 * 判断权限是否为群主
 */
@InlineOnly
inline fun MemberPermission.isOwner(): Boolean = this == MemberPermission.OWNER

/**
 * 判断权限是否为管理员
 */
@InlineOnly
inline fun MemberPermission.isAdministrator(): Boolean = this == MemberPermission.ADMINISTRATOR

/**
 * 判断权限是否为管理员或群主
 */
@InlineOnly
inline fun MemberPermission.isOperator(): Boolean = isAdministrator() || isOwner()


/**
 * 判断权限是否为群主
 */
inline fun Member.isOwner(): Boolean = this.permission.isOwner()

/**
 * 判断权限是否为管理员
 */
inline fun Member.isAdministrator(): Boolean = this.permission.isAdministrator()

/**
 * 判断权限是否为管理员或群主
 */
inline fun Member.isOperator(): Boolean = this.permission.isOperator()


/**
 * 权限不足
 */
@Suppress("unused")
class PermissionDeniedException : IllegalStateException {
    constructor() : super("Permission denied")
    constructor(message: String?) : super(message)
}

/**
 * 要求 [Bot] 在这个群里的权限至少为 [required], 否则抛出异常 [PermissionDeniedException]
 *
 * @throws PermissionDeniedException
 */
inline fun Group.checkBotPermission(
    required: MemberPermission,
    crossinline lazyMessage: () -> String = {
        "Permission denied: required $required, got actual $botPermission for $bot in group $id"
    }
) {
    if (botPermission < required) {
        throw PermissionDeniedException(lazyMessage())
    }
}

/**
 * 要求 [Bot] 在这个群里的权限为 [管理员或群主][MemberPermission.isOperator], 否则抛出异常 [PermissionDeniedException]
 *
 * @throws PermissionDeniedException
 */
@Deprecated("use checkBotPermission", ReplaceWith("checkBotPermission(MemberPermission.ADMINISTRATOR)"))
inline fun Group.checkBotPermissionOperator(
    crossinline lazyMessage: () -> String = {
        "Permission denied: required ${MemberPermission.ADMINISTRATOR} or ${MemberPermission.OWNER}, got actual $botPermission for $bot in group $id"
    }
) = checkBotPermission(MemberPermission.ADMINISTRATOR, lazyMessage)