package net.mamoe.mirai.qqandroid.network.protocol.packet


import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.writeFully
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeIntLVPacket
import net.mamoe.mirai.utils.io.writeQQ

internal class OutgoingPacket constructor(
    name: String?,
    val commandName: String,
    val sequenceId: Int,
    val delegate: ByteReadPacket
) {
    val name: String by lazy {
        name ?: commandName
    }
}

internal val KEY_16_ZEROS = ByteArray(16)
internal val EMPTY_BYTE_ARRAY = ByteArray(0)

/**
 * com.tencent.qphone.base.util.CodecWarpper#encodeRequest(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, byte[], int, int, java.lang.String, byte, byte, byte, byte[], byte[], boolean)
 */
@Deprecated("危险", level = DeprecationLevel.ERROR)
@UseExperimental(MiraiInternalAPI::class)
internal inline fun OutgoingPacketFactory<*>.buildOutgoingPacket(
    client: QQAndroidClient,
    bodyType: Byte = 1, // 1: PB?
    name: String? = this.commandName,
    commandName: String = this.commandName,
    key: ByteArray = client.wLoginSigInfo.d2Key,
    body: BytePacketBuilder.(sequenceId: Int) -> Unit
): OutgoingPacket {
    val sequenceId: Int = client.nextSsoSequenceId()

    return OutgoingPacket(name, commandName, sequenceId, buildPacket {
        writeIntLVPacket(lengthOffset = { it + 4 }) {
            writeInt(0x0B)
            writeByte(bodyType)
            writeInt(sequenceId)
            writeByte(0)
            client.uin.toString().let {
                writeInt(it.length + 4)
                writeStringUtf8(it)
            }
            encryptAndWrite(key) {
                body(sequenceId)
            }
        }
    })
}

@UseExperimental(MiraiInternalAPI::class)
internal inline fun OutgoingPacketFactory<*>.buildOutgoingUniPacket(
    client: QQAndroidClient,
    bodyType: Byte = 1, // 1: PB?
    name: String? = this.commandName,
    commandName: String = this.commandName,
    key: ByteArray = client.wLoginSigInfo.d2Key,
    extraData: ByteReadPacket = BRP_STUB,
    sequenceId: Int = client.nextSsoSequenceId(),
    body: BytePacketBuilder.(sequenceId: Int) -> Unit
): OutgoingPacket {

    return OutgoingPacket(name, commandName, sequenceId, buildPacket {
        writeIntLVPacket(lengthOffset = { it + 4 }) {
            writeInt(0x0B)
            writeByte(bodyType)
            writeInt(sequenceId)
            writeByte(0)
            client.uin.toString().let {
                writeInt(it.length + 4)
                writeStringUtf8(it)
            }
            encryptAndWrite(key) {
                writeUniPacket(commandName, client.outgoingPacketUnknownValue, extraData) {
                    body(sequenceId)
                }
            }
        }
    })
}


@UseExperimental(MiraiInternalAPI::class)
internal inline fun IncomingPacketFactory<*>.buildResponseUniPacket(
    client: QQAndroidClient,
    bodyType: Byte = 1, // 1: PB?
    name: String? = this.responseCommandName,
    commandName: String = this.responseCommandName,
    key: ByteArray = client.wLoginSigInfo.d2Key,
    extraData: ByteReadPacket = BRP_STUB,
    sequenceId: Int = client.nextSsoSequenceId(),
    body: BytePacketBuilder.(sequenceId: Int) -> Unit
): OutgoingPacket {
    return OutgoingPacket(name, commandName, sequenceId, buildPacket {
        writeIntLVPacket(lengthOffset = { it + 4 }) {
            writeInt(0x0B)
            writeByte(bodyType)
            writeInt(sequenceId)
            writeByte(0)
            client.uin.toString().let {
                writeInt(it.length + 4)
                writeStringUtf8(it)
            }
            encryptAndWrite(key) {
                writeUniPacket(commandName, client.outgoingPacketUnknownValue, extraData) {
                    body(sequenceId)
                }
            }
        }
    })
}

