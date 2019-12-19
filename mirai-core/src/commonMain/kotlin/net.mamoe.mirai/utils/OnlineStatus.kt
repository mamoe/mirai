@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmStatic

/**
 * QQ 在线状态
 *
 * @author Him188moe
 * @see net.mamoe.mirai.timpc.network.packet.login.ChangeOnlineStatusPacket
 */
inline class OnlineStatus(
    inline val id: UByte
) {
    companion object {
        /**
         * 我在线上
         */
        @JvmStatic
        val ONLINE = OnlineStatus(0x0Au)

        /**
         * 忙碌
         */
        @JvmStatic
        val BUSY = OnlineStatus(0x32u)

        /**
         * 离线 ? 也可能是被删好友 TODO confirm that
         */
        @JvmStatic
        val OFFLINE = OnlineStatus(0x02u)

        @JvmStatic
        val UNKNOWN1 = OnlineStatus(0x20u)
        @JvmStatic
        val UNKNOWN2 = OnlineStatus(0x46u)
        @JvmStatic
        val UNKNOWN3 = OnlineStatus(0x14u)
        @JvmStatic
        val UNKNOWN4 = OnlineStatus(0xC9u)
        @JvmStatic
        val UNKNOWN5 = OnlineStatus(0x1Eu)
    }

    // TODO: 2019/10/29  what is 0x20u
    // TODO: 2019/11/11  what is 0x46u
    // TODO: 2019/11/11  what is 0x14u
    // TODO: 2019/11/11 0xC9u
    // TODO: 2019/11/11 0x1Eu
}
