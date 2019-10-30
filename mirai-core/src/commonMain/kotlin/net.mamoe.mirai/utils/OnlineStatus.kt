@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

/**
 * QQ 在线状态
 *
 * @author Him188moe
 * @see net.mamoe.mirai.network.protocol.tim.packet.login.ChangeOnlineStatusPacket
 */
enum class OnlineStatus(
        val id: UByte//1 ubyte
) {
    /**
     * 我在线上
     */
    ONLINE(0x0Au),

    /**
     * 忙碌
     */
    BUSY(0x32u);


    // TODO: 2019/10/29  what is 0x20u

    companion object {
        fun ofId(id: UByte): OnlineStatus? = values().firstOrNull { it.id == id }
    }
}
