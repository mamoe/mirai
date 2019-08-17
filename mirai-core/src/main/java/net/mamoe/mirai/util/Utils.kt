package net.mamoe.mirai.util

import net.mamoe.mirai.network.Protocol
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/**
 * @author Him188moe @ Mirai Project
 */
object Utils {
    fun toHexString(byteArray: ByteArray, separator: String = ","): String = byteArray.joinToString(separator) {
        var ret = it.toString(16).toUpperCase();
        if (ret.length == 1) {
            ret = "0$ret";
        }
        return@joinToString ret;
    }

    @ExperimentalUnsignedTypes
    fun toHexString(byteArray: UByteArray, separator: String = ","): String = byteArray.joinToString(separator) {
        var ret = it.toString(16).toUpperCase();
        if (ret.length == 1) {
            ret = "0$ret";
        }
        return@joinToString ret;
    }
}

fun ByteArray.toHexString(separator: String = ", "): String = Utils.toHexString(this, separator)
@ExperimentalUnsignedTypes
fun UByteArray.toHexString(separator: String = ", "): String = Utils.toHexString(this, separator)

fun Byte.toHexString(): String = this.toString(16)

fun String.hexToBytes(): ByteArray = Protocol.hexToBytes(this)
@ExperimentalUnsignedTypes
fun String.hexToUBytes(): UByteArray = Protocol.hexToUBytes(this)

open class ByteArrayDataOutputStream : DataOutputStream(ByteArrayOutputStream()) {
    open fun toByteArray(): ByteArray = (out as ByteArrayOutputStream).toByteArray()
    @ExperimentalUnsignedTypes
    open fun toUByteArray(): UByteArray = (out as ByteArrayOutputStream).toByteArray().toUByteArray();
}