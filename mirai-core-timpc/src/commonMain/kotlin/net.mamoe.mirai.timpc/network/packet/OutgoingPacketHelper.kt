package net.mamoe.mirai.timpc.network.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.serialization.SerializationStrategy

import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmOverloads


/**
 * 构造一个待发送给服务器的数据包.
 */
@UseExperimental(ExperimentalContracts::class, MiraiInternalAPI::class, ExperimentalUnsignedTypes::class)
@JvmOverloads
inline fun PacketFactory<*, *>.buildOutgoingPacket(
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    block: BytePacketBuilder.() -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return buildOutgoingPacket0(name, id, sequenceId, headerSizeHint, TIMProtocol.head, TIMProtocol.ver, TIMProtocol.tail, block)
}


/**
 * 构造一个待发送给服务器的会话数据包.
 */
@UseExperimental(ExperimentalContracts::class, MiraiInternalAPI::class, ExperimentalUnsignedTypes::class)
@JvmOverloads
inline fun PacketFactory<*, *>.buildSessionPacket(
    bot: Long,
    sessionKey: SessionKey,
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    version: ByteArray = TIMProtocol.version0x02, // in packet body
    block: BytePacketBuilder.() -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return buildSessionPacket0(
        bot = bot,
        sessionKey = sessionKey,
        name = name,
        id = id,
        sequenceId = sequenceId,
        headerSizeHint = headerSizeHint,
        version = version,
        head = TIMProtocol.head,
        ver = TIMProtocol.ver,
        tail = TIMProtocol.tail,
        block = block
    )
}

/**
 * 构造一个待发送给服务器的会话数据包.
 */
@UseExperimental(ExperimentalContracts::class, MiraiInternalAPI::class, ExperimentalUnsignedTypes::class)
@JvmOverloads
fun <T> PacketFactory<*, *>.buildSessionProtoPacket(
    bot: Long,
    sessionKey: SessionKey,
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    version: ByteArray = TIMProtocol.version0x04,
    head: Any,
    serializer: SerializationStrategy<T>,
    protoObj: T
): OutgoingPacket = buildSessionProtoPacket0(
    bot = bot,
    sessionKey = sessionKey,
    name = name,
    id = id,
    sequenceId = sequenceId,
    headerSizeHint = headerSizeHint,
    version = version,
    head = head,
    serializer = serializer,
    protoObj = protoObj,
    packetHead = TIMProtocol.head,
    ver = TIMProtocol.ver,
    tail = TIMProtocol.tail
)