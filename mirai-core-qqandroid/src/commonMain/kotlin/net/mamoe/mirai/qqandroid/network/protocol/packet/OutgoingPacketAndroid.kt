package net.mamoe.mirai.qqandroid.network.protocol.packet


import kotlinx.io.core.*
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.PacketId
import net.mamoe.mirai.qqandroid.utils.ECDH
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeQQ
import net.mamoe.mirai.utils.io.writeShortLVByteArray

/**
 * 待发送给服务器的数据包. 它代表着一个 [ByteReadPacket],
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
class OutgoingPacket constructor(
    name: String?,
    val packetId: PacketId,
    val sequenceId: UShort,
    val delegate: ByteReadPacket
) : Packet {
    val name: String by lazy {
        name ?: packetId.toString()
    }
}

/*
private open fun writeHead(
    always_8001: Int,
    command: Int,
    uin: Long,
    encryptType: Int,
    const8_always_0: Int,
    appClientVersion: Int,
    constp_always_0: Int,
    bodyLength: Int
) {
    val j: Int = this.j + 1
    this.j = j
    this.pos = 0
    util.int8_to_buf(this.buffer, this.pos, 2)
    ++this.pos
    util.int16_to_buf(this.buffer, this.pos, this.d + 2 + bodyLength)
    this.pos += 2
    util.int16_to_buf(this.buffer, this.pos, always_8001)
    this.pos += 2
    util.int16_to_buf(this.buffer, this.pos, command)
    this.pos += 2
    util.int16_to_buf(this.buffer, this.pos, j)
    this.pos += 2
    util.int32_to_buf(this.buffer, this.pos, uin.toInt())
    this.pos += 4
    util.int8_to_buf(this.buffer, this.pos, 3)
    ++this.pos
    util.int8_to_buf(this.buffer, this.pos, encryptType)
    ++this.pos
    util.int8_to_buf(this.buffer, this.pos, const8_always_0)
    ++this.pos
    util.int32_to_buf(this.buffer, this.pos, 2)
    this.pos += 4
    util.int32_to_buf(this.buffer, this.pos, appClientVersion)
    this.pos += 4
    util.int32_to_buf(this.buffer, this.pos, constp_always_0)
    this.pos += 4
}
*/

@UseExperimental(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeHead(
    always_8001: Short = 8001,
    command: Short,
    uin: Long,
    encryptType: Int, //
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    const8_always_0: Byte = 0,
    appClientVersion: Int,
    constp_always_0: Int = 0,
    bodyLength: Int
) {
    writeByte(2)
    writeShort((27 + 2 + bodyLength).toShort())
    writeShort(always_8001)
    writeShort(command)
    writeUShort(sequenceId)
    writeInt(uin.toInt())
    writeByte(3)
    writeByte(encryptType.toByte())
    writeByte(const8_always_0)
    writeInt(2)
    writeInt(appClientVersion)
    writeInt(constp_always_0)
}

@UseExperimental(ExperimentalUnsignedTypes::class)
inline class EncryptMethod(val value: UByte) {
    companion object {
        val BySessionToken = EncryptMethod(69u)
        val ByECDH7 = EncryptMethod(7u)
        // 登录都使用 135
        val ByECDH135 = EncryptMethod(135u)
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
internal inline fun PacketFactory<*, *>.buildOutgoingPacket(
    client: QQAndroidClient,
    encryptMethod: EncryptMethod,
    name: String? = null,
    id: PacketId = this.id,
    sequenceId: UShort = PacketFactory.atomicNextSequenceId(),
    bodyBlock: BytePacketBuilder.() -> Unit
): OutgoingPacket {
    val body = buildPacket { bodyBlock() }
    return OutgoingPacket(name, id, sequenceId, buildPacket {
        // Head
        writeByte(0x02) // head
        writeShort((27 + 2 + body.remaining).toShort()) // orthodox algorithm
        writeShort(client.protocolVersion)
        writeShort(id.commandId.toShort())
        writeShort(sequenceId.toShort())
        writeQQ(client.account.id)
        writeByte(3) // originally const
        writeUByte(encryptMethod.value)
        writeByte(0) // const8_always_0
        writeInt(2) // originally const
        writeInt(client.appClientVersion)
        writeInt(0) // constp_always_0

        // Body
        writePacket(body)

        // Tail
        writeByte(0x03) // tail
    })
}

/**
 * buildPacket{
 *     byte 1
 *     byte 1
 *     fully privateKey
 *     short 258
 *     short publicKey.length
 *     fully publicKey
 *     encryptAndWrite(shareKey, body)
 * }
 */
inline fun BytePacketBuilder.writeECDHEncryptedPacket(
    ecdh: ECDH,
    body: BytePacketBuilder.() -> Unit
) = ecdh.run {
    writeByte(1) // const
    writeByte(1) // const
    writeFully(privateKey)
    writeShort(258) // const
    writeShortLVByteArray(publicKey)
    encryptAndWrite(shareKey, body)
}


/**
 * buildPacket{
 *     byte 1
 *     if loginState == 2:
 *       byte 3
 *     else:
 *       byte 2
 *     fully key
 *     short 258
 *     short 0
 *     fully encrypted
 * }
 */
inline fun BytePacketBuilder.writeTEAEncryptedPacket(
    loginState: Int,
    key: ByteArray,
    body: BytePacketBuilder.() -> Unit
) {
    require(loginState == 2 || loginState == 3)
    writeByte(1) // const
    writeByte(if (loginState == 2) 3 else 2) // const
    writeFully(key)
    writeShort(258) // const
    writeShort(0) // const, length of publicKey
    encryptAndWrite(key, body)
}