package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket

/**
 * 一个包的数据 (body)
 */
interface Packet

object IgnoredPacket : Packet

/**
 * 未知的包.
 */
class UnknownPacket(val id: PacketId, val body: ByteReadPacket) : Packet

/**
 * 仅用于替换类型应为 [Unit] 的情况
 */
object NoPacket : Packet