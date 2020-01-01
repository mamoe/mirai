@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.pool.useInstance
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.cryptor.Decrypter
import net.mamoe.mirai.utils.cryptor.DecrypterType
import net.mamoe.mirai.utils.cryptor.readProtoMap
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.debugPrint
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.toUHexString


/**
 * 一种数据包的处理工厂. 它可以解密解码服务器发来的这个包, 也可以编码加密要发送给服务器的这个包
 * 应由一个 `object` 实现, 且实现 `operator fun invoke`
 *
 * @param TPacket 服务器回复包解析结果
 * @param TDecrypter 服务器回复包解密器
 */
abstract class PacketFactory<out TPacket : Packet, TDecrypter : Decrypter>(val decrypterType: DecrypterType<TDecrypter>) {

    @Suppress("PropertyName")
    internal var _id: PacketId = NullPacketId

    /**
     * 包 ID.
     */
    open val id: PacketId get() = _id

    /**
     * **解码**服务器的回复数据包
     */
    abstract suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): TPacket

    fun <T> ByteReadPacket.decodeProtoPacket(
        deserializer: DeserializationStrategy<T>,
        debuggingTag: String? = null
    ): T {
        val headLength = readInt()
        val protoLength = readInt()
        if (debuggingTag != null) {
            readBytes(headLength).debugPrint("$debuggingTag head")
        } else {
            discardExact(headLength)
        }
        val bytes = readBytes(protoLength)
        // println(ByteReadPacket(bytes).readProtoMap())

        if (debuggingTag != null) {
            bytes.read { readProtoMap() }.toString().debugPrint("$debuggingTag proto")
        }

        return ProtoBuf.load(deserializer, bytes)
    }

    companion object {
        private val sequenceId: AtomicInt = atomic(1)

        fun atomicNextSequenceId(): UShort = atomicNextSequenceId0().toUShort()

        private fun atomicNextSequenceId0(): Int {
            val id = sequenceId.getAndAdd(1)
            if (id > Short.MAX_VALUE.toInt() * 2) {
                sequenceId.getAndSet(0) // do not `sequenceId.value = 0`, causes a bug
                return sequenceId.getAndAdd(1)
            }
            return id
        }
    }
}

internal object UnknownPacketFactory : SessionPacketFactory<UnknownPacket>() {
    override suspend fun BotNetworkHandler.handlePacket(packet: UnknownPacket) {
        ByteArrayPool.useInstance {
            packet.body.readAvailable(it)
            bot.logger.debug("UnknownPacket(${packet.id.value.toUHexString()}) = " + it.toUHexString())
        }
        packet.body.close()
    }

    override suspend fun ByteReadPacket.decode(
        id: PacketId,
        sequenceId: UShort,
        handler: BotNetworkHandler
    ): UnknownPacket {
        return UnknownPacket(id, this)
    }
}

internal object IgnoredPacketFactory : SessionPacketFactory<IgnoredPacket>() {
    override suspend fun ByteReadPacket.decode(
        id: PacketId,
        sequenceId: UShort,
        handler: BotNetworkHandler
    ): IgnoredPacket = IgnoredPacket(id)
}