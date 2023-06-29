/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet


import io.ktor.utils.io.core.*
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.components.EcdhInitialPublicKeyUpdater
import net.mamoe.mirai.internal.network.components.encryptServiceOrNull
import net.mamoe.mirai.internal.network.protocol.data.proto.SSOReserveField
import net.mamoe.mirai.internal.network.protocol.packet.sso.TRpcRawPacket
import net.mamoe.mirai.internal.spi.EncryptService
import net.mamoe.mirai.internal.spi.EncryptServiceContext
import net.mamoe.mirai.internal.utils.io.encryptAndWrite
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.internal.utils.io.writeHex
import net.mamoe.mirai.internal.utils.io.writeIntLVPacket
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.Either.Companion.fold
import kotlin.random.Random

@Suppress("unused")
internal class OutgoingPacketWithRespType<R : Packet?> constructor(
    remark: String?,
    commandName: String,
    sequenceId: Int,
    delegate: ByteReadPacket
) : OutgoingPacket(remark, commandName, sequenceId, delegate)

internal open class OutgoingPacket constructor(
    remark: String?,
    val commandName: String,
    val sequenceId: Int,
    delegate: ByteReadPacket
) {
    val delegate = delegate.readBytes()
    val displayName: String = if (remark == null) commandName else "$commandName($remark)"
}

internal class IncomingPacket private constructor(
    val commandName: String,
    val sequenceId: Int,

    val result: Either<Throwable, Packet?> // exception will be the same as caught from PacketFactory.decode. So they can be ISE, NPE, etc.
) {
    companion object {
        operator fun invoke(commandName: String, sequenceId: Int, data: Packet?) =
            IncomingPacket(commandName, sequenceId, Either(data))

        operator fun invoke(commandName: String, sequenceId: Int, throwable: Throwable) =
            IncomingPacket(commandName, sequenceId, Either(throwable))


        @TestOnly
        operator fun invoke(commandName: String, data: Packet?) =
            IncomingPacket(commandName, Random.nextInt(), data)

        @TestOnly
        operator fun invoke(commandName: String, throwable: Throwable) =
            IncomingPacket(commandName, Random.nextInt(), throwable)
    }

    override fun toString(): String {
        return result.fold(
            onLeft = { "IncomingPacket(cmd=$commandName, seq=$sequenceId, FAILURE, e=$it)" },
            onRight = { "IncomingPacket(cmd=$commandName, seq=$sequenceId, SUCCESS, r=$it)" }
        )
    }
}

internal enum class PacketEncryptType(val value: Int) {
    NoEncrypt(0x00) { // 0x00
        override fun defaultKey(client: QQAndroidClient): ByteArray = NO_ENCRYPT
    },
    D2(0x01) { //0x01
        override fun defaultKey(client: QQAndroidClient): ByteArray {
            return client.wLoginSigInfo.d2Key
        }
    },
    Empty(0x02) { // 16 zeros,// 0x02
        override fun defaultKey(client: QQAndroidClient): ByteArray {
            return KEY_16_ZEROS
        }
    },
    Unknown(-1) {
        override fun defaultKey(client: QQAndroidClient): ByteArray {
            error("unreachable")
        }
    }
    ;

    inline val codec: Byte get() = ordinal.toByte()

    abstract fun defaultKey(client: QQAndroidClient): ByteArray

    companion object {
        internal fun of(value: Int) = enumValues<PacketEncryptType>().find { it.value == value } ?: Unknown
    }
}


