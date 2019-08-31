package net.mamoe.mirai.util

import net.mamoe.mirai.network.packet.ClientPacket
import net.mamoe.mirai.network.packet.ServerPacket

/**
 * @author Him188moe
 */
object DebugLogger {
    val buff = StringBuilder()
}


fun ByteArray.encryptionDebugLogging() {
    DebugLogger.buff.append("TEA encrypt:   " + this.toUHexString()).append("\n")
}

fun ByteArray.packetSentDebugLogging() {
    DebugLogger.buff.append("packet sent:   " + this.toUHexString()).append("\n")
}

fun ByteArray.decryptionDebugLogging() {
    DebugLogger.buff.append("TEA decrypted: " + this.toUHexString()).append("\n")
}

fun ServerPacket.logging() {
    DebugLogger.buff.append(this.toString()).append("\n")

}

@ExperimentalUnsignedTypes
fun ClientPacket.logging() {
    DebugLogger.buff.append(this.toString()).append("\n")
}