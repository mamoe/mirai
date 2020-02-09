/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.MiraiExperimentalAPI


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
 * 是管理员或群主
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Member.isOperator(): Boolean = this.permission.isOperator()



/**
 * 权限不足
 */
class PermissionDeniedException : IllegalStateException {
    constructor() : super("Permission denied")
    constructor(message: String?) : super(message)
}

@UseExperimental(MiraiExperimentalAPI::class)
inline fun Group.checkBotPermission(
    required: MemberPermission,
    lazyMessage: () -> String = {
        "Permission denied: required $required, got actual $botPermission for $bot in group $id"
    }
) {
    if (botPermission != required) {
        throw PermissionDeniedException(lazyMessage())
    }
}

@UseExperimental(MiraiExperimentalAPI::class)
inline fun Group.checkBotPermissionOperator(
    lazyMessage: () -> String = {
        "Permission denied: required ${MemberPermission.ADMINISTRATOR} or ${MemberPermission.OWNER}, got actual $botPermission for $bot in group $id"
    }
) {
    if (!botPermission.isOperator()) {
        throw PermissionDeniedException(lazyMessage())
    }
}