@Suppress("DuplicatedCode")
internal fun <R : Packet?> buildRawUniPacket(
    client: QQAndroidClient,
    encryptMethod: PacketEncryptType = PacketEncryptType.D2,
    remark: String?,
    commandName: String,
    key: ByteArray = encryptMethod.defaultKey(client),
    extraData: ByteReadPacket = BRP_STUB,
    uin: String = client.uin.toString(),
    sequenceId: Int = client.nextSsoSequenceId(),
    body: BytePacketBuilder.(sequenceId: Int) -> Unit
): OutgoingPacketWithRespType<R> {

    return OutgoingPacketWithRespType(remark, commandName, sequenceId, buildPacket {
        writeIntLVPacket(lengthOffset = { it + 4 }) {
            writeInt(0x0B) // req type simple
            writeByte(encryptMethod.codec)
            writeInt(sequenceId)
            writeByte(0)
            uin.let {
                writeInt(it.length + 4)
                writeText(it)
            }
            val encryptWorker = client.bot.encryptServiceOrNull
            val bodyBytes = buildPacket { body(sequenceId) }.readBytes()
            val signDataPacket = if (encryptWorker != null) {

                val signResult = encryptWorker.qSecurityGetSign(
                    EncryptServiceContext(client.uin),
                    sequenceId,
                    commandName,
                    bodyBytes
                )
                if (signResult != null)
                    buildPacket {
                        writeProtoBuf(
                            SSOReserveField.ReserveFields.serializer(),
                            SSOReserveField.ReserveFields(
                                flag = 0,
                                qimei = client.qimei16?.toByteArray() ?: EMPTY_BYTE_ARRAY,
                                newconnFlag = 0,
                                uid = client.uin.toString(),
                                imsi = 0,
                                networkType = 1,
                                ipStackType = 1,
                                messageType = 0,
                                secInfo = SSOReserveField.SsoSecureInfo(
                                    secSig = signResult.sign,
                                    secDeviceToken = signResult.token,
                                    secExtra = signResult.extra
                                ),
                                ssoIpOrigin = 0,
                            )
                        )
                    } else BRP_STUB
            } else BRP_STUB

            if (signDataPacket != BRP_STUB && (extraData != BRP_STUB && extraData.remaining != 0L)) {
                throw IllegalStateException("$commandName cmd needs sign but has extraData!")
            }

            if (encryptMethod === PacketEncryptType.NoEncrypt) {
                writeUniPacket(
                    commandName,
                    client.outgoingPacketSessionId,
                    if (signDataPacket == BRP_STUB) {
                        extraData
                    } else {
                        signDataPacket
                    },
                    (client.qimei16?.encodeToByteArray() ?: EMPTY_BYTE_ARRAY)
                ) {
                    writeFully(bodyBytes)
                }
            } else {
                encryptAndWrite(key) {
                    writeUniPacket(
                        commandName,
                        client.outgoingPacketSessionId,
                        if (signDataPacket == BRP_STUB) {
                            extraData
                        } else {
                            signDataPacket
                        },
                        (client.qimei16?.encodeToByteArray() ?: EMPTY_BYTE_ARRAY)
                    ) {
                        writeFully(bodyBytes)
                    }
                }
            }
        }
    })
}

@Suppress("DuplicatedCode", "NOTHING_TO_INLINE")
internal inline fun <R : Packet?> OutgoingPacketFactory<R>.buildOutgoingUniPacket(
    client: QQAndroidClient,
    encryptMethod: PacketEncryptType = PacketEncryptType.D2,
    remark: String? = this.commandName,
    commandName: String = this.commandName,
    key: ByteArray = encryptMethod.defaultKey(client),
    extraData: ByteReadPacket = BRP_STUB,
    uin: String = client.uin.toString(),
    sequenceId: Int = client.nextSsoSequenceId(),
    noinline body: BytePacketBuilder.(sequenceId: Int) -> Unit
): OutgoingPacketWithRespType<R> =
    buildRawUniPacket(client, encryptMethod, remark, commandName, key, extraData, uin, sequenceId, body)

@Suppress("NOTHING_TO_INLINE")
internal inline fun <R : Packet?> IncomingPacketFactory<R>.buildResponseUniPacket(
    client: QQAndroidClient,
    encryptMethod: PacketEncryptType = PacketEncryptType.D2, // 1: PB?
    remark: String? = this.responseCommandName,
    commandName: String = this.responseCommandName,
    key: ByteArray = encryptMethod.defaultKey(client),
    extraData: ByteReadPacket = BRP_STUB,
    sequenceId: Int = client.nextSsoSequenceId(),
    noinline body: BytePacketBuilder.(sequenceId: Int) -> Unit = {}
): OutgoingPacketWithRespType<R> = buildRawUniPacket(
    client = client,
    encryptMethod = encryptMethod,
    remark = remark,
    commandName = commandName,
    key = key,
    extraData = extraData,
    sequenceId = sequenceId,
    body = body
)


