package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.NullPacketId
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.NullPacketId.commandName
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.PacketId
import net.mamoe.mirai.utils.cryptor.Decrypter
import net.mamoe.mirai.utils.cryptor.DecrypterType
import net.mamoe.mirai.utils.cryptor.adjustToPublicKey
import net.mamoe.mirai.utils.cryptor.decryptBy
import net.mamoe.mirai.utils.io.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * 一种数据包的处理工厂. 它可以解密解码服务器发来的这个包, 也可以编码加密要发送给服务器的这个包
 * 应由一个 `object` 实现, 且实现 `operator fun invoke`
 *
 * @param TPacket 服务器回复包解析结果
 * @param TDecrypter 服务器回复包解密器
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
internal abstract class PacketFactory<out TPacket : Packet, TDecrypter : Decrypter>(val decrypterType: DecrypterType<TDecrypter>) {

    @Suppress("PropertyName")
    internal var _id: PacketId = NullPacketId

    /**
     * 包 ID.
     */
    open val id: PacketId get() = _id

    /**
     * **解码**服务器的回复数据包
     */
    abstract suspend fun ByteReadPacket.decode(bot: QQAndroidBot): TPacket
}

@JvmName("decode0")
private suspend inline fun <P : Packet> PacketFactory<P, *>.decode(bot: QQAndroidBot, packet: ByteReadPacket): P {
    return this.run {
        packet.decode(bot)
    }
}

private val DECRYPTER_16_ZERO = ByteArray(16)

internal typealias PacketConsumer = suspend (packet: Packet, packetId: PacketId, ssoSequenceId: Int) -> Unit

internal object KnownPacketFactories : List<PacketFactory<*, *>> by mutableListOf(
    LoginPacket
) {

    fun findPacketFactory(commandName: String): PacketFactory<*, *> = this.first { it.id.commandName == commandName }

    fun findPacketFactory(commandId: Int): PacketFactory<*, *> = this.first { it.id.commandName == commandName }

    // do not inline. Exceptions thrown will not be reported correctly
    suspend fun parseIncomingPacket(bot: QQAndroidBot, rawInput: ByteReadPacket, consumer: PacketConsumer) =
        rawInput.debugPrintIfFail("Incoming packet") {
            require(remaining < Int.MAX_VALUE) { "rawInput is too long" }
            val expectedLength = readInt() - 4
            check(remaining.toInt() == expectedLength) { "Invalid packet length. Expected $expectedLength, got ${rawInput.remaining} Probably packets merged? " }
            // login
            when (val flag1 = readInt()) {
                0x0A -> when (val flag2 = readByte().toInt()) {
                    0x02 -> {
                        val flag3 = readByte().toInt()
                        check(flag3 == 0) { "Illegal flag3. Expected 0, got $flag3" }

                        bot.logger.verbose(readString(readInt() - 4)) // uinAccount

                        //debugPrint("remaining")
                        parseLoginSsoPacket(bot, decryptBy(DECRYPTER_16_ZERO), consumer)
                    }
                    else -> error("Illegal flag2. Expected 0x02, got $flag2")
                }
                else -> error("Illegal flag1. Expected 0x0A or 0x0B, got $flag1")
            }
        }

    @UseExperimental(ExperimentalUnsignedTypes::class)
    private suspend fun parseLoginSsoPacket(bot: QQAndroidBot, rawInput: ByteReadPacket, consumer: PacketConsumer) =
        rawInput.debugPrintIfFail("Login sso packet") {
            val commandName: String
            val ssoSequenceId: Int
            readIoBuffer(readInt() - 4).withUse {
                ssoSequenceId = readInt()
                check(readInt() == 0)
                val loginExtraData = readIoBuffer(readInt() - 4)

                commandName = readString(readInt() - 4)
                val unknown = readBytes(readInt() - 4)
                if (unknown.toInt() != 0x02B05B8B) DebugLogger.debug("got new unknown: ${unknown.toUHexString()}")

                check(readInt() == 0)
            }

            bot.logger.verbose(commandName)
            val packetFactory = findPacketFactory(commandName)

            val qq: Long
            val subCommandId: Int
            readIoBuffer(readInt() - 4).withUse {
                check(readByte().toInt() == 2)
                this.discardExact(2) // 27 + 2 + body.size
                this.discardExact(2) // const, =8001
                this.readUShort() // commandId
                this.readShort() // const, =0x0001
                qq = this.readUInt().toLong()
                val encryptionMethod = this.readUShort().toInt()

                this.discardExact(1) // const = 0
                val packet = when (encryptionMethod) {
                    4 -> { // peer public key, ECDH
                        var data = this.decryptBy(bot.client.ecdh.keyPair.shareKey, this.readRemaining - 1)

                        val peerShareKey = bot.client.ecdh.calculateShareKeyByPeerPublicKey(readUShortLVByteArray().adjustToPublicKey())
                        data = data.decryptBy(peerShareKey)

                        packetFactory.decode(bot, data.toReadPacket())
                    }
                    0 -> {
                        val data = if (bot.client.loginState == 0) {
                            ByteArrayPool.useInstance { byteArrayBuffer ->
                                val size = this.readRemaining - 1
                                this.readFully(byteArrayBuffer, 0, size)

                                runCatching {
                                    byteArrayBuffer.decryptBy(bot.client.ecdh.keyPair.shareKey, size)
                                }.getOrElse {
                                    byteArrayBuffer.decryptBy(bot.client.tgtgtKey, size)
                                } // 这里实际上应该用 privateKey(另一个random出来的key)
                            }
                        } else {
                            this.decryptBy(bot.client.tgtgtKey, 0, this.readRemaining - 1)
                        }

                        packetFactory.decode(bot, data.toReadPacket())

                    }
                    else -> error("Illegal encryption method. expected 0 or 4, got $encryptionMethod")
                }

                consumer(packet, packetFactory.id, ssoSequenceId)
            }
        }
}

@UseExperimental(ExperimentalContracts::class)
internal inline fun <I : IoBuffer, R> I.withUse(block: I.() -> R): R {
    contract {
        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block(this)
    } finally {
        this.release(IoBuffer.Pool)
    }
}