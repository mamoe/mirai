@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.atomicfu.atomic
import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.writeHex

/**
 * 发给服务器的数据包. 必须有 [PacketId] 注解或 `override` [packetId]. 否则将会抛出 [IllegalStateException]
 */
abstract class ClientPacket : Packet(), Closeable {
    /**
     * Encode this packet.
     *
     * Before sending the packet, a [tail][TIMProtocol.tail] is added.
     */
    protected abstract fun encode(builder: BytePacketBuilder)

    override val sequenceId: UShort by lazy {
        atomicNextSequenceId()
    }

    companion object {
        private val sequenceIdInternal = atomic(1)
        internal fun atomicNextSequenceId() = sequenceIdInternal.getAndIncrement().toUShort()
    }

    /**
     * 务必 [ByteReadPacket.close] 或 [close] 或使用 [Closeable.use]
     */
    internal var packet: ByteReadPacket = UninitializedByteReadPacket
        get() {
            if (field === UninitializedByteReadPacket) build()
            return field
        }
        private set

    private fun build(): ByteReadPacket {
        packet = buildPacket {
            writeHex(TIMProtocol.head)
            writeHex(TIMProtocol.ver)
            writePacketId()
            encode(this)
            writeHex(TIMProtocol.tail)
        }
        return packet
    }

    override fun toString(): String = packetToString()

    override fun close() = if (this.packet === UninitializedByteReadPacket) Unit else this.packet.close()

    private fun BytePacketBuilder.writePacketId() {
        writeUShort(this@ClientPacket.id)
        writeUShort(sequenceId)
    }
}

private val UninitializedByteReadPacket = ByteReadPacket(IoBuffer.Empty, IoBuffer.EmptyPool)
