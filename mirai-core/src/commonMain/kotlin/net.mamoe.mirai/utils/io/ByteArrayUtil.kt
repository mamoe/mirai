@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.utils.io

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.String
import kotlinx.io.core.use
import net.mamoe.mirai.utils.checkOffsetAndLength
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic


@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
@UseExperimental(ExperimentalUnsignedTypes::class)
fun List<Byte>.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }

    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toUByte().toString(16).toUpperCase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
@UseExperimental(ExperimentalUnsignedTypes::class)
fun ByteArray.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
    this.checkOffsetAndLength(offset, length)
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toUByte().toString(16).toUpperCase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

@JvmSynthetic
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
@ExperimentalUnsignedTypes
fun UByteArray.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toByte().toUByte().toString(16).toUpperCase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.encodeToString(): String = String(this)

fun ByteArray.toReadPacket(offset: Int = 0, length: Int = this.size) = ByteReadPacket(this, offset = offset, length = length)

@UseExperimental(ExperimentalContracts::class)
inline fun <R> ByteArray.read(t: ByteReadPacket.() -> R): R {
    contract {
        callsInPlace(t, InvocationKind.EXACTLY_ONCE)
    }
    return this.toReadPacket().use(t)
}

fun ByteArray.cutTail(length: Int): ByteArray = this.copyOfRange(0, this.size - length)