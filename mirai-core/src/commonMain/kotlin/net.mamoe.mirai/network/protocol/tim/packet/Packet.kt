@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.utils.io.toUHexString

/**
 * 数据包.
 */
abstract class Packet {
    /**
     * 2 Ubyte
     */
    open val id: UShort by lazy { (this::class.annotations.firstOrNull { it is PacketId } as? PacketId)?.value ?: error("Annotation PacketId not found") }

    /**
     * 包序列 id. 唯一
     */
    abstract val sequenceId: UShort

    val idHexString: String get() = (id.toInt().shl(16) or sequenceId.toInt()).toUHexString()
}

internal expect fun Packet.packetToString(): String