private inline fun BytePacketBuilder.writeUniPacket(
    commandName: String,
    outgoingPacketSessionId: ByteArray,
    extraData: ByteReadPacket = BRP_STUB,
    qimei16: ByteArray = EMPTY_BYTE_ARRAY,
    crossinline body: BytePacketBuilder.() -> Unit
) {
    writeIntLVPacket(lengthOffset = { it + 4 }) {
        commandName.let {
            writeInt(it.length + 4)
            writeText(it)
        }

        writeInt(outgoingPacketSessionId.size + 4)
        writeFully(outgoingPacketSessionId) //  02 B0 5B 8B

        if (extraData === BRP_STUB) {
            writeInt(0x04)
        } else {
            writeInt((extraData.remaining + 4).toInt())
            writePacket(extraData)
        }

        writeInt(qimei16.size + 4)
        writeFully(qimei16)
    }

    // body
    writeIntLVPacket(lengthOffset = { it + 4 }, builder = body)
}

internal val NO_ENCRYPT: ByteArray = ByteArray(0)

/**
 * com.tencent.qphone.base.util.CodecWarpper#encodeRequest(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, byte[], int, int, java.lang.String, byte, byte, byte, byte[], byte[], boolean)
 */
internal fun <R : Packet?> OutgoingPacketFactory<R>.buildLoginOutgoingPacket(
    client: QQAndroidClient,
    encryptMethod: PacketEncryptType,
    uin: String = client.uin.toString(),
    extraData: ByteArray = if (encryptMethod == PacketEncryptType.D2) client.wLoginSigInfo.d2.data else EMPTY_BYTE_ARRAY,
    remark: String? = null,
    commandName: String = this.commandName,
    key: ByteArray = encryptMethod.defaultKey(client),
    body: BytePacketBuilder.(sequenceId: Int) -> Unit
): OutgoingPacketWithRespType<R> {
    val sequenceId: Int = client.nextSsoSequenceId()

    return OutgoingPacketWithRespType(remark, commandName, sequenceId, buildPacket {
        writeIntLVPacket(lengthOffset = { it + 4 }) {
            writeInt(0x00_00_00_0A) // packet login
            writeByte(encryptMethod.codec) // encrypt type

            extraData.let { // actually d2 key if encryptMethod = d2
                writeInt(it.size + 4)
                writeFully(it)
            }

            writeByte(0x00)

            uin.let {
                writeInt(it.length + 4)
                writeText(it)
            }

            if (encryptMethod == PacketEncryptType.NoEncrypt) {
                body(sequenceId)
            } else {
                encryptAndWrite(key) { body(sequenceId) }
            }
        }
    })
}

private inline val BRP_STUB get() = ByteReadPacket.Empty

internal fun createChannelProxy(bot: QQAndroidBot): EncryptService.ChannelProxy {
    return object : EncryptService.ChannelProxy {
        override suspend fun sendMessage(
            remark: String,
            commandName: String,
            uin: Long,
            data: ByteArray
        ): EncryptService.ChannelResult? {
            if (commandName.startsWith(TRpcRawPacket.COMMAND_PREFIX)) {
                val client = bot.client
                val packet = client.bot.network.sendAndExpect(
                    TRpcRawPacket.buildLoginOutgoingPacket(
                        client = client,
                        encryptMethod = PacketEncryptType.Empty,
                        uin = uin.toString(),
                        remark = remark,
                        commandName = commandName,
                    ) {
                        writeSsoPacket(
                            client,
                            client.subAppId,
                            sequenceId = it,
                            commandName = commandName,
                            body = { writeFully(data) }
                        )
                    }
                )
                return EncryptService.ChannelResult(commandName, packet.data)
            }
            return null
        }
    }
}

