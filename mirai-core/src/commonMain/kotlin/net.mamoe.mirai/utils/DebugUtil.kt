package net.mamoe.mirai.utils

import kotlinx.io.core.Input
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes


internal object DebugLogger : MiraiLogger by PlatformLogger("Packet Debug")

internal fun debugPrintln(any: Any?) = DebugLogger.logPurple(any)

@Deprecated("Debugging Warning", ReplaceWith(""))
internal fun ByteArray.debugPrint(name: String): ByteArray {
    DebugLogger.logPurple(name + "=" + this.toUHexString())
    return this
}

@Deprecated("Debugging Warning", ReplaceWith(""))
internal fun IoBuffer.debugPrint(name: String): IoBuffer {
    val readBytes = this.readBytes()
    DebugLogger.logPurple(name + "=" + readBytes.toUHexString())
    return readBytes.toIoBuffer()
}

@Deprecated("Debugging Warning", ReplaceWith(""))
internal fun Input.debugDiscardExact(n: Number, name: String = "") {
    DebugLogger.logPurple("Discarded($n) $name=" + this.readBytes(n.toInt()).toUHexString())
}
