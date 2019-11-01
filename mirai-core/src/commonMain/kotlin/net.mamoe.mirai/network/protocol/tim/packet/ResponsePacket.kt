package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket

/**
 * 返回包.
 * 在登录完成后, 任意一个 [OutgoingPacket] 发送后服务器都会给返回包. 则一个 [OutgoingPacket] 一定对应一个 [ResponsePacket]
 * 它们都使用 sessionKey 解密.
 * 它们都必须有一个公开的仅有一个 [ByteReadPacket] 参数的构造器.
 *
 * 注意: 需要指定 ID, 通过 [AnnotatedId].
 */
abstract class ResponsePacket(input: ByteReadPacket) : ServerPacket(input) {

    /**
     * 加密过的 [ResponsePacket]. 将会在处理时解密为对应的 [ResponsePacket]
     */
    class Encrypted<P : ResponsePacket>(input: ByteReadPacket, val constructor: (ByteReadPacket) -> P) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): P = constructor(decryptBy(sessionKey)).applySequence(sequenceId)
    }

    companion object {
        @Suppress("FunctionName")
        inline fun <reified P : ResponsePacket> Encrypted(input: ByteReadPacket): Encrypted<P> = Encrypted(input) { P::class.constructors.first().call(it) }
    }
}

