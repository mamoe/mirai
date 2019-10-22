package net.mamoe.mirai.utils.io

import kotlinx.io.core.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toIoBuffer


internal object DebugLogger : MiraiLogger by PlatformLogger("Packet Debug")

internal fun debugPrintln(any: Any?) = DebugLogger.logPurple(any)

internal fun ByteArray.debugPrint(name: String): ByteArray {
    DebugLogger.logPurple(name + "=" + this.toUHexString())
    return this
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith("this"))
internal fun IoBuffer.debugPrint(name: String): IoBuffer {
    val readBytes = this.readBytes()
    DebugLogger.logPurple(name + "=" + readBytes.toUHexString())
    return readBytes.toIoBuffer()
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith("discardExact(n)"))
internal fun Input.debugDiscardExact(n: Number, name: String = "") {
    DebugLogger.logPurple("Discarded($n) $name=" + this.readBytes(n.toInt()).toUHexString())
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith("this"))
internal fun ByteReadPacket.debugPrint(name: String = ""): ByteReadPacket {
    val bytes = this.readBytes()
    DebugLogger.logPurple("ByteReadPacket $name=" + bytes.toUHexString())
    return bytes.toReadPacket()
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith("this"))
internal fun ByteReadPacket.debugColorizedPrint(name: String = "", ignoreUntilFirstConst: Boolean = false): ByteReadPacket {
    val bytes = this.readBytes()
    bytes.printColorizedHex(name, ignoreUntilFirstConst)
    return bytes.toReadPacket()
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith(" "))
internal fun BytePacketBuilder.debugColorizedPrintThis(name: String = "") {
    val data = this.build().readBytes()
    data.printColorizedHex(name)
    this.writeFully(data)
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith(" "))
internal fun BytePacketBuilder.debugColorizedPrintThis(name: String = "", compareTo: String? = null) {
    val data = this.build().readBytes()
    data.printColorizedHex(name, compareTo = compareTo)
    this.writeFully(data)
}

@Deprecated("Low efficiency, only for debug purpose", ReplaceWith(" "))
internal fun BytePacketBuilder.debugPrintThis(name: String = "") {
    val data = this.build().readBytes()
    data.debugPrint(name)
    this.writeFully(data)
}

internal fun String.printStringFromHex() {
    println(this.hexToBytes().stringOfWitch())
}


internal fun ByteArray.printColorizedHex(name: String = "", ignoreUntilFirstConst: Boolean = false, compareTo: String? = null) {
    println("Hex比较 `$name`")
    if (compareTo != null) {
        println(printCompareHex(toUHexString(), compareTo))
    } else {
        println(toUHexString().printColorize(ignoreUntilFirstConst))
    }
    println()
}

expect fun printCompareHex(hex1s: String, hex2s: String): String
expect fun String.printColorize(ignoreUntilFirstConst: Boolean = false): String


fun main() {
    "00 02 3E 03 3F A2 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 39 00 00 00 0B 00 00 00 2E 51 51 E7 A9 BA E9 97 B4 20 0A 20 20 E6 9C 89 E6 96 B0 E8 AE BF E5 AE A2 20 0A 20 20 E6 9C 89 E6 96 B0 E5 A5 BD E5 8F 8B E5 8A A8 E6 80 81 00 00 01 2C 00 00 00 00"
            .printStringFromHex()
}