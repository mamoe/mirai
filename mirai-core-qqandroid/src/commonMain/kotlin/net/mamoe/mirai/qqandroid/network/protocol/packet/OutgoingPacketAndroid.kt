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

/**
 * Encryption method to be used for packet body.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
inline class EncryptMethod(val value: UByte) {
    companion object {
        val BySessionToken = EncryptMethod(69u)
        val ByECDH7 = EncryptMethod(7u)
        // 登录都使用 135
        val ByECDH135 = EncryptMethod(135u)
    }
}

/**
 * Builds a outgoing packet.
 * [OutgoingPacket] is the **outermost** packet structure.
 * This packet will be sent to the server.
 *
 *
 * **Packet Structure**
 * byte     2 // head
 * short    27 + 2 + body.size
 * ushort   client.protocolVersion // const 8001
 * ushort   sequenceId
 * uint     client.account.id
 * byte     3 // const
 * ubyte    encryptMethod.value // [EncryptMethod]
 * byte     0 // const
 * int      2 // const
 * int      client.appClientVersion
 * int      0 // const
 * bodyBlock()
 * byte     3 // tail
 *
 * @param name optional name to be displayed in logs
 *
 */
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
 * Encrypt the [body] by [ECDH.shareKey], then write encryption arguments stuff.
 * This is **the second outermost** packet structure
 *
 * **Packet Structure**
 * byte     1
 * byte     1
 * byte[]   [ECDH.privateKey]
 * short    258
 * short    [ECDH.publicKey].size
 * byte[]   [ECDH.publicKey]
 * byte[]   encrypted `body()` by [ECDH.shareKey]
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