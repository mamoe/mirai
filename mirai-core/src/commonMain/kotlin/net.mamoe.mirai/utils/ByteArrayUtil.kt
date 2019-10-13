@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.String
import kotlin.jvm.JvmOverloads

@JvmOverloads
fun ByteArray.toHexString(separator: String = " "): String = this.joinToString(separator) {
    var ret = it.toString(16).toUpperCase()
    if (ret.length == 1) {
        ret = "0$ret"
    }
    return@joinToString ret
}

@JvmOverloads
fun ByteArray.toUHexString(separator: String = " "): String = this.toUByteArray().toUHexString(separator)

fun ByteArray.stringOf(): String = String(this)

//@JvmSynthetic TODO 等待 kotlin 修复 bug 后添加这个注解
@JvmOverloads
fun UByteArray.toUHexString(separator: String = " "): String = this.joinToString(separator) {
    var ret = it.toString(16).toUpperCase()
    if (ret.length == 1) {
        ret = "0$ret"
    }
    return@joinToString ret
}

fun ByteArray.toReadPacket() = ByteReadPacket(this)

fun <R> ByteArray.read(t: ByteReadPacket.() -> R): R = this.toReadPacket().run(t)

fun ByteArray.cutTail(length: Int): ByteArray = this.copyOfRange(0, this.size - length)