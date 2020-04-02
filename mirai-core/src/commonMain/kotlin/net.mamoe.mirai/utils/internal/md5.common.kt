@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils.internal

expect abstract class InputStream {
    open fun available(): Int
    open fun close()
    abstract fun read(): Int
    open fun read(b: ByteArray): Int
    open fun read(b: ByteArray, offset: Int, len: Int): Int
    open fun skip(n: Long): Long
}

internal expect fun InputStream.md5(): ByteArray
internal expect fun ByteArray.md5(offset: Int = 0, length: Int = this.size - offset): ByteArray

@Suppress("DuplicatedCode") // false positive. `this` is not the same for `List<Byte>` and `ByteArray`
internal fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}

internal inline fun InputStream.readInSequence(block: (Int) -> Unit) {
    var read: Int
    while (this.read().also { read = it } != -1) {
        block(read)
    }
}
