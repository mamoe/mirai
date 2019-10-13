@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

/**
 * QQ 在线状态
 *
 * @author Him188moe
 * @see net.mamoe.mirai.network.protocol.tim.packet.login.ClientChangeOnlineStatusPacket
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


    companion object {
        fun ofId(id: UByte): OnlineStatus = values().first { it.id == id }
    }
}
