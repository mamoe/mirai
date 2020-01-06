package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
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

    companion object {
        private val sequenceId: AtomicInt = atomic(1)

        fun atomicNextSequenceId(): Int {
            TODO("使用 SSO ")
            val id = sequenceId.getAndAdd(1)
            if (id > Short.MAX_VALUE.toInt() * 2) {
                sequenceId.value = 0
                return atomicNextSequenceId()
            }
            // return id.toShort()
        }
    }
}

private val DECRYPTER_16_ZERO = ByteArray(16)

internal typealias PacketConsumer = (packet: Packet, packetId: PacketId, ssoSequenceId: Int) -> Unit

internal object KnownPacketFactories : List<PacketFactory<*, *>> by mutableListOf() {

    fun findPacketFactory(commandName: String): PacketFactory<*, *> = this.first { it.id.commandName == commandName }

    fun findPacketFactory(commandId: Int): PacketFactory<*, *> = this.first { it.id.commandName == commandName }

    suspend inline fun parseIncomingPacket(bot: QQAndroidBot, rawInput: ByteReadPacket, consumer: PacketConsumer) =
        rawInput.debugPrintIfFail("Incoming packet") {
            require(rawInput.remaining < Int.MAX_VALUE) { "rawInput is too long" }
            val expectedLength = readInt() - 4
            check(rawInput.remaining.toInt() == expectedLength) { "Invalid packet length. Expected $expectedLength, got ${rawInput.remaining} Probably packets merged? " }
            // login
            when (val flag1 = readInt()) {
                0x0A -> when (val flag2 = readByte().toInt()) {
                    0x02 -> {
                        val flag3 = readByte().toInt()
                        check(flag3 == 0) { "Illegal flag3. Expected 0, got $flag3" }

                        discardExact(readInt() - 4) // uinAccount

                        parseLoginSsoPacket(bot, decryptBy(DECRYPTER_16_ZERO), consumer)
                    }
                    else -> error("Illegal flag2. Expected 0x02, got $flag2")
                }
                else -> error("Illegal flag1. Expected 0x0A or 0x0B, got $flag1")
            }
        }

    @UseExperimental(ExperimentalUnsignedTypes::class)
    private suspend inline fun parseLoginSsoPacket(bot: QQAndroidBot, rawInput: ByteReadPacket, consumer: PacketConsumer) =
        rawInput.debugPrintIfFail("Login sso packet") {
            val commandName: String
            val ssoSequenceId: Int
            readIoBuffer(readInt() - 4).withUse {
                ssoSequenceId = readInt()
                check(readInt() == 0)
                val loginExtraData = readIoBuffer(readInt() - 4)

                commandName = readString(readInt() - 4)
                val unknown = readBytes(readInt() - 4)
                if (unknown.toInt() != 0x02B05B8B) DebugLogger.debug("got new unknown: $unknown")

                check(readInt() == 0)
            }

            val packetFactory = findPacketFactory(commandName)

            val qq: Long
            val subCommandId: Int
            readIoBuffer(readInt() - 4).withUse {
                check(readByte().toInt() == 2)
                discardExact(2) // 27 + 2 + body.size
                discardExact(2) // const, =8001
                readShort() // commandId
                readShort() // innerSequenceId
                qq = readInt().toLong()

                discardExact(1) // const = 0
                val packet = when (val encryptionMethod = readByte().toInt()) {
                    4 -> { // peer public key, ECDH
                        packetFactory.run {
                            bot.client.ecdh.calculateShareKeyByPeerPublicKey(readUShortLVByteArray().adjustToPublicKey()).read {
                                decode(bot)
                            }
                        }
                    }
                    else -> error("Illegal encryption method. expected 4, got $encryptionMethod")
                }

                consumer(packet, packetFactory.id, ssoSequenceId)
            }
        }
}

@UseExperimental(ExperimentalContracts::class)
internal inline fun <I : Closeable, R> I.withUse(block: I.() -> R): R {
    contract {
        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block(this)
    } finally {
        close()
    }
}