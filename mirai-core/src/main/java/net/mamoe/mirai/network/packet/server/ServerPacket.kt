package net.mamoe.mirai.network.packet.server

import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.client.session.ServerAccountInfoResponsePacketEncrypted
import net.mamoe.mirai.network.packet.client.toHexString
import net.mamoe.mirai.network.packet.client.touch.ServerHeartbeatResponsePacket
import net.mamoe.mirai.network.packet.server.event.ServerMessageEventPacketRawEncoded
import net.mamoe.mirai.network.packet.server.login.*
import net.mamoe.mirai.network.packet.server.security.ServerLoginSuccessPacket
import net.mamoe.mirai.network.packet.server.security.ServerSKeyResponsePacketEncrypted
import net.mamoe.mirai.network.packet.server.security.ServerSessionKeyResponsePacketEncrypted
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacket
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacketEncrypted
import net.mamoe.mirai.util.getAllDeclaredFields
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.toUHexString
import java.io.DataInputStream

/**
 * @author Him188moe
 */
abstract class ServerPacket(val input: DataInputStream) : Packet {

    abstract fun decode()

    companion object {

        @ExperimentalUnsignedTypes
        fun ofByteArray(bytes: ByteArray): ServerPacket {
            //println("Raw received: ${bytes.toUByteArray().toUHexString()}")

            val stream = bytes.dataInputStream()

            stream.skip(3)


            return when (val idHex = stream.readInt().toHexString(" ")) {
                "08 25 31 01" -> ServerTouchResponsePacketEncrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_01, stream)

                "08 25 31 02" -> ServerTouchResponsePacketEncrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_02, stream)

                "08 36 31 03", "08 36 31 04", "08 36 31 05", "08 36 31 06" -> {
                    when (bytes.size) {
                        271, 207 -> return ServerLoginResponseResendPacketEncrypted(stream, when (idHex) {
                            "08 36 31 03" -> ServerLoginResponseResendPacket.Flag.`08 36 31 03`
                            else -> {
                                println("flag=$idHex"); ServerLoginResponseResendPacket.Flag.OTHER
                            }
                        })
                        871 -> return ServerLoginResponseVerificationCodePacketEncrypted(stream)
                    }

                    if (bytes.size > 700) {
                        return ServerLoginResponseSuccessPacketEncrypted(stream)
                    }

                    return ServerLoginResponseFailedPacket(when (bytes.size) {
                        319 -> ServerLoginResponseFailedPacket.State.WRONG_PASSWORD
                        135 -> ServerLoginResponseFailedPacket.State.RETYPE_PASSWORD
                        279 -> ServerLoginResponseFailedPacket.State.BLOCKED
                        263 -> ServerLoginResponseFailedPacket.State.UNKNOWN_QQ_NUMBER
                        551, 487 -> ServerLoginResponseFailedPacket.State.DEVICE_LOCK
                        359 -> ServerLoginResponseFailedPacket.State.TAKEN_BACK

                        //unknown
                        63 -> throw IllegalArgumentException(bytes.size.toString() + " (Already logged in)")//可能是已经完成登录, 服务器拒绝第二次登录
                        351 -> throw IllegalArgumentException(bytes.size.toString() + " (Illegal package data)")//包数据有误

                        else -> throw IllegalArgumentException(bytes.size.toString())
                    }, stream)
                }

                "08 28 04 34" -> ServerSessionKeyResponsePacketEncrypted(stream)


                "00 81 EC 78" -> UnknownPacket(stream)
                "00 81 AD 7A" -> UnknownPacket(stream)

                else -> when (idHex.substring(0, 5)) {
                    "00 EC" -> ServerLoginSuccessPacket(stream)
                    "00 1D" -> ServerSKeyResponsePacketEncrypted(stream)
                    "00 5C" -> ServerAccountInfoResponsePacketEncrypted(stream)

                    "00 58" -> ServerHeartbeatResponsePacket(stream)

                    "00 BA" -> ServerVerificationCodePacketEncrypted(stream)


                    "00 CE", "00 17" -> ServerMessageEventPacketRawEncoded(stream, idHex.hexToBytes())

                    else -> throw IllegalArgumentException(idHex)
                }
            }
        }
    }

    override fun toString(): String {
        return this.javaClass.simpleName + this.getAllDeclaredFields().joinToString(", ", "{", "}") {
            it.trySetAccessible(); it.name + "=" + it.get(this).let { value ->
            when (value) {
                is ByteArray -> value.toUHexString()
                is UByteArray -> value.toUHexString()
                else -> value?.toString()
            }
        }
        }
    }
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
        val byte = readUnsignedByte()
        buff += byte.toString()
        if (i != 3) buff += "."
    }
    return buff
}

fun DataInputStream.readShortVarString(): String {
    return String(this.readNBytes(this.readShort().toInt()))
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