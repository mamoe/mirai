@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.writeHex

//TODO 将序列 ID 从包 ID 中独立出来
abstract class ClientPacket : Packet(), Closeable {
    /**
     * Encode this packet.
     *
     * Before sending the packet, a [tail][TIMProtocol.tail] is added.
     */
    protected abstract fun encode(builder: BytePacketBuilder)

    companion object {
        @Suppress("PrivatePropertyName")
        private val UninitializedByteReadPacket = ByteReadPacket(IoBuffer.Empty, IoBuffer.EmptyPool)
    }

    /**
     * 务必 [ByteReadPacket.close] 或 [close] 或使用 [Closeable.use]
     */
    var packet: ByteReadPacket = UninitializedByteReadPacket
        get() {
            if (field === UninitializedByteReadPacket) build()
            return field
        }

    private fun build(): ByteReadPacket {
        packet = buildPacket {
            writeHex(TIMProtocol.head)
            writeHex(TIMProtocol.ver)
            writeHex(idHex)
            encode(this)
            writeHex(TIMProtocol.tail)
        }
        return packet
    }

    override fun toString(): String = packetToString()

    override fun close() = if (this.packet === UninitializedByteReadPacket) Unit else this.packet.close()
}