package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes
import kotlin.jvm.JvmStatic

internal expect object TEA { //TODO 优化为 buffer
    internal fun doOption(data: ByteArray, key: ByteArray, encrypt: Boolean): ByteArray

    @JvmStatic
    fun encrypt(source: ByteArray, key: ByteArray): ByteArray

    @JvmStatic
    fun decrypt(source: ByteArray, key: ByteArray): ByteArray
}

fun ByteArray.decryptBy(key: ByteArray): ByteArray = TEA.decrypt(checkLength(), key)
fun ByteArray.decryptBy(key: IoBuffer): ByteArray = TEA.decrypt(checkLength(), key.readBytes())
fun ByteArray.decryptBy(keyHex: String): ByteArray = TEA.decrypt(checkLength(), keyHex.hexToBytes())

fun ByteArray.encryptBy(key: ByteArray): ByteArray = TEA.encrypt(checkLength(), key)
fun ByteArray.encryptBy(keyHex: String): ByteArray = TEA.encrypt(checkLength(), keyHex.hexToBytes())

private fun ByteArray.checkLength(): ByteArray {
    size.let {
        require(it % 8 == 0 && it >= 16) { "data must len % 8 == 0 && len >= 16 but given $it" }
    }
    return this
}