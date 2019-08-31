package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.toUHexString
import java.io.DataInputStream


/**
 * 告知服务器已经收到数据
 */
@PacketId("")//随后写入
@ExperimentalUnsignedTypes
open class ClientMessageResponsePacket(
        private val qq: Int,
        private val packetIdFromServer: ByteArray,
        private val sessionKey: ByteArray,
        private val eventIdentity: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.write(packetIdFromServer)
        this.writeQQ(qq)
        this.writeHex(Protocol._fixVer)
        this.encryptAndWrite(sessionKey) {
            it.write(eventIdentity)
        }
    }
}


/**
 * 群聊和好友消息分析
 */
@PacketId("00 17")
class ServerMessageEventPacketRaw(
        input: DataInputStream,
        private val dataLength: Int,
        private val packetId: ByteArray
) : ServerPacket(input) {
    lateinit var type: ByteArray;
    lateinit var eventIdentity: ByteArray;

    override fun decode() {
        eventIdentity = this.input.readNBytes(16)
        type = this.input.goto(18).readNBytes(2)
    }

    fun analyze(): ServerEventPacket = when (val typeHex = type.toUHexString()) {
        "00 C4" -> {
            if (this.input.goto(33).readBoolean()) {
                ServerAndroidOnlineEventPacket(this.input, packetId, eventIdentity)
            } else {
                ServerAndroidOfflineEventPacket(this.input, packetId, eventIdentity)
            }
        }
        "00 2D" -> ServerGroupUploadFileEventPacket(this.input, packetId, eventIdentity)

        "00 52" -> ServerGroupMessageEventPacket(this.input, packetId, eventIdentity)

        "00 A6" -> ServerFriendMessageEventPacket(this.input, packetId, eventIdentity)

        //"02 10", "00 12" -> ServerUnknownEventPacket(this.input, packetId, eventIdentity)

        else -> ServerUnknownEventPacket(this.input, packetId, eventIdentity)
    }
}

class ServerUnknownEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity)

@PacketId("00 17")
class ServerMessageEventPacketRawEncoded(input: DataInputStream, val packetId: ByteArray) : ServerPacket(input) {


    override fun decode() {

    }

    fun decrypt(sessionKey: ByteArray): ServerMessageEventPacketRaw {
        this.input goto 14
        val data = this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }
        return ServerMessageEventPacketRaw(TEACryptor.decrypt(data, sessionKey).dataInputStream(), data.size, packetId);
    }

}