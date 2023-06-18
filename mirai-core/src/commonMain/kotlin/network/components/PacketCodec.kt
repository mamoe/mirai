/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.components.PacketCodec.Companion.PacketLogger
import net.mamoe.mirai.internal.network.components.PacketCodecException.Kind.*
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.protocol.data.proto.SSOReserveField
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.network.protocol.packet.sso.TRpcRawPacket
import net.mamoe.mirai.internal.utils.crypto.Ecdh
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.utils.*


/**
 * Packet decoders.
 *
 * - Transforms [ByteReadPacket] to [RawIncomingPacket]
 */
internal interface PacketCodec {
    /**
     * It's caller's responsibility to close [input].
     *
     * @throws PacketCodecException normal, known errors
     * @throws Exception unexpected errors
     * @param input received from sockets.
     * @return decoded
     */
    @Throws(PacketCodecException::class)
    fun decodeRaw(client: SsoSession, input: ByteReadPacket): RawIncomingPacket

    /**
     * Process [RawIncomingPacket] using [IncomingPacketFactory.decode].
     *
     * This function throws **no** exception and wrap them into [IncomingPacket].
     */
    suspend fun processBody(bot: QQAndroidBot, input: RawIncomingPacket): IncomingPacket?

    companion object : ComponentKey<PacketCodec> {
        val PACKET_DEBUG = systemProp("mirai.network.packet.logger", false)

        internal val PacketLogger: MiraiLoggerWithSwitch by lazy {
            MiraiLogger.Factory.create(PacketCodec::class, "Packet").withSwitch(PACKET_DEBUG)
        }
    }
}

/**
 * Wraps an exception thrown by [PacketCodec.decodeRaw], which is not a [PacketCodecException] (meaning unexpected).
 */
internal data class ExceptionInPacketCodecException(
    override val cause: Throwable,
) : IllegalStateException("Exception in PacketCodec.", cause)

/**
 * Thrown by [PacketCodec.decodeRaw], representing an excepted error.
 */
internal class PacketCodecException(
    val targetException: Throwable,
    val kind: Kind,
) : NetworkException(recoverable = true, cause = targetException) {
    constructor(message: String, kind: Kind) : this(IllegalStateException(message), kind)

    enum class Kind {
        /**
         * 会触发重连
         */
        SESSION_EXPIRED,

        /**
         * 只记录日志
         */
        PROTOCOL_UPDATED,

        /**
         * 只记录日志
         */
        OTHER,
    }

//     not available in native
//    override fun getStackTrace(): Array<StackTraceElement> {
//        return targetException.stackTrace
//    }
}

internal enum class IncomingPacketType(val value: Int) {
    Login(0x0A),
    Simple(0x0B),
    Unknown(-1)
    ;

    companion object {
        internal fun of(value: Int) = enumValues<IncomingPacketType>().find { it.value == value } ?: Unknown
    }
}

internal class PacketCodecImpl : PacketCodec {

