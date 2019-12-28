package net.mamoe.mirai.utils.cryptor

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.utils.io.encryptAndWrite


/**
 * [ByteArray] 解密器
 */
interface DecrypterByteArray : Decrypter {
    val value: ByteArray
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input.decryptBy(value)
}

/**
 * [IoBuffer] 解密器
 */
interface DecrypterIoBuffer : Decrypter {
    val value: IoBuffer
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input.decryptBy(value)
}

/**
 * 连接在一起的解密器
 */
inline class LinkedDecrypter(inline val block: (ByteReadPacket) -> ByteReadPacket) : Decrypter {
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = block(input)
}

object NoDecrypter : Decrypter,
    DecrypterType<NoDecrypter> {
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input
}

/**
 * 解密器
 */ 
interface Decrypter {
    fun decrypt(input: ByteReadPacket): ByteReadPacket
    /**
     * 连接后将会先用 this 解密, 再用 [another] 解密
     */
    operator fun plus(another: Decrypter): Decrypter =
        LinkedDecrypter { another.decrypt(this.decrypt(it)) }
}
 
interface DecrypterType<D : Decrypter>

inline fun BytePacketBuilder.encryptAndWrite(key: DecrypterByteArray, encoder: BytePacketBuilder.() -> Unit) =
    this.encryptAndWrite(key.value, encoder)
