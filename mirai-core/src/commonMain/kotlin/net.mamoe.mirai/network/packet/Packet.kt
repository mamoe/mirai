package net.mamoe.mirai.network.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.utils.io.toUHexString

/**
 * 被忽略的数据包.
 */
inline class IgnoredPacket(internal val id: PacketId) : Packet

/**
 * 未知的包.
 */
class UnknownPacket(val id: PacketId, val body: ByteReadPacket) : Packet {
    override fun toString(): String = "UnknownPacket(${id.value.toUHexString()})\nbody=${body.readBytes().toUHexString()}"
}

/**
 * 仅用于替换类型应为 [Unit] 的情况
 */
object NoPacket : Packet {
    override fun toString(): String = "NoPacket"
}
