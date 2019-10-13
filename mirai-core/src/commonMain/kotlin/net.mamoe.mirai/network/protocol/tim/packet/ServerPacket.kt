@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes
import net.mamoe.mirai.utils.*


abstract class ServerPacket(val input: ByteReadPacket) : Packet(), Closeable {
    override var idHex: String = EMPTY_ID_HEX
        get() {
            if (field === EMPTY_ID_HEX) {
                idHex = (this::class.annotations.firstOrNull { it::class == PacketId::class } as? PacketId)?.value?.trim()
                        ?: ""
            }
            return field
        }

    var encoded: Boolean = false

    open fun decode() {

    }

    override fun close() = this.input.close()

    companion object {
        private const val EMPTY_ID_HEX = "EMPTY_ID_HEX"
    }


    override fun toString(): String = this.packetToString()

    fun getFixedId(id: String): String = when (id.length) {
        0 -> "__ __ __ __"
        2 -> "$id __ __ __"
        5 -> "$id __ __"
        7 -> "$id __"
        else -> id
    }

    fun decryptBy(key: ByteArray): ByteReadPacket {
        return ByteReadPacket(decryptAsByteArray(key))
    }

    fun decryptBy(key: IoBuffer): ByteReadPacket {
        return ByteReadPacket(decryptAsByteArray(key))
    }

    fun decryptBy(keyHex: String): ByteReadPacket {
        return this.decryptBy(keyHex.hexToBytes())
    }

    fun decryptBy(key1: ByteArray, key2: ByteArray): ByteReadPacket {
        return TEA.decrypt(this.decryptAsByteArray(key1), key2).toReadPacket()
    }


    fun decryptBy(key1: String, key2: ByteArray): ByteReadPacket {
        return this.decryptBy(key1.hexToBytes(), key2)
    }

    fun decryptBy(key1: String, key2: IoBuffer): ByteReadPacket {
        return this.decryptBy(key1.hexToBytes(), key2.readBytes())
    }


    fun decryptBy(key1: ByteArray, key2: String): ByteReadPacket {
        return this.decryptBy(key1, key2.hexToBytes())
    }


    fun decryptBy(keyHex1: String, keyHex2: String): ByteReadPacket {
        return this.decryptBy(keyHex1.hexToBytes(), keyHex2.hexToBytes())
    }

    fun decryptAsByteArray(key: ByteArray): ByteArray {
        return TEA.decrypt(input.readRemainingBytes().cutTail(1), key)
    }

    fun decryptAsByteArray(keyHex: String): ByteArray = this.decryptAsByteArray(keyHex.hexToBytes())
    fun decryptAsByteArray(buffer: IoBuffer): ByteArray = this.decryptAsByteArray(buffer.readBytes())
}

fun <P : ServerPacket> P.setId(idHex: String): P {
    this.idHex = idHex
    return this
}
