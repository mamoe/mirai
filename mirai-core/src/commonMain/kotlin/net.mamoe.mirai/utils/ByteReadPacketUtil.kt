@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerCanAddFriendResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerSendFriendMessageResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerSendGroupMessageResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.login.*


fun ByteReadPacket.readRemainingBytes(
        n: Int = remaining.toInt()//not that safe but adequate
): ByteArray = ByteArray(n).also { readAvailable(it, 0, n) }

fun ByteReadPacket.readIoBuffer(
        n: Int = remaining.toInt()//not that safe but adequate
): IoBuffer = IoBuffer.Pool.borrow().also { this.readFully(it, n) }

fun ByteReadPacket.readIoBuffer(n: Number) = this.readIoBuffer(n.toInt())

//必须消耗完 packet
fun ByteReadPacket.parseServerPacket(size: Int): ServerPacket {//TODO 优化
    discardExact(3)

    val idHex = readInt().toUHexString(" ")

    discardExact(7)//4 for qq number, 3 for 0x00 0x00 0x00. 但更可能是应该 discard 8
    return when (idHex) {
        "08 25 31 01" -> ServerTouchResponsePacket.Encrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_01, this)
        "08 25 31 02" -> ServerTouchResponsePacket.Encrypted(ServerTouchResponsePacket.Type.TYPE_08_25_31_02, this)

        "08 36 31 03", "08 36 31 04", "08 36 31 05", "08 36 31 06" -> {
            when (size) {
                271, 207 -> return ServerLoginResponseKeyExchangePacket.Encrypted(this, when (idHex) {
                    "08 36 31 03" -> ServerLoginResponseKeyExchangePacket.Flag.`08 36 31 03`
                    else -> ServerLoginResponseKeyExchangePacket.Flag.OTHER
                }).setId(idHex)
                871 -> return ServerLoginResponseVerificationCodeInitPacket.Encrypted(this).setId(idHex)
            }

            if (size > 700) {
                return ServerLoginResponseSuccessPacket.Encrypted(this).setId(idHex)
            }

            println(size)
            return ServerLoginResponseFailedPacket(when (size) {
                135 -> {//包数据错误. 目前怀疑是 tlv0006
                    this.readRemainingBytes().cutTail(1).decryptBy(TIMProtocol.shareKey).read {
                        discardExact(51)
                        MiraiLogger.logError("Internal logError: " + readLVString())//抱歉，请重新输入密码。
                    }

                    LoginResult.INTERNAL_ERROR
                } //可能是包数据错了. 账号没有被ban, 用TIM官方可以登录

                319, 351 -> LoginResult.WRONG_PASSWORD
                //135 -> LoginState.RETYPE_PASSWORD
                63, 279 -> LoginResult.BLOCKED
                263 -> LoginResult.UNKNOWN_QQ_NUMBER
                551, 487 -> LoginResult.DEVICE_LOCK
                343, 359 -> LoginResult.TAKEN_BACK

                else -> LoginResult.UNKNOWN
                /*
                //unknown
                63 -> throw IllegalArgumentException(bytes.size.toString() + " (Unknown logError)")
                351 -> throw IllegalArgumentException(bytes.size.toString() + " (Unknown logError)")

                else -> throw IllegalArgumentException(bytes.size.toString())*/
            }, this).setId(idHex)
        }

        "08 28 04 34" -> ServerSessionKeyResponsePacket.Encrypted(this)


        else -> when (idHex.substring(0, 5)) {
            "00 EC" -> ServerLoginSuccessPacket(this)
            "00 1D" -> ServerSKeyResponsePacket.Encrypted(this)
            "00 5C" -> ServerAccountInfoResponsePacket.Encrypted(this)

            "00 58" -> ServerHeartbeatResponsePacket(this)

            "00 BA" -> ServerCaptchaPacket.Encrypted(this, idHex)


            "00 CE", "00 17" -> ServerEventPacket.Raw.Encrypted(this)

            "00 81" -> ServerFieldOnlineStatusChangedPacket.Encrypted(this)

            "00 CD" -> ServerSendFriendMessageResponsePacket(this)
            "00 02" -> ServerSendGroupMessageResponsePacket(this)

            "00 A7" -> ServerCanAddFriendResponsePacket(this)

            "03 88" -> ServerTryGetImageIDResponsePacket.Encrypted(this)

            else -> UnknownServerPacket.Encrypted(this)
        }
    }.setId(idHex)
}

fun Input.readIP(): String = buildString(4 + 3) {
    repeat(4) {
        val byte = readUByte()
        this.append(byte.toString())
        if (it != 3) this.append(".")
    }
}

fun Input.readLVString(): String = String(this.readLVByteArray())

fun Input.readLVByteArray(): ByteArray = this.readBytes(this.readShort().toInt())

fun Input.readTLVMap(expectingEOF: Boolean = false): Map<Int, ByteArray> {
    val map = mutableMapOf<Int, ByteArray>()
    var type: UByte

    try {
        type = readUByte()
    } catch (e: EOFException) {
        if (expectingEOF) {
            return map
        }
        throw e
    }

    while (type != UByte.MAX_VALUE) {
        map[type.toInt()] = this.readLVByteArray()

        try {
            type = readUByte()
        } catch (e: EOFException) {
            if (expectingEOF) {
                return map
            }
            throw e
        }
    }
    return map
}

fun Map<Int, ByteArray>.printTLVMap(name: String) = debugPrintln("TLVMap $name= " + this.mapValues { (_, value) -> value.toUHexString() })

fun Input.readString(length: Number): String = String(this.readBytes(length.toInt()))

private const val TRUE_BYTE_VALUE: Byte = 1
fun Input.readBoolean(): Boolean = this.readByte() == TRUE_BYTE_VALUE
fun Input.readLVNumber(): Number {
    return when (this.readShort().toInt()) {
        1 -> this.readByte()
        2 -> this.readShort()
        4 -> this.readInt()
        8 -> this.readLong()
        else -> throw UnsupportedOperationException()
    }
}

//添加@JvmSynthetic 导致 idea 无法检查这个文件的错误
//@JvmSynthetic
@Deprecated("Low efficiency", ReplaceWith(""))
fun <I : Input> I.gotoWhere(matcher: UByteArray): I {
    return this.gotoWhere(matcher.toByteArray())
}

/**
 * 去往下一个含这些连续字节的位置
 */
@Deprecated("Low efficiency", ReplaceWith(""))
fun <I : Input> I.gotoWhere(matcher: ByteArray): I {
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