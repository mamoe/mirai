@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.utils.io

import kotlinx.io.core.*


fun ByteReadPacket.readRemainingBytes(
    n: Int = remaining.toInt()//not that safe but adequate
): ByteArray = ByteArray(n).also { readAvailable(it, 0, n) }

fun ByteReadPacket.readIoBuffer(
    n: Int = remaining.toInt()//not that safe but adequate
): IoBuffer = IoBuffer.Pool.borrow().also { this.readFully(it, n) }

fun ByteReadPacket.readIoBuffer(n: Number) = this.readIoBuffer(n.toInt())

fun Input.readIP(): String = buildString(4 + 3) {
    repeat(4) {
        val byte = readUByte()
        this.append(byte.toString())
        if (it != 3) this.append(".")
    }
}

fun Input.readUShortLVString(): String = String(this.readUShortLVByteArray())

fun Input.readUShortLVByteArray(): ByteArray = this.readBytes(this.readUShort().toInt())

private inline fun <R> inline(block: () -> R): R = block()

fun Input.readTLVMap(expectingEOF: Boolean = false, tagSize: Int = 1): MutableMap<UInt, ByteArray> {
    val map = mutableMapOf<UInt, ByteArray>()
    var type: UShort = 0u

    while (inline {
            try {
                type = when (tagSize) {
                    1 -> readUByte().toUShort()
                    2 -> readUShort()
                    else -> error("Unsupported tag size: $tagSize")
                }
            } catch (e: EOFException) {
                if (expectingEOF) {
                    return map
                }
                throw e
            }
            type
        }.toUByte() != UByte.MAX_VALUE) {

        check(!map.containsKey(type.toUInt())) {
            "Count not readTLVMap: duplicated key 0x${type.toUInt().toUHexString("")}. " +
                    "map=$map" +
                    ", duplicating value=${this.readUShortLVByteArray()}" +
                    ", remaining=" + if (expectingEOF) this.readBytes().toUHexString() else "[Not expecting EOF]"
        }
        map[type.toUInt()] = this.readUShortLVByteArray()
    }
    return map
}

fun Map<*, ByteArray>.printTLVMap(name: String) =
    debugPrintln("TLVMap $name= " + this.mapValues { (_, value) -> value.toUHexString() })

fun Input.readString(length: Number): String = String(this.readBytes(length.toInt()))

private const val TRUE_BYTE_VALUE: Byte = 1
fun Input.readBoolean(): Boolean = this.readByte() == TRUE_BYTE_VALUE
fun Input.readLVNumber(): Number {
    return when (this.readShort().toInt()) {
        1 -> this.readByte()
        2 -> this.readShort()
        4 -> this.readInt()
        8 -> this.readLong()
        else -> throw UnsupportedOperationException()
    }
}