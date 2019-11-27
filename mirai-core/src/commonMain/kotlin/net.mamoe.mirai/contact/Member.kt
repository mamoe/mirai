package net.mamoe.mirai.contact

/**
 * 群成员.
 *
 * 使用 [QQ.equals]. 因此同 ID 的群成员和 QQ 是 `==` 的
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
    OPERATOR,
    /**
     * 一般群成员
     */
    MEMBER;
}
