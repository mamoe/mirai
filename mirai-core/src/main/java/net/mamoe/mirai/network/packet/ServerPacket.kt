package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.packet.PacketNameFormatter.adjustName
import net.mamoe.mirai.network.packet.action.ServerCanAddFriendResponsePacket
import net.mamoe.mirai.network.packet.action.ServerSendFriendMessageResponsePacket
import net.mamoe.mirai.network.packet.action.ServerSendGroupMessageResponsePacket
import net.mamoe.mirai.network.packet.login.*
import net.mamoe.mirai.utils.*
import java.io.DataInputStream
import java.io.EOFException

/**
 * @author Him188moe
 */
abstract class ServerPacket(val input: DataInputStream) : Packet {
    var idHex: String

    var idByteArray: ByteArray//fixed 4 size


    var encoded: Boolean = false

    init {
        idHex = try {
            val annotation = this.javaClass.getAnnotation(PacketId::class.java)
            annotation.value.trim()
        } catch (e: NullPointerException) {
            ""
        }

        idByteArray = if (idHex.isEmpty()) {
            byteArrayOf(0, 0, 0, 0)
        } else {
            idHex.hexToBytes()
        }
    }

    fun <P : ServerPacket> P.setId(idHex: String): P {
        this.idHex = idHex
        return this
    }

    open fun decode() {

    }

    companion object {

        @ExperimentalUnsignedTypes
        fun ofByteArray(bytes: ByteArray): ServerPacket {
            val stream = bytes.dataInputStream()

            stream.skip(3)

            val idHex = stream.readInt().toUHexString(" ")
            return when (idHex) {
                "08 25 31 01" -> ServerTouchResponsePacket.Encrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_01, stream)
                "08 25 31 02" -> ServerTouchResponsePacket.Encrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_02, stream)

                "08 36 31 03", "08 36 31 04", "08 36 31 05", "08 36 31 06" -> {
                    when (bytes.size) {
                        271, 207 -> return ServerLoginResponseKeyExchangePacket.Encrypted(stream, when (idHex) {
                            "08 36 31 03" -> ServerLoginResponseKeyExchangePacket.Flag.`08 36 31 03`
                            else -> ServerLoginResponseKeyExchangePacket.Flag.OTHER

                        }).apply { this.idHex = idHex }
                        871 -> return ServerLoginResponseVerificationCodeInitPacket.Encrypted(stream).apply { this.idHex = idHex }
                    }

                    if (bytes.size > 700) {
                        return ServerLoginResponseSuccessPacket.Encrypted(stream).apply { this.idHex = idHex }
                    }

                    println(bytes.size)
                    return ServerLoginResponseFailedPacket(when (bytes.size) {
                        63, 319, 135, 351 -> LoginState.WRONG_PASSWORD//这四个其中一个也是被冻结
                        //135 -> LoginState.RETYPE_PASSWORD
                        279 -> LoginState.BLOCKED
                        263 -> LoginState.UNKNOWN_QQ_NUMBER
                        551, 487 -> LoginState.DEVICE_LOCK
                        359 -> LoginState.TAKEN_BACK

                        else -> LoginState.UNKNOWN
                        /*
                        //unknown
                        63 -> throw IllegalArgumentException(bytes.size.toString() + " (Unknown error)")
                        351 -> throw IllegalArgumentException(bytes.size.toString() + " (Unknown error)")

                        else -> throw IllegalArgumentException(bytes.size.toString())*/
                    }, stream).apply { this.idHex = idHex }
                }

                "08 28 04 34" -> ServerSessionKeyResponsePacket.Encrypted(stream)


                else -> when (idHex.substring(0, 5)) {
                    "00 EC" -> ServerLoginSuccessPacket(stream)
                    "00 1D" -> ServerSKeyResponsePacket.Encrypted(stream)
                    "00 5C" -> ServerAccountInfoResponsePacket.Encrypted(stream)

                    "00 58" -> ServerHeartbeatResponsePacket(stream)

                    "00 BA" -> ServerVerificationCodePacket.Encrypted(stream, idHex)


                    "00 CE", "00 17" -> ServerEventPacket.Raw.Encrypted(stream, idHex.hexToBytes())

                    "00 81" -> UnknownServerPacket(stream)

                    "00 CD" -> ServerSendFriendMessageResponsePacket(stream)
                    "00 02" -> ServerSendGroupMessageResponsePacket(stream)

                    "00 A7" -> ServerCanAddFriendResponsePacket(stream)

                    else -> throw IllegalArgumentException(idHex)
                }
            }.apply { this.idHex = idHex }
        }
    }


    @ExperimentalUnsignedTypes
    override fun toString(): String {
        return adjustName(this.javaClass.simpleName + "(${this.getFixedId()})") + this.getAllDeclaredFields().filterNot { it.name == "idHex" || it.name == "encoded" }.joinToString(", ", "{", "}") {
            it.trySetAccessible(); it.name + "=" + it.get(this).let { value ->
            when (value) {
                is ByteArray -> value.toUHexString()
                is UByteArray -> value.toUHexString()
                else -> value?.toString()
            }
        }
        }
    }

    open fun getFixedId(): String = getFixedId(this.idHex)

    fun getFixedId(id: String): String = when (id.length) {
        0 -> "__ __ __ __"
        2 -> "$id __ __ __"
        5 -> "$id __ __"
        7 -> "$id __"
        else -> id
    }

    fun decryptBy(key: ByteArray): DataInputStream {
        return decryptAsByteArray(key).dataInputStream()
    }

    @ExperimentalUnsignedTypes
    fun decryptBy(keyHex: String): DataInputStream {
        return this.decryptBy(keyHex.hexToBytes())
    }

    fun decryptBy(key1: ByteArray, key2: ByteArray): DataInputStream {
        return TEA.decrypt(this.decryptAsByteArray(key1), key2).dataInputStream();
    }

    @ExperimentalUnsignedTypes
    fun decryptBy(key1: String, key2: ByteArray): DataInputStream {
        return this.decryptBy(key1.hexToBytes(), key2)
    }

    @ExperimentalUnsignedTypes
    fun decryptBy(key1: ByteArray, key2: String): DataInputStream {
        return this.decryptBy(key1, key2.hexToBytes())
    }

    @ExperimentalUnsignedTypes
    fun decryptBy(keyHex1: String, keyHex2: String): DataInputStream {
        return this.decryptBy(keyHex1.hexToBytes(), keyHex2.hexToBytes())
    }

    fun decryptAsByteArray(key: ByteArray): ByteArray {
        input.goto(14)
        return TEA.decrypt(input.readAllBytes().cutTail(1), key)
    }
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

