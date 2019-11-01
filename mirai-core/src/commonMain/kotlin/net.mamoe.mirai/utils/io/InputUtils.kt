@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.utils.io

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.KnownPacketId.*
import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.decryptBy


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
    return when (PacketId(id)) {
        TOUCH -> TouchResponsePacket.Encrypted(this)
        LOGIN ->
            //todo 不要用size分析
            when {
                size == 271 || size == 207 -> LoginResponseKeyExchangeResponsePacket.Encrypted(this)
                size == 871 -> LoginResponseCaptchaInitPacket.Encrypted(this)
                size > 700 -> LoginResponseSuccessPacket.Encrypted(this)

                else -> LoginResponseFailedPacket(
                    when (size) {
                        135 -> {//包数据错误. 目前怀疑是 tlv0006
                            this.readRemainingBytes().cutTail(1).decryptBy(TIMProtocol.shareKey).read {
                                discardExact(51)
                                MiraiLogger.error("Internal error: " + readLVString())//抱歉，请重新输入密码。
                            }

                            LoginResult.INTERNAL_ERROR
                        }

                        319, 351 -> LoginResult.WRONG_PASSWORD
                        //135 -> LoginState.RETYPE_PASSWORD
                        63 -> LoginResult.BLOCKED
                        263 -> LoginResult.UNKNOWN_QQ_NUMBER
                        279, 495, 551, 487 -> LoginResult.DEVICE_LOCK
                        343, 359 -> LoginResult.TAKEN_BACK

                        else -> LoginResult.UNKNOWN
                    }, this)
            }
        SESSION_KEY -> SessionKeyResponsePacket.Encrypted(this)

        CHANGE_ONLINE_STATUS -> ServerLoginSuccessPacket(this)
        CAPTCHA -> ServerCaptchaPacket.Encrypted(this)
        SERVER_EVENT_1, SERVER_EVENT_2 -> ServerEventPacket.Raw.Encrypted(this, PacketId(id), sequenceId)
        FRIEND_ONLINE_STATUS_CHANGE -> FriendOnlineStatusChangedPacket.Encrypted(this)

        S_KEY -> ResponsePacket.Encrypted<RequestSKeyPacket.Response>(this)
        ACCOUNT_INFO -> ResponsePacket.Encrypted<RequestAccountInfoPacket.Response>(this)
        SEND_GROUP_MESSAGE -> ResponsePacket.Encrypted<SendGroupMessagePacket.Response>(this)
        SEND_FRIEND_MESSAGE -> ResponsePacket.Encrypted<SendFriendMessagePacket.Response>(this)
        CAN_ADD_FRIEND -> ResponsePacket.Encrypted<CanAddFriendPacket.Response>(this)
        HEARTBEAT -> ResponsePacket.Encrypted<HeartbeatPacket.Response>(this)
        GROUP_IMAGE_ID -> ResponsePacket.Encrypted<GroupImageIdRequestPacket.Response>(this)
        FRIEND_IMAGE_ID -> ResponsePacket.Encrypted<FriendImageIdRequestPacket.Response>(this)
        // 0x01_BDu -> EventResponse.Encrypted<SubmitImageFilenamePacket.Response>(this)

        else -> UnknownServerPacket.Encrypted(this, PacketId(id), sequenceId)
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

fun Map<Int, ByteArray>.printTLVMap(name: String) =
    debugPrintln("TLVMap $name= " + this.mapValues { (_, value) -> value.toUHexString() })

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