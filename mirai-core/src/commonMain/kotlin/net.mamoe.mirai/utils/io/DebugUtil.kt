package net.mamoe.mirai.utils.io

import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.withSwitch


object DebugLogger : MiraiLogger by DefaultLogger("Packet Debug").withSwitch()

fun Throwable.logStacktrace(message: String? = null) = DebugLogger.error(message, this)

fun debugPrintln(any: Any?) = DebugLogger.debug(any)

fun String.debugPrint(name: String): String {
    DebugLogger.debug("$name=$this")
    return this
}

fun ByteArray.debugPrint(name: String): ByteArray {
    DebugLogger.debug(name + "=" + this.toUHexString())
    return this
}

fun IoBuffer.debugPrint(name: String): IoBuffer {
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        DebugLogger.debug(name + "=" + it.toUHexString(offset = 0, length = count))
        return it.toIoBuffer(0, count)
    }
}

inline fun IoBuffer.debugCopyUse(block: IoBuffer.() -> Unit): IoBuffer {
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        block(it.toIoBuffer(0, count))
        return it.toIoBuffer(0, count)
    }
}

fun Input.debugDiscardExact(n: Number, name: String = "") {
    DebugLogger.debug("Discarded($n) $name=" + this.readBytes(n.toInt()).toUHexString())
}

fun ByteReadPacket.debugPrint(name: String = ""): ByteReadPacket {
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        DebugLogger.debug("ByteReadPacket $name=" + it.toUHexString(offset = 0, length = count))
        return it.toReadPacket(0, count)
    }
}

inline fun <R> Input.debugPrintIfFail(name: String = "", block: ByteReadPacket.() -> R): R {
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        try {
            return block(it.toReadPacket(0, count))
        } catch (e: Throwable) {
            DebugLogger.debug("Error in ByteReadPacket $name=" + it.toUHexString(offset = 0, length = count))
            throw e
        }
    }
}