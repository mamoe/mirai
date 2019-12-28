@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.timpc.network.packet

import kotlinx.io.core.*
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler

import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.cryptor.encryptAndWrite
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.writeQQ
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmOverloads

/**
 * 待发送给服务器的数据包. 它代表着一个 [ByteReadPacket],
 */
class OutgoingPacket(
    name: String?,
    val packetId: PacketId,
    val sequenceId: UShort,
    val delegate: ByteReadPacket
) : Packet {
    val name: String by lazy {
        name ?: packetId.toString()
    }
}

/**
 * 登录完成建立 session 之后发出的包.
 * 均使用 sessionKey 加密
 *
 * @param TPacket invariant
 */
abstract class SessionPacketFactory<TPacket : Packet> : PacketFactory<TPacket, SessionKey>(
    SessionKey
) {
    /**
     * 在 [BotNetworkHandler] 下处理这个包. 广播事件等.
     */
    open suspend fun BotNetworkHandler.handlePacket(packet: TPacket) {}
}

/**
 * 构造一个待发送给服务器的数据包.
 */
@UseExperimental(ExperimentalContracts::class, MiraiInternalAPI::class)
@JvmOverloads
inline fun PacketFactory<*, *>.buildOutgoingPacket0(
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    head: ByteArray,
    ver: ByteArray,
    tail: ByteArray,
    block: BytePacketBuilder.() -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return OutgoingPacket(name, id, sequenceId, buildPacket(headerSizeHint) {
        writeFully(head)
        writeFully(ver)
        writeUShort(id.value.toUShort())
        writeUShort(sequenceId)
        block(this)
        writeFully(tail)
    })
}


/**
 * 构造一个待发送给服务器的会话数据包.
 */
@UseExperimental(ExperimentalContracts::class, MiraiInternalAPI::class)
@JvmOverloads
inline fun PacketFactory<*, *>.buildSessionPacket0(
    bot: Long,
    sessionKey: SessionKey,
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    version: ByteArray, // in packet body
    head: ByteArray,
    ver: ByteArray, // in packet head
    tail: ByteArray,
    block: BytePacketBuilder.() -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return buildOutgoingPacket0(
        name = name,
        id = id,
        sequenceId = sequenceId,
        headerSizeHint = headerSizeHint,
        head = head,
        ver = ver,
        tail = tail
    ) {
        writeQQ(bot)
        writeFully(version)
        encryptAndWrite(sessionKey) {
            block()
        }
    }
}

/**
 * 构造一个待发送给服务器的会话数据包.
 */
@UseExperimental(ExperimentalContracts::class, MiraiInternalAPI::class)
@JvmOverloads
fun <T> PacketFactory<*, *>.buildSessionProtoPacket0(
    bot: Long,
    sessionKey: SessionKey,
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    headerSizeHint: Int = 0,
    version: ByteArray,
    head: Any,
    serializer: SerializationStrategy<T>,
    protoObj: T,
    packetHead: ByteArray,
    ver: ByteArray, // in packet head
    tail: ByteArray
): OutgoingPacket {
    require(head is ByteArray || head is UByteArray || head is String) { "Illegal head type" }
    return buildOutgoingPacket0(name, id, sequenceId, headerSizeHint, head = packetHead, ver = ver, tail = tail) {
        writeQQ(bot)
        writeFully(version)
        encryptAndWrite(sessionKey) {
            when (head) {
                is ByteArray -> {
                    val proto = ProtoBuf.dump(serializer, protoObj)
                    writeInt(head.size)
                    writeInt(proto.size)
                    writeFully(head)
                    writeFully(proto)
                }
                is UByteArray -> {
                    val proto = ProtoBuf.dump(serializer, protoObj)
                    writeInt(head.size)
                    writeInt(proto.size)
                    writeFully(head)
                    writeFully(proto)
                }
                is String -> buildSessionProtoPacket0(
                    bot = bot,
                    sessionKey = sessionKey,
                    name = name,
                    id = id,
                    sequenceId = sequenceId,
                    headerSizeHint = headerSizeHint,
                    version = version,
                    head = head.hexToBytes(),
                    serializer = serializer,
                    protoObj = protoObj,
                    packetHead = packetHead,
                    ver = ver,
                    tail = tail
                )
            }
        }
    }
}