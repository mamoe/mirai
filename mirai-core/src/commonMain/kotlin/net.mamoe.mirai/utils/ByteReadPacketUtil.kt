@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

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
fun ByteReadPacket.parseServerPacket(size: Int): ServerPacket {
    discardExact(3)

    val id = readUShort()
    val sequenceId = readUShort()

    discardExact(7)//4 for qq number, 3 for 0x00 0x00 0x00. 但更可能是应该 discard 8
    return when (id.toUInt()) {
        0x08_25u -> ServerTouchResponsePacket.Encrypted(this)
        0x08_36u -> {
            //todo 不要用size分析
            when (size) {
                271, 207 -> return ServerLoginResponseKeyExchangePacket.Encrypted(this).applySequence(sequenceId)
                871 -> return ServerLoginResponseCaptchaInitPacket.Encrypted(this).applySequence(sequenceId)
            }

            if (size > 700) return ServerLoginResponseSuccessPacket.Encrypted(this).applySequence(sequenceId)

            println("登录包size=$size")
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
                63 -> LoginResult.BLOCKED
                263 -> LoginResult.UNKNOWN_QQ_NUMBER
                279, 495, 551, 487 -> LoginResult.DEVICE_LOCK
                343, 359 -> LoginResult.TAKEN_BACK

                else -> LoginResult.UNKNOWN
                /*
                //unknown
                63 -> throw IllegalArgumentException(bytes.size.toString() + " (Unknown logError)")
                351 -> throw IllegalArgumentException(bytes.size.toString() + " (Unknown logError)")

                else -> throw IllegalArgumentException(bytes.size.toString())*/
            }, this).applySequence(sequenceId)
        }
        0x08_28u -> ServerSessionKeyResponsePacket.Encrypted(this)

        0x00_EC_u -> ServerLoginSuccessPacket(this)
        0x00_1D_u -> ServerSKeyResponsePacket.Encrypted(this)
        0x00_5C_u -> ServerAccountInfoResponsePacket.Encrypted(this)
        0x00_58_u -> ServerHeartbeatResponsePacket(this)
        0x00_BA_u -> ServerCaptchaPacket.Encrypted(this)
        0x00_CE_u, 0x00_17_u -> ServerEventPacket.Raw.Encrypted(this, id, sequenceId)
        0x00_81_u -> ServerFieldOnlineStatusChangedPacket.Encrypted(this)
        0x00_CD_u -> ServerSendFriendMessageResponsePacket(this)
        0x00_02_u -> ServerSendGroupMessageResponsePacket(this)
        0x00_A7_u -> ServerCanAddFriendResponsePacket(this)
        0x03_88_u -> ServerTryGetImageIDResponsePacket.Encrypted(this)

        else -> UnknownServerPacket.Encrypted(this, id, sequenceId)
    }.applySequence(sequenceId)
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
    @Suppress("DEPRECATION")
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