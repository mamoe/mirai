package net.mamoe.mirai.network.packet.server

import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.server.login.ServerLoginFailedResponsePacket
import net.mamoe.mirai.network.packet.server.login.ServerLoginResendResponsePacket
import net.mamoe.mirai.network.packet.server.login.ServerLoginSucceedResponsePacket
import net.mamoe.mirai.network.packet.server.login.ServerLoginVerificationCodeResponsePacket
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacket
import net.mamoe.mirai.util.toHexString
import java.io.DataInputStream

/**
 * @author Him188moe @ Mirai Project
 */
abstract class ServerPacket(val input: DataInputStream) : Packet {

    abstract fun decode()

    companion object {

        fun ofByteArray(bytes: ByteArray): ServerPacket {

            val stream = DataInputStream(bytes.inputStream())

            stream.skipUntil(10)
            val idBytes = stream.readUntil(11)

            return when (idBytes.joinToString("") { it.toString(16) }) {
                "08 25 31 01" -> ServerTouchResponsePacket(ServerTouchResponsePacket.Type.TYPE_08_25_31_01, stream)
                "08 25 31 02" -> ServerTouchResponsePacket(ServerTouchResponsePacket.Type.TYPE_08_25_31_02, stream)
                "08 36 31 03", "08 36 31 04", "08 36 31 05", "08 36 31 06" -> {
                    when (bytes.size) {
                        271, 207 -> {
                            ServerLoginResendResponsePacket(stream)
                        }
                        871 -> return ServerLoginVerificationCodeResponsePacket(stream)
                    }

                    if (bytes.size > 700) {
                        return ServerLoginSucceedResponsePacket(stream)
                    }

                    return ServerLoginFailedResponsePacket(when (bytes.size) {
                        319 -> ServerLoginFailedResponsePacket.State.WRONG_PASSWORD
                        135 -> ServerLoginFailedResponsePacket.State.RETYPE_PASSWORD
                        279 -> ServerLoginFailedResponsePacket.State.BLOCKED
                        263 -> ServerLoginFailedResponsePacket.State.UNKNOWN_QQ_NUMBER
                        551, 487 -> ServerLoginFailedResponsePacket.State.DEVICE_LOCK
                        359 -> ServerLoginFailedResponsePacket.State.TAKEN_BACK
                        else -> throw IllegalStateException()
                    }, stream)
                }

                else -> throw IllegalStateException()
            }
        }
    }
}


fun DataInputStream.skipUntil(byte: Byte) {
    while (readByte() != byte);
}

fun DataInputStream.readUntil(byte: Byte): ByteArray {
    var buff = byteArrayOf()
    var b: Byte
    b = readByte()
    while (b != byte) {
        buff += b
        b = readByte()
    }
    return buff
}

@ExperimentalUnsignedTypes
fun DataInputStream.readIP(): String {
    var buff = ""
    for (i in 0..3) {
        val byte = readByte();
        buff += (byte.toUByte().toString())
        if (i != 3) buff += "."
        println(byte.toHexString())
    }
    return buff
}
