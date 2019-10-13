@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import net.mamoe.mirai.utils.OnlineStatus
import kotlin.properties.Delegates


/**
 * 好友在线状态改变
 */
@PacketId("00 81")
class ServerFieldOnlineStatusChangedPacket(input: ByteReadPacket) : ServerPacket(input) {
    var qq: UInt by Delegates.notNull()
    lateinit var status: OnlineStatus

    override fun decode() = with(input) {
        qq = readUInt()
        discardExact(8)
        status = OnlineStatus.ofId(readUByte())
    }

    //在线     XX XX XX XX 01 00 00 00 00 00 00 00 0A 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
    //忙碌     XX XX XX XX 01 00 00 00 00 00 00 00 32 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00

    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerFieldOnlineStatusChangedPacket = ServerFieldOnlineStatusChangedPacket(this.decryptBy(sessionKey)).setId(this.idHex)
    }
}