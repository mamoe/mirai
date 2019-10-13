package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import kotlin.jvm.JvmStatic


expect object TEA {
    internal fun doOption(data: ByteArray, key: ByteArray, encrypt: Boolean): ByteArray

    @JvmStatic
    fun encrypt(source: ByteArray, key: ByteArray): ByteArray

    @JvmStatic
    fun decrypt(source: ByteArray, key: ByteArray): ByteArray

    @JvmStatic
    fun decrypt(source: ByteArray, key: IoBuffer): ByteArray

    @JvmStatic
    fun decrypt(source: ByteArray, keyHex: String): ByteArray
}

fun ByteArray.decryptBy(key: ByteArray): ByteArray = TEA.decrypt(this, key)
fun ByteArray.decryptBy(key: IoBuffer): ByteArray = TEA.decrypt(this, key)
fun ByteArray.decryptBy(key: String): ByteArray = TEA.decrypt(this, key)