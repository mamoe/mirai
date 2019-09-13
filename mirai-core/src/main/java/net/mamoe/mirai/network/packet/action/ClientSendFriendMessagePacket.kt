package net.mamoe.mirai.network.packet.action

import net.mamoe.mirai.message.defaults.MessageChain
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
        private val botQQ: Long,
        private val targetQQ: Long,
        private val sessionKey: ByteArray,
        private val message: MessageChain
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(botQQ)
        this.writeHex(Protocol.fixVer2)

        this.encryptAndWrite(sessionKey) {
            it.writeQQ(botQQ)
            it.writeQQ(targetQQ)
            it.writeHex("00 00 00 08 00 01 00 04 00 00 00 00")
            it.writeHex("37 0F")
            it.writeQQ(botQQ)
            it.writeQQ(targetQQ)
            it.write(md5(lazyEncode { md5Key -> md5Key.writeQQ(targetQQ); md5Key.write(sessionKey) }))
            it.writeHex("00 0B")
            it.writeRandom(2)
            it.writeTime()
            it.writeHex("00 00 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00")
            it.writeTime()
            it.writeRandom(4)
            it.writeHex("00 00 00 00 09 00 86")
            it.writeHex(Protocol.friendMessageConst1)//... 85 E9 BB 91
            it.writeZero(2)


            it.write(message.toByteArray())

            /*
                //Plain text
                val bytes = message.toByteArray()
                it.writeByte(0x01)
                it.writeShort(bytes.size + 3)
                it.writeByte(0x01)
                it.writeShort(bytes.size)
                it.write(bytes)*/
        }
    }
}

@PacketId("00 CD")
class ServerSendFriendMessageResponsePacket(input: DataInputStream) : ServerPacket(input)