@UseExperimental(MiraiInternalAPI::class)
internal inline fun BytePacketBuilder.writeUniPacket(
    commandName: String,
    unknownData: ByteArray,
    extraData: ByteReadPacket = BRP_STUB,
    body: BytePacketBuilder.() -> Unit
) {
    writeIntLVPacket(lengthOffset = { it + 4 }) {
        commandName.let {
            writeInt(it.length + 4)
            writeStringUtf8(it)
        }

        writeInt(4 + 4)
        writeFully(unknownData) //  02 B0 5B 8B

        if (extraData === BRP_STUB) {
            writeInt(0x04)
        } else {
            writeInt((extraData.remaining + 4).toInt())
            writePacket(extraData)
        }
    }

    // body
    writeIntLVPacket(lengthOffset = { it + 4 }, builder = body)
}


/**
 * com.tencent.qphone.base.util.CodecWarpper#encodeRequest(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, byte[], int, int, java.lang.String, byte, byte, byte, byte[], byte[], boolean)
 */
@UseExperimental(MiraiInternalAPI::class)
internal inline fun OutgoingPacketFactory<*>.buildLoginOutgoingPacket(
    client: QQAndroidClient,
    bodyType: Byte,
    extraData: ByteArray = EMPTY_BYTE_ARRAY,
    name: String? = null,
    commandName: String = this.commandName,
    key: ByteArray = KEY_16_ZEROS,
    body: BytePacketBuilder.(sequenceId: Int) -> Unit
): OutgoingPacket {
    val sequenceId: Int = client.nextSsoSequenceId()

    return OutgoingPacket(name, commandName, sequenceId, buildPacket {
        writeIntLVPacket(lengthOffset = { it + 4 }) {
            writeInt(0x00_00_00_0A)
            writeByte(bodyType)
            extraData.let {
                writeInt(it.size + 4)
                writeFully(it)
            }
            writeByte(0x00)

            client.uin.toString().let {
                writeInt(it.length + 4)
                writeStringUtf8(it)
            }

            encryptAndWrite(key) {
                body(sequenceId)
            }
        }
    })
}

private inline val BRP_STUB get() = ByteReadPacket.Empty

@UseExperimental(MiraiInternalAPI::class)
internal inline fun BytePacketBuilder.writeSsoPacket(
    client: QQAndroidClient,
    subAppId: Long,
    commandName: String,
    extraData: ByteReadPacket = BRP_STUB,
    unknownHex: String = "01 00 00 00 00 00 00 00 00 00 01 00",
    sequenceId: Int,
    body: BytePacketBuilder.() -> Unit
) {
    writeIntLVPacket(lengthOffset = { it + 4 }) {
        writeInt(sequenceId)
        writeInt(subAppId.toInt())
        writeInt(subAppId.toInt())
        writeHex(unknownHex)
        if (extraData === BRP_STUB || extraData.remaining == 0L) {
            // fast-path
            writeInt(0x04)
        } else {
            writeInt((extraData.remaining + 4).toInt())
            writePacket(extraData)
        }
        commandName.let {
            writeInt(it.length + 4)
            writeStringUtf8(it)
        }

        writeInt(4 + 4)
        writeFully(client.outgoingPacketUnknownValue) //  02 B0 5B 8B

        client.device.imei.let {
            writeInt(it.length + 4)
            writeStringUtf8(it)
        }

        writeInt(4)

        client.ksid.let {
            writeShort((it.size + 2).toShort())
            writeFully(it)
        }

        writeInt(4)
    }

    // body
    writeIntLVPacket(lengthOffset = { it + 4 }, builder = body)
}

@UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
internal fun BytePacketBuilder.writeOicqRequestPacket(
    client: QQAndroidClient,
    encryptMethod: EncryptMethod,
    commandId: Int,
    bodyBlock: BytePacketBuilder.() -> Unit
) {
    val body = encryptMethod.makeBody(client, bodyBlock)
    writeByte(0x02) // head
    writeShort((27 + 2 + body.remaining).toShort()) // orthodox algorithm
    writeShort(client.protocolVersion)
    writeShort(commandId.toShort())
    writeShort(1) // const??
    writeQQ(client.uin)
    writeByte(3) // originally const
    writeByte(encryptMethod.id.toByte())
    writeByte(0) // const8_always_0
    writeInt(2) // originally const
    writeInt(client.appClientVersion)
    writeInt(0) // constp_always_0

    writePacket(body)

    writeByte(0x03) // tail
}