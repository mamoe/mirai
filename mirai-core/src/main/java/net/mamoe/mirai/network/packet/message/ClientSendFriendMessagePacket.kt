package net.mamoe.mirai.network.packet.message

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.lazyEncode
import java.io.DataInputStream

/**
 * @author Him188moe
 */
@PacketId("00 CD")
@ExperimentalUnsignedTypes
class ClientSendFriendMessagePacket(
        private val robotQQ: Long,
        private val targetQQ: Long,
        private val sessionKey: ByteArray,
        private val message: String
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(robotQQ)
        this.writeHex(Protocol._fixVer)

        this.encryptAndWrite(sessionKey) {
            it.writeQQ(robotQQ)
            it.writeQQ(targetQQ)
            it.writeHex("00 00 00 08 00 01 00 04 00 00 00 00")
            it.writeHex("37 0F")
            it.writeQQ(robotQQ)
            it.writeQQ(targetQQ)
            it.write(md5(lazyEncode { md5Key -> md5Key.writeQQ(targetQQ); md5Key.write(sessionKey) }))
            it.writeHex("00 0B")
            it.writeRandom(2)
            it.writeTime()
            it.writeHex("00 00 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00")
            it.writeTime()
            it.writeRandom(4)
            it.writeHex("00 00 00 00 09 00 86 00 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91")
            it.writeZero(2)

            if ("[face" in message
                    || ".gif]" in message
                    || ".jpg]" in message
                    || ".png]" in message
            ) {
                TODO("复合消息构建")
            } else {
                //Plain text
                val bytes = message.toByteArray()
                it.writeByte(0x01)
                it.writeShort(bytes.size + 3)
                it.writeByte(0x01)
                it.writeShort(bytes.size)
                it.write(bytes)
            }
        }
    }
}

@PacketId("00 CD")
class ServerSendFriendMessageResponsePacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {
    }
}