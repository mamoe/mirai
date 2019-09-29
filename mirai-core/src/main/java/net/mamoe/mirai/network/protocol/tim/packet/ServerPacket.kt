@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.network.ServerPacketReceivedEvent
import net.mamoe.mirai.event.hookWhile
import net.mamoe.mirai.network.protocol.tim.packet.PacketNameFormatter.adjustName
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerCanAddFriendResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerSendFriendMessageResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerSendGroupMessageResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.image.ServerTryGetImageIDResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.task.MiraiThreadPool
import net.mamoe.mirai.utils.*
import java.io.DataInputStream
import java.io.EOFException
import kotlin.reflect.KClass

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
                        135 -> LoginState.UNKNOWN//账号已经在另一台电脑登录??

                        63, 319, 351 -> LoginState.WRONG_PASSWORD//63不是密码错误, 应该是登录过频繁
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

                    "00 BA" -> ServerCatchaPacket.Encrypted(stream, idHex)


                    "00 CE", "00 17" -> ServerEventPacket.Raw.Encrypted(stream, idHex.hexToBytes())

                    "00 81" -> UnknownServerPacket(stream)

                    "00 CD" -> ServerSendFriendMessageResponsePacket(stream)
                    "00 02" -> ServerSendGroupMessageResponsePacket(stream)

                    "00 A7" -> ServerCanAddFriendResponsePacket(stream)

                    "03 88" -> ServerTryGetImageIDResponsePacket.Encrypted(stream)

                    else -> UnknownServerPacket(stream)
                }
            }.apply { this.idHex = idHex }
        }
    }


    override fun toString(): String {
        return adjustName(this.javaClass.simpleName + "(${this.getFixedId()})") + this.getAllDeclaredFields().filterNot { it.name == "idHex" || it.name == "idByteArray" || it.name == "encoded" }.joinToString(", ", "{", "}") {
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


    fun decryptBy(keyHex: String): DataInputStream {
        return this.decryptBy(keyHex.hexToBytes())
    }

    fun decryptBy(key1: ByteArray, key2: ByteArray): DataInputStream {
        return TEA.decrypt(this.decryptAsByteArray(key1), key2).dataInputStream()
    }


    fun decryptBy(key1: String, key2: ByteArray): DataInputStream {
        return this.decryptBy(key1.hexToBytes(), key2)
    }


    fun decryptBy(key1: ByteArray, key2: String): DataInputStream {
        return this.decryptBy(key1, key2.hexToBytes())
    }


    fun decryptBy(keyHex1: String, keyHex2: String): DataInputStream {
        return this.decryptBy(keyHex1.hexToBytes(), keyHex2.hexToBytes())
    }

    fun decryptAsByteArray(key: ByteArray): ByteArray {
        input.goto(14)
        return TEA.decrypt(input.readAllBytes().cutTail(1), key)
    }
}


fun DataInputStream.readIP(): String {
    var buff = ""
    for (i in 0..3) {
        val byte = readUnsignedByte()
        buff += byte.toString()
        if (i != 3) buff += "."
    }
    return buff
}

fun DataInputStream.readLVString(): String {
    return String(this.readLVByteArray())
}

fun DataInputStream.readLVByteArray(): ByteArray {
    return this.readNBytes(this.readShort().toInt())
}

fun DataInputStream.readTLVMap(expectingEOF: Boolean = false): Map<Int, ByteArray> {
    val map = mutableMapOf<Int, ByteArray>()
    var type: Int

    try {
        type = readUnsignedByte()
    } catch (e: EOFException) {
        if (expectingEOF) {
            return map
        }
        throw e
    }

    while (type != 0xff) {
        map[type] = this.readLVByteArray()

        try {
            type = readUnsignedByte()
        } catch (e: EOFException) {
            if (expectingEOF) {
                return map
            }
            throw e
        }
    }
    return map
}

fun Map<Int, ByteArray>.printTLVMap() {
    println(this.mapValues { (_, value) -> value.toUHexString() })
}


fun DataInputStream.readString(length: Number): String {
    return String(this.readNBytes(length))
}


fun ByteArray.dataInputStream(): DataInputStream = DataInputStream(this.inputStream())

/**
 * Reset and skip(position)
 */
infix fun <N : Number> DataInputStream.goto(position: N): DataInputStream {
    this.reset()
    this.skip(position.toLong())
    return this
}

fun <N : Number> DataInputStream.readNBytesAt(position: N, length: Int): ByteArray {
    this.goto(position)
    return this.readNBytes(length)
}

fun <N : Number> DataInputStream.readNBytes(length: N): ByteArray {
    return this.readNBytes(length.toInt())
}


fun DataInputStream.readLVNumber(): Number {
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
    return this.readInt()
}


fun <N : Number> DataInputStream.readUIntAt(position: N): UInt {
    this.goto(position)
    return this.readNBytes(4).toUInt()
}

fun DataInputStream.readUInt(): UInt {
    return this.readNBytes(4).toUInt()
}

fun <N : Number> DataInputStream.readByteAt(position: N): Byte {
    this.goto(position)
    return this.readByte()
}

fun <N : Number> DataInputStream.readShortAt(position: N): Short {
    this.goto(position)
    return this.readShort()
}


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
            }
            return this
        }
    } while (true)
}


@Suppress("UNCHECKED_CAST")
internal fun <P : ServerPacket> Bot.waitForPacket(packetClass: KClass<P>, timeoutMillis: Long = Long.MAX_VALUE, timeout: () -> Unit = {}) {
    var got = false
    ServerPacketReceivedEvent::class.hookWhile {
        if (packetClass.isInstance(it.packet) && it.bot === this) {
            got = true
            true
        } else {
            false
        }
    }


    MiraiThreadPool.getInstance().submit {
        val startingTime = System.currentTimeMillis()
        while (!got) {
            if (System.currentTimeMillis() - startingTime > timeoutMillis) {
                timeout.invoke()
                return@submit
            }
            Thread.sleep(10)
        }
    }
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

fun ByteArray.getRight(length: Int): ByteArray = this.copyOfRange(this.size - length, this.size)