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
    BUSY(0x32u),

    /**
     * 离线 ? 也可能是被删好友 TODO confirm that
     */
    OFFLINE(0x02u),

    UNKNOWN1(0x20u),
    UNKNOWN2(0x46u),
    UNKNOWN3(0x14u),
    UNKNOWN4(0xC9u),
    UNKNOWN5(0x1Eu),
    ;

    // TODO: 2019/10/29  what is 0x20u
    // TODO: 2019/11/11  what is 0x46u
    // TODO: 2019/11/11  what is 0x14u
    // TODO: 2019/11/11 0xC9u
    // TODO: 2019/11/11 0x1Eu
    companion object {
        fun ofId(id: UByte): OnlineStatus? = values().firstOrNull { it.id == id }
    }
}