fun DataInputStream.readVarString(): String {
    return String(this.readVarByteArray())
}

fun DataInputStream.readVarByteArray(): ByteArray {
    return this.readNBytes(this.readShort().toInt())
}

fun DataInputStream.readString(length: Int): String {
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


fun DataInputStream.readVarNumber(): Number {
    return when (this.readShort().toInt()) {
        1 -> this.readByte()
        2 -> this.readShort()
        4 -> this.readInt()
        8 -> this.readLong()
        else -> throw UnsupportedOperationException()
    }
}

fun DataInputStream.readNBytesIn(range: IntRange): ByteArray {
    this.goto(range.first)
    return this.readNBytes(range.last - range.first + 1)
}

fun <N : Number> DataInputStream.readIntAt(position: N): Int {
    this.goto(position)
    return this.readInt();
}

@ExperimentalUnsignedTypes
fun <N : Number> DataInputStream.readUIntAt(position: N): UInt {
    this.goto(position)
    return this.readNBytes(4).toUInt();
}

fun <N : Number> DataInputStream.readByteAt(position: N): Byte {
    this.goto(position)
    return this.readByte();
}

fun <N : Number> DataInputStream.readShortAt(position: N): Short {
    this.goto(position)
    return this.readShort();
}

@ExperimentalUnsignedTypes
@JvmSynthetic
fun DataInputStream.gotoWhere(matcher: UByteArray): DataInputStream {
    return this.gotoWhere(matcher.toByteArray())
}

/**
 * 去往下一个含这些连续字节的位置
 */
@Throws(EOFException::class)
fun DataInputStream.gotoWhere(matcher: ByteArray): DataInputStream {
    require(matcher.isNotEmpty())

    loop@
    do {
        val byte = this.readByte()
        if (byte == matcher[0]) {
            //todo mark here
            for (i in 1 until matcher.size) {
                val b = this.readByte()
                if (b != matcher[i]) {
                    continue@loop //todo goto mark
                }
                return this
            }
        }
    } while (true)
}

/*
@Throws(EOFException::class)
fun DataInputStream.gotoWhere(matcher: ByteArray) {
    require(matcher.isNotEmpty())
    do {
        val byte = this.readByte()
        if (byte == matcher[0]) {
            for (i in 1 until matcher.size){

            }
        }
    } while (true)
}*/

fun ByteArray.cutTail(length: Int): ByteArray = this.copyOfRange(0, this.size - length)