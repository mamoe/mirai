package net.mamoe.mirai.util

import net.mamoe.mirai.network.Protocol

/**
 * @author Him188moe @ Mirai Project
 */
object Utils {
    fun toHexString(byteArray: ByteArray): String = byteArray.joinToString(" ") { it.toString(16) }
}

fun ByteArray.toHexString(): String = Utils.toHexString(this)

fun Byte.toHexString(): String = this.toString(16)

fun String.hexToBytes(): ByteArray = Protocol.hexToBytes(this)