    override fun decodeRaw(
        client: SsoSession,
        input: ByteReadPacket
    ): RawIncomingPacket = input.run {
        // packet type
        val packetType = IncomingPacketType.of(readInt())

        PacketLogger.verbose { "开始处理一个包" }

        val encryptMethod = PacketEncryptType.of(readByte().toInt())
        val flag3 = readByte().toInt()
        val flag3Exception = if (flag3 != 0) {
            PacketCodecException(
                "Illegal flag3. Expected 0, whereas got $flag3. packet type=$packetType, encrypt method=$encryptMethod. ",
                kind = PROTOCOL_UPDATED
            )
        } else null

        readString(readInt() - 4)// uinAccount

        ByteArrayPool.useInstance(this.remaining.toInt()) { buffer ->
            val size = this.readAvailable(buffer)

            val raw = try {
                when (encryptMethod) {
                    PacketEncryptType.Empty -> TEA.decrypt(buffer, DECRYPTER_16_ZERO, size)
                    PacketEncryptType.D2 -> {
                        TEA.decrypt(buffer, kotlin.runCatching { client.wLoginSigInfo.d2Key }.getOrElse {
                            throw PacketCodecException(
                                "Received packet needed d2Key to decrypt but d2Key doesn't existed, ignoring. Please report to https://github.com/mamoe/mirai/issues/new/choose if you see anything abnormal",
                                PROTOCOL_UPDATED
                            )
                        }, size)
                    }

                    PacketEncryptType.NoEncrypt -> buffer
                    else -> throw PacketCodecException("Unknown encrypt type=$encryptMethod", PROTOCOL_UPDATED)
                }.let { decryptedData ->
                    PacketLogger.verbose { "Parsing: type=${packetType}: len=${decryptedData.size}, value=${decryptedData.toUHexString()}" }

                    when (packetType) {
                        IncomingPacketType.Login -> parseSsoFrame(client, decryptedData)
                        IncomingPacketType.Simple -> parseSsoFrame(
                            client,
                            decryptedData
                        ) // 这里可能是 uni?? 但测试时候发现结构跟 sso 一样.
                        else -> throw PacketCodecException(
                            "unknown packet type: ${packetType.value.toUHexString()}",
                            PROTOCOL_UPDATED
                        )
                    }.also { pkg ->
                        PacketLogger.debug {
                            "result: ${pkg.commandName}, seq=${pkg.sequenceId}, ${
                                pkg.body.copy()
                                    .useBytes { data: ByteArray, length: Int -> data.toUHexString(length = length) }
                            }"
                        }
                    }
                }
            } catch (e: Exception) {
                throw e.also {
                    if (flag3Exception != null) {
                        it.addSuppressed(flag3Exception)
                    }
                }
            }

            if (flag3 != 0 && flag3Exception != null) {
                if (raw.commandName == WtLogin.TransEmp.commandName) {
                    PacketLogger.warning(
                        "unknown flag3: $flag3 in packet ${WtLogin.TransEmp.commandName}, " +
                                "which may means protocol is updated.",
                        flag3Exception
                    )
                } else if (raw.commandName.startsWith(TRpcRawPacket.COMMAND_PREFIX)) {
                    PacketLogger.verbose { "received a trpc native packet: ${raw.commandName}" }
                } else {
                    throw flag3Exception
                }
            }

            when (encryptMethod) {
                PacketEncryptType.NoEncrypt,
                PacketEncryptType.D2 -> RawIncomingPacket(
                    raw.commandName,
                    raw.sequenceId,
                    raw.body.readBytes()
                )

                PacketEncryptType.Empty -> {
                    RawIncomingPacket(
                        raw.commandName,
                        raw.sequenceId,
                        raw.body.withUse {
                            if (raw.commandName.startsWith(TRpcRawPacket.COMMAND_PREFIX)) {
                                readBytes()
                            } else {
                                try {
                                    parseOicqResponse(client, raw.commandName)
                                } catch (e: Throwable) {
                                    throw PacketCodecException(e, PacketCodecException.Kind.OTHER)
                                }
                            }
                        }
                    )
                }

                else -> error("unreachable")
            }
        }
    }

    internal class DecodeResult constructor(
        val commandName: String,
        val sequenceId: Int,
        /**
         * Can be passed to [PacketFactory]
         */
        val body: ByteReadPacket,
    )

    private fun parseSsoFrame(client: SsoSession, bytes: ByteArray): DecodeResult =
        bytes.toReadPacket().let { input ->
            val commandName: String
            val ssoSequenceId: Int
            val dataCompressed: Int
            input.readPacketExact(input.readInt() - 4).withUse {
                ssoSequenceId = readInt()
                PacketLogger.verbose { "sequenceId = $ssoSequenceId" }

                val returnCode = readInt()
                if (returnCode != 0) {
                    if (returnCode <= -10000) {
                        // #470: -10008, 例如在手机QQ强制下线机器人
                        // #1957: -10106, 未知原因, 但会导致收不到消息

                        throw PacketCodecException(
                            "Received packet returnCode = $returnCode, which may mean session expired.",
                            SESSION_EXPIRED
                        )

                        // 备注: 之后该异常将会导致 NetworkHandler close, 然后由 selector 触发重连.
                        // 重连时会在 net.mamoe.mirai.internal.network.components.SsoProcessorImpl.login 进行 FastLogin.
                        // 不确定在这种情况下执行 FastLogin 是否正确. 若有问题, 考虑强制执行 SlowLogin (by invalidating session).
                    } else {
                        throw PacketCodecException(
                            "Received unknown packet returnCode = $returnCode, ignoring. Please report to https://github.com/mamoe/mirai/issues/new/choose if you see anything abnormal",
                            OTHER
                        )

                        // 备注: OTHER 不会触发重连, 只会记录日志.
                    }
                }

                if (PacketLogger.isEnabled) {
                    val extraData = readBytes(readInt() - 4)
                    if (extraData.isNotEmpty()) {
                        PacketLogger.verbose {
                            "(sso/inner)extraData = ${extraData.toUHexString()}, result= ${
                                kotlin.runCatching {
                                    readProtoBuf(
                                        SSOReserveField.ReserveFields.serializer()
                                    ).structureToString()
                                }.getOrElse { e -> "error: " + e.message }
                            }"
                        }
                    }
                } else {
                    discardExact(readInt() - 4)
                }

                commandName = readString(readInt() - 4)
                client.outgoingPacketSessionId = readBytes(readInt() - 4)

                dataCompressed = readInt()
            }

            val packet = when (dataCompressed) {
                0 -> {
                    val size = input.readInt().toLong() and 0xffffffff
                    if (size == input.remaining || size == input.remaining + 4) {
                        input
                    } else {
                        buildPacket {
                            writeInt(size.toInt())
                            writePacket(input)
                        }
                    }
                }

                1 -> {
                    input.discardExact(4)
                    input.inflateAllAvailable().let { bytes ->
                        val size = bytes.toInt()
                        if (size == bytes.size || size == bytes.size + 4) {
                            bytes.toReadPacket(offset = 4)
                        } else {
                            bytes.toReadPacket()
                        }
                    }
                }

                8 -> input
                else -> throw PacketCodecException("Unknown dataCompressed flag: $dataCompressed", PROTOCOL_UPDATED)
            }

            // body

            return DecodeResult(commandName, ssoSequenceId, packet)
        }

    private fun ByteReadPacket.parseOicqResponse(
        client: SsoSession,
        commandName: String
    ): ByteArray {
        val qqEcdh = (client as QQAndroidClient).bot.components[EcdhInitialPublicKeyUpdater].getQQEcdh()
        fun decrypt(encryptionMethod: Int): ByteArray {
            return when (encryptionMethod) {
                4 -> {
                    val size = (this.remaining - 1).toInt()
                    val data =
                        TEA.decrypt(
                            this.readBytes(),
                            qqEcdh.initialQQShareKey,
                            length = size
                        )

                    val peerShareKey =
                        qqEcdh.calculateQQShareKey(Ecdh.Instance.importPublicKey(readUShortLVByteArray()))
                    TEA.decrypt(data, peerShareKey)
                }

                3 -> {
                    val size = (this.remaining - 1).toInt()
                    // session
                    TEA.decrypt(
                        this.readBytes(),
                        client.wLoginSigInfo.wtSessionTicketKey,
                        length = size
                    )
                }

                0 -> {
                    if (client.loginState == 0) {
                        val size = (this.remaining - 1).toInt()
                        val byteArrayBuffer = this.readBytes(size)

                        runCatching {
                            TEA.decrypt(byteArrayBuffer, qqEcdh.initialQQShareKey, length = size)
                        }.getOrElse {
                            TEA.decrypt(byteArrayBuffer, client.randomKey, length = size)
                        }
                    } else {
                        val size = (this.remaining - 1).toInt()
                        TEA.decrypt(this.readBytes(), client.randomKey, length = size)
                    }
                }

                else -> error("Illegal encryption method. expected 0 or 4, got $encryptionMethod")
            }
        }

        val packetType = readByte().toInt()
        if (packetType != 2) {
            val fullPacketDump = copy().readBytes().toUHexString()
            var decryptedData: String? = null
            if (remaining > 15) {
                discardExact(12)
                val encryptionMethod = this.readShort().toUShort().toInt()
                discardExact(1)
                decryptedData = kotlin.runCatching {
                    decrypt(encryptionMethod).toUHexString()
                }.getOrNull()
            }
            throw PacketCodecException(
                "Received unknown oicq packet type = $packetType, command name = $commandName, ignoring..." +
                        "\nPlease report this message to https://github.com/mamoe/mirai/issues/new/choose, \n" +
                        "Full packet dump: $fullPacketDump\n" +
                        "Decrypted data (contains your encrypted password, please change your password after reporting issue): $decryptedData",
                PROTOCOL_UPDATED
            )
        }

        this.discardExact(2)
        this.discardExact(2)
        this.readShort().toUShort()
        this.readShort()
        this.readInt().toUInt().toLong()
        val encryptionMethod = this.readShort().toUShort().toInt()

        this.discardExact(1)
        return decrypt(encryptionMethod)
    }

    /**
     * Process [RawIncomingPacket] using [IncomingPacketFactory.decode].
     *
     * This function wraps exceptions into [IncomingPacket]
     */
    override suspend fun processBody(bot: QQAndroidBot, input: RawIncomingPacket): IncomingPacket? {
        val factory = KnownPacketFactories.findPacketFactory(input.commandName)
            ?: TRpcRawPacket.takeIf { input.commandName.startsWith(TRpcRawPacket.COMMAND_PREFIX) }
            ?: return null

        return kotlin.runCatching {
            input.body.toReadPacket().use { body ->
                when (factory) {
                    // specially
                    is TRpcRawPacket -> TRpcRawPacket.decode(input, body)

                    is OutgoingPacketFactory -> factory.decode(bot, body)
                    is IncomingPacketFactory -> factory.decode(bot, body, input.sequenceId)
                }
            }
        }.fold(
            onSuccess = { packet ->
                IncomingPacket(input.commandName, input.sequenceId, packet)
            },
            onFailure = { exception: Throwable ->
                IncomingPacket(input.commandName, input.sequenceId, exception)
            }
        )
    }
}

/**
 * Represents a packet that has just been decrypted. Subsequent operation is normally passing it to a responsible [PacketFactory] according to [commandName] from [KnownPacketFactories].
 */
internal class RawIncomingPacket constructor(
    val commandName: String,
    val sequenceId: Int,
    /**
     * Can be passed to [PacketFactory]
     */
    val body: ByteArray,
)
