package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.NullPacketId
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.PacketId
import net.mamoe.mirai.utils.cryptor.Decrypter
import net.mamoe.mirai.utils.cryptor.DecrypterType

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
    abstract suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): TPacket

    companion object {
        private val sequenceId: AtomicInt = atomic(1)

        fun atomicNextSequenceId(): UShort {
            val id = sequenceId.getAndAdd(1)
            if (id > Short.MAX_VALUE.toInt() * 2) {
                sequenceId.value = 0
                return atomicNextSequenceId()
            }
            return id.toUShort()
        }
    }
}