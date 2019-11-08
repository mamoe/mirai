@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.atomicfu.atomic
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.toUHexString

object PacketFactoryList : MutableList<PacketFactory<*, *>> by mutableListOf()

/**
 * 一种数据包的处理工厂. 它可以解密解码服务器发来的这个包, 也可以编码加密要发送给服务器的这个包
 * 应由一个 `object` 实现, 且实现 `operator fun invoke`
 *
 * @param TPacket 服务器回复包解析结果
 * @param TDecrypter 服务器回复包解密器
 */
abstract class PacketFactory<out TPacket : Packet, TDecrypter : Decrypter>(val decrypterType: DecrypterType<TDecrypter>) {

    /**
     * 2 Ubyte.
     * 读取注解 [AnnotatedId]
     */
    private val annotatedId: AnnotatedId
        get() = (this::class.annotations.firstOrNull { it is AnnotatedId } as? AnnotatedId)
            ?: error("Annotation AnnotatedId not found")

    /**
     * 包 ID.
     */
    open val id: PacketId by lazy { annotatedId.id }

    init {
        @Suppress("LeakingThis")
        PacketFactoryList.add(this)
    }

    /**
     * **解码**服务器的回复数据包
     */
    abstract suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): TPacket

    companion object {
        private val sequenceIdInternal = atomic(1)

        @PublishedApi
        internal fun atomicNextSequenceId(): UShort = sequenceIdInternal.getAndIncrement().toUShort()
    }
}

object UnknownPacketFactory : SessionPacketFactory<UnknownPacket>() {
    override suspend fun BotNetworkHandler<*>.processPacket(packet: UnknownPacket) {
        ByteArrayPool.useInstance {
            packet.body.readAvailable(it)
            bot.logger.debug("Unknown packet(${packet.id.value}) body = " + it.toUHexString())
        }
        packet.body.close()
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): UnknownPacket {
        return UnknownPacket(id, this)
    }
}

object IgnoredPacketFactory : SessionPacketFactory<IgnoredPacket>() {
    override suspend fun BotNetworkHandler<*>.processPacket(packet: IgnoredPacket) {
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): IgnoredPacket = IgnoredPacket
}