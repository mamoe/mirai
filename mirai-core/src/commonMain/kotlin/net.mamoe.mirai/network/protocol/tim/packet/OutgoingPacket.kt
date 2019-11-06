@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.use
import kotlinx.io.core.writeUShort
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeQQ
import kotlin.jvm.JvmOverloads

/**
 * 待发送给服务器的数据包. 它代表着一个 [ByteReadPacket],
 */
class OutgoingPacket(
    name: String?,
    val packetId: PacketId,
    val sequenceId: UShort,
    internal val delegate: ByteReadPacket
) : Packet {
    private val name: String by lazy {
        name ?: packetId.toString()
    }

    constructor(id: PacketId, sequenceId: UShort, delegate: ByteReadPacket) : this(null, id, sequenceId, delegate)

    constructor(annotation: AnnotatedId, sequenceId: UShort, delegate: ByteReadPacket) :
            this(annotation.toString(), annotation.id, sequenceId, delegate)

    override fun toString(): String = packetToString(packetId.value, sequenceId, name)
}

/**
 * 登录完成建立 session 之后发出的包.
 * 均使用 sessionKey 加密
 */
abstract class SessionPacketFactory<out TPacket : Packet> : PacketFactory<TPacket, SessionKey>(SessionKey) {
    final override fun decrypt(input: ByteReadPacket, decrypter: SessionKey): ByteReadPacket = decrypter.decrypt(input)
}

/**
 * 构造一个待发送给服务器的数据包.
 *
 * 若不提供参数 [id], 则会通过注解 [AnnotatedId] 获取 id.
 */
@JvmOverloads
fun PacketFactory<*, *>.buildOutgoingPacket(
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
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


/**
 * 构造一个待发送给服务器的会话数据包.
 *
 * 若不提供参数 [id], 则会通过注解 [AnnotatedId] 获取 id.
 */
@JvmOverloads
fun PacketFactory<*, *>.buildSessionPacket(
    bot: UInt,
    sessionKey: SessionKey,
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    block: BytePacketBuilder.() -> Unit
): OutgoingPacket = buildOutgoingPacket(name, id, sequenceId, headerSizeHint) {
    writeQQ(bot)
    writeHex(TIMProtocol.version0x02)
    encryptAndWrite(sessionKey) {
        block()
    }
}