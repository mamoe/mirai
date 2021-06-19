/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.components.PacketCodec.Companion.PacketLogger
import net.mamoe.mirai.internal.network.context.SsoSession
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.crypto.adjustToPublicKey
import net.mamoe.mirai.utils.*
import kotlin.io.use


/**
 * Packet decoders.
 *
 * - Transforms [ByteReadPacket] to [RawIncomingPacket]
 */
internal interface PacketCodec {
    /**
     * It's caller's responsibility to close [input]
     * @param input received from sockets.
     * @return decoded
     */
    fun decodeRaw(client: SsoSession, input: ByteReadPacket): RawIncomingPacket

    /**
     * Process [RawIncomingPacket] using [IncomingPacketFactory.decode].
     *
     * This function throws **no** exception and wrap them into [IncomingPacket].
     */
    suspend fun processBody(bot: QQAndroidBot, input: RawIncomingPacket): IncomingPacket?

    companion object : ComponentKey<PacketCodec> {
        val PACKET_DEBUG = systemProp("mirai.debug.network.packet.logger", false)

        internal val PacketLogger: MiraiLoggerWithSwitch by lazy {
            MiraiLogger.create("Packet").withSwitch(PACKET_DEBUG)
        }
    }
}

internal class OicqDecodingException(
    val targetException: Throwable
) : RuntimeException(
    null, targetException,
    true, // enableSuppression
    false, // writableStackTrace
) {
    override fun getStackTrace(): Array<StackTraceElement> {
        return targetException.stackTrace
    }
}

internal class PacketCodecImpl : PacketCodec {

    override fun decodeRaw(client: SsoSession, input: ByteReadPacket): RawIncomingPacket = input.run {
        // login
        val flag1 = readInt()

        PacketLogger.verbose { "开始处理一个包" }

        val flag2 = readByte().toInt()
        val flag3 = readByte().toInt()
        check(flag3 == 0) {
            "Illegal flag3. Expected 0, whereas got $flag3. flag1=$flag1, flag2=$flag2. " +
                    "Remaining=${this.readBytes().toUHexString()}"
        }

        readString(readInt() - 4)// uinAccount

        ByteArrayPool.useInstance(this.remaining.toInt()) { buffer ->
            val size = this.readAvailable(buffer)

            when (flag2) {
                2 -> TEA.decrypt(buffer, DECRYPTER_16_ZERO, size)
                1 -> TEA.decrypt(buffer, client.wLoginSigInfo.d2Key, size)
                0 -> buffer
                else -> error("Unknown flag2=$flag2")
            }.let { decryptedData ->
                when (flag1) {
                    0x0A -> parseSsoFrame(client, decryptedData)
                    0x0B -> parseSsoFrame(client, decryptedData) // 这里可能是 uni?? 但测试时候发现结构跟 sso 一样.
                    else -> error("unknown flag1: ${flag1.toByte().toUHexString()}")
                }
            }.let { raw ->
                when (flag2) {
                    0, 1 -> RawIncomingPacket(raw.commandName, raw.sequenceId, raw.body.readBytes())
                    2 -> RawIncomingPacket(
                        raw.commandName,
                        raw.sequenceId,
                        raw.body.withUse {
                            try {
                                parseOicqResponse(client)
                            } catch (e: Throwable) {
                                throw OicqDecodingException(e)
                            }
                        }
                    )
                    else -> error("Unknown flag2=$flag2")
                }
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
                check(returnCode == 0) {
                    if (returnCode <= -10000) {
                        // https://github.com/mamoe/mirai/issues/470
                        error("returnCode = $returnCode")
                    } else "returnCode = $returnCode"
                }

                if (PacketLogger.isEnabled) {
                    val extraData = readBytes(readInt() - 4)
                    PacketLogger.verbose { "(sso/inner)extraData = ${extraData.toUHexString()}" }
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
                    input.useBytes { data, length ->
                        data.unzip(0, length).let {
                            val size = it.toInt()
                            if (size == it.size || size == it.size + 4) {
                                it.toReadPacket(offset = 4)
                            } else {
                                it.toReadPacket()
                            }
                        }
                    }
                }
                8 -> input
                else -> error("unknown dataCompressed flag: $dataCompressed")
            }

            // body

            return DecodeResult(commandName, ssoSequenceId, packet)
        }

    private fun ByteReadPacket.parseOicqResponse(
        client: SsoSession,
    ): ByteArray {
        readByte().toInt().let {
            check(it == 2) { "$it" }
        }
        this.discardExact(2)
        this.discardExact(2)
        this.readUShort()
        this.readShort()
        this.readUInt().toLong()
        val encryptionMethod = this.readUShort().toInt()

        this.discardExact(1)
        return when (encryptionMethod) {
            4 -> {
                val data =
                    TEA.decrypt(
                        this.readBytes(),
                        client.ecdh.keyPair.initialShareKey,
                        length = (this.remaining - 1).toInt()
                    )

                val peerShareKey =
                    client.ecdh.calculateShareKeyByPeerPublicKey(readUShortLVByteArray().adjustToPublicKey())
                TEA.decrypt(data, peerShareKey)
            }
            3 -> {
                // session
                TEA.decrypt(
                    this.readBytes(),
                    client.wLoginSigInfo.wtSessionTicketKey,
                    length = (this.remaining - 1).toInt()
                )
            }
            0 -> {
                if (client.loginState == 0) {
                    val size = (this.remaining - 1).toInt()
                    val byteArrayBuffer = this.readBytes(size)

                    runCatching {
                        TEA.decrypt(byteArrayBuffer, client.ecdh.keyPair.initialShareKey, length = size)
                    }.getOrElse {
                        TEA.decrypt(byteArrayBuffer, client.randomKey, length = size)
                    }
                } else {
                    TEA.decrypt(this.readBytes(), client.randomKey, length = (this.remaining - 1).toInt())
                }
            }
            else -> error("Illegal encryption method. expected 0 or 4, got $encryptionMethod")
        }
    }

    /**
     * Process [RawIncomingPacket] using [IncomingPacketFactory.decode].
     *
     * This function wraps exceptions into [IncomingPacket]
     */
    override suspend fun processBody(bot: QQAndroidBot, input: RawIncomingPacket): IncomingPacket? {
        val factory = KnownPacketFactories.findPacketFactory(input.commandName) ?: return null

        return kotlin.runCatching {
            input.body.toReadPacket().use { body ->
                when (factory) {
                    is OutgoingPacketFactory -> factory.decode(bot, body)
                    is IncomingPacketFactory -> factory.decode(bot, body, input.sequenceId)
                }
            }
        }.fold(
            onSuccess = { packet ->
                IncomingPacket(input.commandName, input.sequenceId, packet, null)
            },
            onFailure = { exception: Throwable ->
                IncomingPacket(input.commandName, input.sequenceId, null, exception)
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