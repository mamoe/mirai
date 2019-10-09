package net.mamoe.mirai.network.protocol.tim.packet.action

import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.toByteArray
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.dataEncode
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream

/**
 * @author Him188moe
 */
@PacketId("00 02")
class ClientSendGroupMessagePacket(
        private val botQQ: Long,
        private val groupId: Long,//不是 number
        private val sessionKey: ByteArray,
        private val message: MessageChain
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(botQQ)
        this.writeHex(TIMProtocol.fixVer2)

        this.encryptAndWrite(sessionKey) {
            val bytes = message.toByteArray()
            writeByte(0x2A)
            writeGroup(groupId)

            writeLVByteArray(dataEncode { child ->
                child.writeHex("00 01 01")
                child.writeHex("00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00")

                child.writeTime()
                child.writeRandom(4)
                child.writeHex("00 00 00 00 09 00 86")
                child.writeHex(TIMProtocol.messageConst1)
                child.writeZero(2)

                //messages
                child.write(bytes)
            })
            /*it.writeByte(0x01)
            it.writeShort(bytes.size + 3)
            it.writeByte(0x01)
            it.writeShort(bytes.size)
            it.write(bytes)*/

            println(toByteArray().toUHexString())
        }
    }
}

@PacketId("00 02")
class ServerSendGroupMessageResponsePacket(input: DataInputStream) : ServerPacket(input)