@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.utils.decryptBy
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.hexToBytes


fun ByteReadPacket.decryptBy(key: ByteArray): ByteReadPacket = decryptAsByteArray(key) { data -> ByteReadPacket(data, 0) }
fun ByteReadPacket.decryptBy(key: IoBuffer): ByteReadPacket = decryptAsByteArray(key) { data -> ByteReadPacket(data, 0) }
fun ByteReadPacket.decryptBy(keyHex: String): ByteReadPacket = decryptBy(keyHex.hexToBytes())

inline fun <R> ByteReadPacket.decryptAsByteArray(key: ByteArray, consumer: (ByteArray) -> R): R =
    ByteArrayPool.useInstance {
        val length = remaining.toInt()
        readFully(it, 0, length)
        consumer(it.decryptBy(key, length))
    }.also { close() }

inline fun <R> ByteReadPacket.decryptAsByteArray(keyHex: String, consumer: (ByteArray) -> R): R =
    this.decryptAsByteArray(keyHex.hexToBytes(), consumer)

inline fun <R> ByteReadPacket.decryptAsByteArray(key: IoBuffer, consumer: (ByteArray) -> R): R =
    ByteArrayPool.useInstance {
        val length = remaining.toInt()
        readFully(it, 0, length)
        consumer(it.decryptBy(key, length))
    }.also { close() }
