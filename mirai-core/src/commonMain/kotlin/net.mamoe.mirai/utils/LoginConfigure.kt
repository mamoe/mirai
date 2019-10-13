package net.mamoe.mirai.utils

import net.mamoe.mirai.network.protocol.tim.packet.login.ServerTouchResponsePacket

class LoginConfiguration {
    /**
     * 等待 [ServerTouchResponsePacket] 的时间
     */
    var touchTimeoutMillis: Long = 2000

    var randomDeviceName: Boolean = false

    companion object {
        val Default = LoginConfiguration()
    }
}


