package net.mamoe.mirai.network.packet.server

import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.client.toHexString
import net.mamoe.mirai.network.packet.server.login.*
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacket
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacketEncrypted
import net.mamoe.mirai.util.toHexString
import java.io.DataInputStream

/**
 * @author Him188moe
 */
abstract class ServerPacket(val input: DataInputStream) : Packet {

    abstract fun decode()

    companion object {

        @ExperimentalUnsignedTypes
        fun ofByteArray(bytes: ByteArray): ServerPacket {
            println("Raw received: $bytes")

            val stream = bytes.dataInputStream()

            stream.skip(3)

            return when (val idBytes = stream.readInt().toHexString(" ")) {
                "08 25 31 01" -> ServerTouchResponsePacketEncrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_01, stream)
                "08 25 31 02" -> ServerTouchResponsePacketEncrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_02, stream)
                "08 36 31 03", "08 36 31 04", "08 36 31 05", "08 36 31 06" -> {
                    when (bytes.size) {
                        271, 207 -> return ServerLoginResponseResendPacketEncrypted(stream, when (idBytes) {
                            "08 36 31 03" -> ServerLoginResponseResendPacket.Flag.`08 36 31 03`
                            else -> ServerLoginResponseResendPacket.Flag.OTHER
                        })
                        871 -> return ServerLoginResponseVerificationCodePacket(stream, bytes.size)
                    }

                    if (bytes.size > 700) {
                        return ServerLoginResponseSuccessPacketEncrypted(stream, bytes.size)
                    }

                    return ServerLoginResponseFailedPacket(when (bytes.size) {
                        319 -> ServerLoginResponseFailedPacket.State.WRONG_PASSWORD
                        135 -> ServerLoginResponseFailedPacket.State.RETYPE_PASSWORD
                        279 -> ServerLoginResponseFailedPacket.State.BLOCKED
                        263 -> ServerLoginResponseFailedPacket.State.UNKNOWN_QQ_NUMBER
                        551, 487 -> ServerLoginResponseFailedPacket.State.DEVICE_LOCK
                        359 -> ServerLoginResponseFailedPacket.State.TAKEN_BACK
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
        val byte = readByte()
        buff += (byte.toUByte().toString())
        if (i != 3) buff += "."
        println(byte.toHexString())
    }
    return buff
}

fun DataInputStream.readVarString(length: Int): String {
    return String(this.readNBytes(length))
}


fun ByteArray.dataInputStream(): DataInputStream = DataInputStream(this.inputStream())

/**
 * Reset and skip(position)
 */
infix fun <N : Number> DataInputStream.goto(position: N): DataInputStream {
    this.reset()
    this.skip(position.toLong());
    return this
}

fun <N : Number> DataInputStream.readNBytes(position: N, length: Int): ByteArray {
    this.goto(position)
    return this.readNBytes(length)
}

fun <N : Number> DataInputStream.readInt(position: N): Int {
    this.goto(position)
    return this.readInt();
}

fun <N : Number> DataInputStream.readByte(position: N): Byte {
    this.goto(position)
    return this.readByte();
}

fun <N : Number> DataInputStream.readShort(position: N): Short {
    this.goto(position)
    return this.readShort();
}