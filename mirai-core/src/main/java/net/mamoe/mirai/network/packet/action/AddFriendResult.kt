package net.mamoe.mirai.network.packet.action

/**
 * 添加好友结果
 */
enum class AddFriendResult {
    /**
     * 等待对方处理
     */
    WAITING_FOR_AGREEMENT,

    /**
     * 和对方已经是好友了
     */
    ALREADY_ADDED,

    /**
     * 对方设置为不添加好友等
     */
    FAILED,
}