internal fun BytePacketBuilder.writeSsoPacket(
    client: QQAndroidClient,
    subAppId: Long,
    commandName: String,
    extraData: ByteReadPacket = BRP_STUB,
    unknownHex: String = "01 00 00 00 00 00 00 00 00 00 01 00",
    sequenceId: Int,
    body: BytePacketBuilder.() -> Unit
) {

    /* send
     * 00 00 00 78
     * 00 00 94 90
     * 20 02 ED BD
     * 20 02 ED BD
     * 01 00 00 00 00 00 00 00 00 00 01 00
     * 00 00 00 04
     * 00 00 00 13 48 65 61 72 74 62 65 61 74 2E 41 6C 69 76 65
     * 00 00 00 08 59 E7 DF 4F
     * 00 00 00 13 38 36 35 31 36 36 30 32 36 34 34 36 39 32 35
     * 00 00 00 04
     * 00 22 7C 34 36 30 30 30 31 39 31 39 38 37 36 30 32 36 7C 41 38 2E 32 2E 30 2E 32 37 66 36 65 61 39 36
     * 00 00 00 04
     *
     * 00 00 00 04
     */
    val encryptWorker = client.bot.encryptServiceOrNull
    val bodyBytes = buildPacket(body).readBytes()
    val reserveField = if (encryptWorker != null) {

        val signResult = encryptWorker.qSecurityGetSign(
            EncryptServiceContext(client.uin),
            sequenceId,
            commandName,
            bodyBytes
        )
        if (signResult != null)
            ProtoBuf.encodeToByteArray(
                SSOReserveField.ReserveFields(
                    flag = 0,
                    qimei = client.qimei16?.toByteArray() ?: EMPTY_BYTE_ARRAY,
                    newconnFlag = 0,
                    uid = client.uin.toString(),
                    imsi = 0,
                    networkType = 1,
                    ipStackType = 1,
                    messageType = 0,
                    secInfo = SSOReserveField.SsoSecureInfo(
                        secSig = signResult.sign,
                        secDeviceToken = signResult.token,
                        secExtra = signResult.extra
                    ),
                    ssoIpOrigin = 0,
                )
            ) else EMPTY_BYTE_ARRAY
    } else EMPTY_BYTE_ARRAY

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

        writeInt(commandName.length + 4)
        writeText(commandName)

        writeInt(client.outgoingPacketSessionId.size + 4)
        writeFully(client.outgoingPacketSessionId) //  02 B0 5B 8B

        writeInt(client.device.imei.length + 4)
        writeText(client.device.imei)
        writeInt(0x4)

        writeShort((client.ksid.size + 2).toShort())
        writeFully(client.ksid)

        writeInt(reserveField.size + 4)
        writeFully(reserveField)


        val qimei16Bytes = client.qimei16?.toByteArray() ?: EMPTY_BYTE_ARRAY
        writeInt(qimei16Bytes.size + 4)
        writeFully(qimei16Bytes)
    }

    // body
    writeIntLVPacket(lengthOffset = { it + 4 }, builder = { writeFully(bodyBytes) })
}

internal fun BytePacketBuilder.writeOicqRequestPacket(
    client: QQAndroidClient,
    uin: Long = client.uin,
    encryptMethod: EncryptMethod = EncryptMethodEcdh(client.bot.components[EcdhInitialPublicKeyUpdater].getQQEcdh()),
    commandId: Int,
    bodyBlock: BytePacketBuilder.() -> Unit
) {
    val body = encryptMethod.makeBody(client, bodyBlock)
    writeByte(0x02) // head
    writeShort((27 + 2 + body.remaining).toShort()) // orthodox algorithm
    writeShort(8001)
    writeShort(commandId.toShort())
    writeShort(1) // const??
    writeInt(uin.toInt())
    writeByte(3) // originally const
    writeByte(encryptMethod.id.toByte())
    writeByte(0) // const8_always_0
    writeInt(2) // originally const
    writeInt(client.appClientVersion)
    writeInt(0) // constp_always_0

    writePacket(body)

    writeByte(0x03) // tail
}