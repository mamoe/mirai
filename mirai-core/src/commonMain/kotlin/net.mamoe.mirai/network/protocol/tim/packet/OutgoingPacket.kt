@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.atomicfu.atomic
import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.io.writeHex
import kotlin.jvm.JvmStatic

/**
 * 发给服务器的数据包. 必须有 [PacketId] 注解或 `override` [id]. 否则将会抛出 [IllegalStateException]
 */
abstract class OutgoingPacket : Packet(), Closeable {
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
        @JvmStatic
        private val sequenceIdInternal = atomic(1)

        internal fun atomicNextSequenceId() = sequenceIdInternal.getAndIncrement().toUShort()
    }

    inline fun buildAndUse(block: (ByteReadPacket) -> Unit) {
        buildPacket().use(block)
    }

    @PublishedApi
    internal fun buildPacket(): ByteReadPacket = buildPacket {
        writeHex(TIMProtocol.head)
        writeHex(TIMProtocol.ver)
        writePacketId()
        encode(this)
        writeHex(TIMProtocol.tail)
    }

    override fun toString(): String = packetToString()

    override fun close() {
    }

    private fun BytePacketBuilder.writePacketId() {
        writeUShort(this@OutgoingPacket.id)
        writeUShort(sequenceId)
    }
}

@Suppress("unused")
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PacketVersion(val date: String, val timVersion: String)

private val Uninitialized = ByteReadPacket(IoBuffer.Empty, IoBuffer.EmptyPool)
