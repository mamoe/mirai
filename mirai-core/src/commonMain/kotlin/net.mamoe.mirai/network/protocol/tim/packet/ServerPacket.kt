@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.utils.decryptBy
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.parseServerPacket
import net.mamoe.mirai.utils.io.toReadPacket
import kotlin.properties.Delegates


/**
 * 来自服务器的数据包
 *
 * @see parseServerPacket 解析包种类
 */
abstract class ServerPacket(val input: ByteReadPacket) : Packet(), Closeable {
    override val id: UShort by lazy { super.id }

    override var sequenceId: UShort by Delegates.notNull()

    open fun decode() {

    }

    override fun close() = this.input.close()

    override fun toString(): String = this.packetToString()

    fun <S : ServerPacket> S.applySequence() = this.applySequence(this@ServerPacket.sequenceId)
}

fun <S : ServerPacket> S.applySequence(sequenceId: UShort): S {
    this.sequenceId = sequenceId
    return this
}

fun ServerPacket.decryptBy(key: ByteArray): ByteReadPacket = decryptAsByteArray(key) { data -> ByteReadPacket(data, 0) }
fun ServerPacket.decryptBy(key: IoBuffer): ByteReadPacket = decryptAsByteArray(key) { data -> ByteReadPacket(data, 0) }
fun ServerPacket.decryptBy(keyHex: String): ByteReadPacket = this.decryptBy(keyHex.hexToBytes())

fun ServerPacket.decryptBy(key1: ByteArray, key2: ByteArray): ByteReadPacket =
    this.decryptAsByteArray(key1) { data ->
        data.decryptBy(key2).toReadPacket()
    }


fun ServerPacket.decryptBy(key1: String, key2: ByteArray): ByteReadPacket = this.decryptBy(key1.hexToBytes(), key2)
fun ServerPacket.decryptBy(key1: String, key2: IoBuffer): ByteReadPacket = this.decryptBy(key1.hexToBytes(), key2.readBytes())
fun ServerPacket.decryptBy(key1: ByteArray, key2: String): ByteReadPacket = this.decryptBy(key1, key2.hexToBytes())
fun ServerPacket.decryptBy(keyHex1: String, keyHex2: String): ByteReadPacket = this.decryptBy(keyHex1.hexToBytes(), keyHex2.hexToBytes())

inline fun <R> ServerPacket.decryptAsByteArray(key: ByteArray, consumer: (ByteArray) -> R): R =
    ByteArrayPool.useInstance {
        val length = input.remaining.toInt() - 1
        input.readFully(it, 0, length)
        consumer(it.decryptBy(key, length))
    }.also { input.close() }

inline fun <R> ServerPacket.decryptAsByteArray(keyHex: String, consumer: (ByteArray) -> R): R = this.decryptAsByteArray(keyHex.hexToBytes(), consumer)
inline fun <R> ServerPacket.decryptAsByteArray(key: IoBuffer, consumer: (ByteArray) -> R): R =
    ByteArrayPool.useInstance {
        val length = input.remaining.toInt() - 1
        input.readFully(it, 0, length)
        consumer(it.decryptBy(key, length))
    }.also { input.close() }
