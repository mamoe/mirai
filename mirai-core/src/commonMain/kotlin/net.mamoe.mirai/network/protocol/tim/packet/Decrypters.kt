package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.utils.decryptBy


/**
 * 会话密匙
 */
internal inline class SessionKey(override val value: ByteArray) : DecrypterByteArray {
    companion object Type : DecrypterType<SessionKey>
}

/**
 * [ByteArray] 解密器
 */
internal interface DecrypterByteArray : Decrypter {
    val value: ByteArray
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input.decryptBy(value)
}

/**
 * [IoBuffer] 解密器
 */
internal interface DecrypterIoBuffer : Decrypter {
    val value: IoBuffer
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input.decryptBy(value)
}

/**
 * 连接在一起的解密器
 */
internal inline class LinkedDecrypter(inline val block: (ByteReadPacket) -> ByteReadPacket) : Decrypter {
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = block(input)
}

internal object NoDecrypter : Decrypter, DecrypterType<NoDecrypter> {
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input
}

/**
 * 解密器
 */
internal interface Decrypter {
    fun decrypt(input: ByteReadPacket): ByteReadPacket
    /**
     * 连接后将会先用 this 解密, 再用 [another] 解密
     */
    operator fun plus(another: Decrypter): Decrypter = LinkedDecrypter { another.decrypt(this.decrypt(it)) }
}

internal interface DecrypterType<D : Decrypter>