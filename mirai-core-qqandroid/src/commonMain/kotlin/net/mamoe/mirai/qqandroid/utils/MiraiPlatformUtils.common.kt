package net.mamoe.mirai.qqandroid.utils

import io.ktor.client.HttpClient

internal expect object MiraiPlatformUtils {
    fun unzip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    fun zip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    fun gzip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    fun ungzip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray


    fun md5(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    inline fun md5(str: String): ByteArray

    fun localIpAddress(): String

    /**
     * Ktor HttpClient. 不同平台使用不同引擎.
     */
    val Http: HttpClient
}

@Suppress("DuplicatedCode") // false positive. `this` is not the same for `List<Byte>` and `ByteArray`
internal fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}