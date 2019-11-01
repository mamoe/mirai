@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.atomicfu.atomic
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.use
import kotlinx.io.core.writeUShort
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.io.writeHex
import kotlin.jvm.JvmOverloads

/**
 * 待发送给服务器的数据包. 它代表着一个 [ByteReadPacket],
 */
class OutgoingPacket(
    name: String?,
    override val packetId: PacketId,
    override val sequenceId: UShort,
    internal val delegate: ByteReadPacket
) : Packet {
    private val name: String by lazy {
        name ?: packetId.toString()
    }

    constructor(id: PacketId, sequenceId: UShort, delegate: ByteReadPacket) : this(null, id, sequenceId, delegate)

    constructor(annotation: AnnotatedId, sequenceId: UShort, delegate: ByteReadPacket) :
            this(annotation.toString(), annotation.id, sequenceId, delegate)

    override fun toString(): String = packetToString(name)
}

/**
 * 发给服务器的数据包的构建器.
 * 应由一个 `object` 实现, 且实现 `operator fun invoke`
 */
interface OutgoingPacketBuilder {
    /**
     * 2 Ubyte.
     * 默认为读取注解 [AnnotatedId]
     */
    val annotatedId: AnnotatedId
        get() = (this::class.annotations.firstOrNull { it is AnnotatedId } as? AnnotatedId)
            ?: error("Annotation AnnotatedId not found")

    companion object {
        private val sequenceIdInternal = atomic(1)

        @PublishedApi
        internal fun atomicNextSequenceId(): UShort = sequenceIdInternal.getAndIncrement().toUShort()
    }
}

/**
 * 构造一个待发送给服务器的数据包.
 * 若不提供参数 [id], 则会通过注解 [AnnotatedId] 获取 id.
 */
@JvmOverloads
fun OutgoingPacketBuilder.buildOutgoingPacket(
    name: String? = null,
    id: PacketId = this.annotatedId.id,
    sequenceId: UShort = OutgoingPacketBuilder.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    block: BytePacketBuilder.() -> Unit
): OutgoingPacket {
    BytePacketBuilder(headerSizeHint).use {
        with(it) {
            writeHex(TIMProtocol.head)
            writeHex(TIMProtocol.ver)
            writeUShort(id.value)
            writeUShort(sequenceId)
            block(this)
            writeHex(TIMProtocol.tail)
        }
        return OutgoingPacket(name, id, sequenceId, it.build())
    }
}