package net.mamoe.mirai.util

import net.mamoe.mirai.network.Protocol
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*
import java.util.zip.CRC32

/**
 * @author Him188moe
 */
object Utils {
    fun toHexString(byteArray: ByteArray, separator: String = " "): String = byteArray.joinToString(separator) {
        var ret = it.toString(16).toUpperCase();
        if (ret.length == 1) {
            ret = "0$ret";
        }
        return@joinToString ret;
    }

    @ExperimentalUnsignedTypes
    fun toHexString(byteArray: UByteArray, separator: String = " "): String = byteArray.joinToString(separator) {
        var ret = it.toString(16).toUpperCase();
        if (ret.length == 1) {
            ret = "0$ret";
        }
        return@joinToString ret;
    }
}

fun ByteArray.toHexString(): String = toHexString(" ")
fun ByteArray.toHexString(separator: String = " "): String = Utils.toHexString(this, separator)
@ExperimentalUnsignedTypes
fun ByteArray.toUHexString(separator: String = " "): String = this.toUByteArray().toHexString(separator)

@ExperimentalUnsignedTypes
fun ByteArray.toUHexString(): String = this.toUByteArray().toHexString()

@ExperimentalUnsignedTypes
fun UByteArray.toHexString(separator: String = " "): String = Utils.toHexString(this, separator)

@ExperimentalUnsignedTypes
fun UByteArray.toHexString(): String = toHexString(" ")

@ExperimentalUnsignedTypes
fun Byte.toHexString(): String = this.toUByte().toString(16)

@ExperimentalUnsignedTypes
fun String.hexToBytes(): ByteArray = Protocol.hexToBytes(this)
@ExperimentalUnsignedTypes
fun String.hexToUBytes(): UByteArray = Protocol.hexToUBytes(this)

@ExperimentalUnsignedTypes
fun String.hexToShort(): Short = hexToBytes().let { ((it[1].toInt() shl 8) + it[0]).toShort() }

@ExperimentalUnsignedTypes
fun String.hexToByte(): Byte = hexToBytes()[0]

open class ByteArrayDataOutputStream : DataOutputStream(ByteArrayOutputStream()) {
    open fun toByteArray(): ByteArray = (out as ByteArrayOutputStream).toByteArray()
    @ExperimentalUnsignedTypes
    open fun toUByteArray(): UByteArray = (out as ByteArrayOutputStream).toByteArray().toUByteArray();
}

fun lazyEncode(t: (ByteArrayDataOutputStream) -> Unit): ByteArray = ByteArrayDataOutputStream().let { t(it); return it.toByteArray() }

@ExperimentalUnsignedTypes
fun getRandomKey(length: Int): ByteArray {
    val bytes = LinkedList<Byte>();
    repeat(length) { bytes.add((Math.random() * 255).toByte()) }
    return bytes.toByteArray();
}

fun getCrc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }