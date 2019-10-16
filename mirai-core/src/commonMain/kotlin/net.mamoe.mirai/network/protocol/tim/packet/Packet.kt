@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.utils.hexToUBytes


/**
 * 数据包
 */
abstract class Packet {
    open val idHex: String by lazy {
        this::class.annotations.filterIsInstance<PacketId>().firstOrNull()?.value?.trim() ?: ""
    }

    open val fixedId: String by lazy {
        when (this.idHex.length) {
            0 -> "__ __ __ __"
            2 -> this.idHex + " __ __ __"
            5 -> this.idHex + " __ __"
            7 -> this.idHex + " __"
            else -> this.idHex
        }
    }

    open val idByteArray: ByteArray by lazy {
        idHex.hexToUBytes().toByteArray()
    }
}

internal expect fun Packet.packetToString(): String