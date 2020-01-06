package net.mamoe.mirai.utils.cryptor

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.toReadPacket


/**
 * [ByteArray] 解密器
 */
interface DecrypterByteArray : Decrypter {
    val value: ByteArray
    override fun decrypt(input: ByteReadPacket, offset: Int, length: Int): ByteReadPacket = input.decryptBy(value, offset, length)
}

/**
 * [IoBuffer] 解密器
 */
interface DecrypterIoBuffer : Decrypter {
    val value: IoBuffer
    override fun decrypt(input: ByteReadPacket, offset: Int, length: Int): ByteReadPacket = input.decryptBy(value, offset, length)
}

/**
 * 连接在一起的解密器
 */
inline class LinkedDecrypter(inline val block: (input: ByteReadPacket, offset: Int, length: Int) -> ByteReadPacket) : Decrypter {
    override fun decrypt(input: ByteReadPacket, offset: Int, length: Int): ByteReadPacket = block(input, offset, length)
}

object NoDecrypter : Decrypter, DecrypterType<NoDecrypter> {
    override fun decrypt(input: ByteReadPacket, offset: Int, length: Int): ByteReadPacket {
        if (offset == 0 && length == input.remaining.toInt()) {
            return input
        }

        ByteArrayPool.useInstance { buffer ->
            input.readFully(buffer, offset, length)
            return buffer.toReadPacket()
        }
    }
}

fun Decrypter.decrypt(input: ByteReadPacket): ByteReadPacket = this.decrypt(input, 0, input.remaining.toInt())

/**
 * 解密器
 */
interface Decrypter {
    // do not write with default args. NoSuchMethodError when inline classes override this function
    fun decrypt(input: ByteReadPacket, offset: Int, length: Int): ByteReadPacket
    /**
     * 连接后将会先用 this 解密, 再用 [another] 解密
     */
    operator fun plus(another: Decrypter): Decrypter =
        LinkedDecrypter { input: ByteReadPacket, offset: Int, length: Int -> another.decrypt(this.decrypt(input, offset, length)) }
}

interface DecrypterType<D : Decrypter>

inline fun BytePacketBuilder.encryptAndWrite(key: DecrypterByteArray, encoder: BytePacketBuilder.() -> Unit) =
    this.encryptAndWrite(key.value, encoder)
