package net.mamoe.mirai.network.packet.message

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.util.lazyEncode

/**
 * @author Him188moe
 */
@PacketId("00 CD")
@ExperimentalUnsignedTypes
class ClientSendFriendMessagePacket(
        val qq: Int,
        val sessionKey: ByteArray,
        val message: String
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(qq)
        this.writeHex(Protocol._fixVer)

        this.encryptAndWrite(sessionKey) {
            it.writeQQ(qq)
            it.writeQQ(qq)//that's correct
            it.writeHex("00 00 00 08 00 01 00 04 00 00 00 00")
            it.writeHex("37 0F")
            it.writeQQ(qq)
            it.write(md5(lazyEncode { md5Key -> md5Key.writeQQ(qq); md5Key.write(sessionKey) }))
            it.writeHex("00 0B")
            it.writeRandom(2)
            it.writeInt(System.currentTimeMillis().toInt())
            it.writeHex("00 00 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00")
            it.writeInt(System.currentTimeMillis().toInt())
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
                this.writeByte(0x01)
                this.writeInt(message.length + 3)
                this.writeByte(0x01)
                this.writeInt(message.length)
                this.write(message.toByteArray())
            }
        }
    }
}