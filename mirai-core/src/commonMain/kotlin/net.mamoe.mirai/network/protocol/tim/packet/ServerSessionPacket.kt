package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket

/**
 * 登录完成之后的所有 packet.
 * 它们都使用 sessionKey 解密.
 * 它们都必须有一个公开的仅有一个 [ByteReadPacket] 参数的构造器.
 *
 * 注意: 需要为指定 ID, 通过 [PacketId].
 */
abstract class ServerSessionPacket(input: ByteReadPacket) : ServerPacket(input) {

    /**
     * 加密过的 [ServerSessionPacket]. 将会在处理时解密为对应的 [ServerSessionPacket]
     */
    class Encrypted<P : ServerSessionPacket>(input: ByteReadPacket, val constructor: (ByteReadPacket) -> P) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): P = constructor(decryptBy(sessionKey)).applySequence(sequenceId)
    }

    companion object {
        @Suppress("FunctionName")
        inline fun <reified P : ServerSessionPacket> Encrypted(input: ByteReadPacket): Encrypted<P> = Encrypted(input) { P::class.constructors.first().call(it) }
    }
}

