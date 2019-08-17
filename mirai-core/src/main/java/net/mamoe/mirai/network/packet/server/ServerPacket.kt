package net.mamoe.mirai.network.packet.server

import net.mamoe.mirai.network.packet.Packet
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

                else -> throw UnsupportedOperationException()
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
        if(i !=3)buff+="."
        println(byte.toHexString())
    }
    return buff
}
