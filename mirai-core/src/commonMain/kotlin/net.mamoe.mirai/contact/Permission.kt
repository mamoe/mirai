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
 * 时管理员或群主
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