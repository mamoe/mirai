package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.packet.login.*
import net.mamoe.mirai.network.packet.message.ServerSendFriendMessageResponsePacket
import net.mamoe.mirai.network.packet.message.ServerSendGroupMessageResponsePacket
import net.mamoe.mirai.utils.getAllDeclaredFields
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toUHexString
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
                        63 -> throw IllegalArgumentException(bytes.size.toString() + " (Unknown error)")//可能是已经完成登录, 服务器拒绝第二次登录
                        351 -> throw IllegalArgumentException(bytes.size.toString() + " (Illegal package data)")//包数据有误

                        else -> throw IllegalArgumentException(bytes.size.toString())
                    }, stream)
                }

                "08 28 04 34" -> ServerSessionKeyResponsePacketEncrypted(stream)


                else -> when (idHex.substring(0, 5)) {
                    "00 EC" -> ServerLoginSuccessPacket(stream)
                    "00 1D" -> ServerSKeyResponsePacketEncrypted(stream)
                    "00 5C" -> ServerAccountInfoResponsePacketEncrypted(stream)

                    "00 58" -> ServerHeartbeatResponsePacket(stream)

                    "00 BA" -> ServerVerificationCodePacketEncrypted(stream)


                    "00 CE", "00 17" -> ServerMessageEventPacketRawEncoded(stream, idHex.hexToBytes())

                    "00 81" -> UnknownServerPacket(stream)

                    "00 CD" -> ServerSendFriendMessageResponsePacket(stream)
                    "00 02" -> ServerSendGroupMessageResponsePacket(stream)

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

fun <N : Number> DataInputStream.readNBytesAt(position: N, length: Int): ByteArray {
    this.goto(position)
    return this.readNBytes(length)
}

fun <N : Number> DataInputStream.readNBytes(length: N): ByteArray {
    return this.readNBytes(length.toInt())
}

fun DataInputStream.readNBytesIn(range: IntRange): ByteArray {
    this.goto(range.first)
    return this.readNBytes(range.last - range.first + 1)
}

fun <N : Number> DataInputStream.readIntAt(position: N): Int {
    this.goto(position)
    return this.readInt();
}

fun <N : Number> DataInputStream.readByteAt(position: N): Byte {
    this.goto(position)
    return this.readByte();
}

fun <N : Number> DataInputStream.readShortAt(position: N): Short {
    this.goto(position)
    return this.readShort();
}