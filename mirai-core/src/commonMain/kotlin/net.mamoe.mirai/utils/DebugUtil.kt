package net.mamoe.mirai.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes


internal object DebugLogger : MiraiLogger by PlatformLogger("Packet Debug")

internal fun debugPrintln(any: Any?) = DebugLogger.logPurple(any)

internal fun ByteArray.debugPrint(name: String): ByteArray {
    DebugLogger.logPurple(name + "=" + this.toUHexString())
    return this
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith(""))
internal fun IoBuffer.debugPrint(name: String): IoBuffer {
    val readBytes = this.readBytes()
    DebugLogger.logPurple(name + "=" + readBytes.toUHexString())
    return readBytes.toIoBuffer()
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith("discardExact(n)"))
internal fun Input.debugDiscardExact(n: Number, name: String = "") {
    DebugLogger.logPurple("Discarded($n) $name=" + this.readBytes(n.toInt()).toUHexString())
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith(""))
internal fun ByteReadPacket.debugPrint(name: String = ""): ByteReadPacket {
    val bytes = this.readBytes()
    DebugLogger.logPurple("ByteReadPacket $name=" + bytes.toUHexString())
    return bytes.toReadPacket()
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith(""))
internal fun ByteReadPacket.debugColorizedPrint(name: String = "", ignoreUntilFirstConst: Boolean = false): ByteReadPacket {
    val bytes = this.readBytes()
    bytes.printColorizedHex(name, ignoreUntilFirstConst)
    return bytes.toReadPacket()
}


internal fun String.printStringFromHex() {
    println(this.hexToBytes().stringOfWitch())
}


internal fun ByteArray.printColorizedHex(name: String = "", ignoreUntilFirstConst: Boolean = false) {
    println("Hex比较 `$name`")
    println(toUHexString().colorize(ignoreUntilFirstConst))
    println()
}

expect fun compareHex(hex1s: String, hex2s: String): String
expect fun String.colorize(ignoreUntilFirstConst: Boolean = false): String