@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.*
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.message.toChain
import net.mamoe.mirai.message.toMessage
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.utils.*

fun main() {
    println("牛逼".toMessage().toChain().toPacket(true).readBytes().toUHexString())
}

@PacketId(0x00_CDu)
class ClientSendFriendMessagePacket(
        private val botQQ: UInt,
        private val targetQQ: UInt,
        private val sessionKey: ByteArray,
        private val message: MessageChain
) : ClientPacket() {


    @PacketVersion(date = "2019.10.19", timVersion = "2.3.2.21173")
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(botQQ)
        writeHex(TIMProtocol.versionNewest)

        encryptAndWrite(sessionKey) {
            // TIM最新, 消息内容 "牛逼"
            // 3E 03 3F A2
            // 76 E4 B8 DD
            // 00 00 00 [08] 00 01 00 04 00 00 00 00
            // 38 03
            // 3E 03 3F A2
            // 76 E4 B8 DD
            // C6 FB 06 30 0C 69 0C AD C6 AD 14 BF 0B C6 38 EA
            // 00 0B
            // 3D 7F
            // 5D AA A8 E2
            // 01 1D
            //  00 00 00 00
            // 01
            //  00
            //  00
            // 00 01 4D 53 47 00 00 00 00 00
            // 5D AA A8 E2
            // E2 AE 94 2D
            // 00 00 00 00 0C 00 86
            // 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91
            // 00 00
            //
            // 01 00 09 01 00 06 E7 89 9B E9 80 BC

            // TIM最新, 消息内容 "发图片群"
            // 3E 03 3F A2
            // 76 E4 B8 DD
            // 00 00 00 [0D] 00 01 00 04 00 00 00 00 00 03 00 01 01
            // 38 03
            // 3E 03 3F A2
            // 76 E4 B8 DD
            // C6 FB 06 30 0C 69 0C AD C6 AD 14 BF 0B C6 38 EA
            // 00 0B
            // 3D 88
            // 5D AA AE 33
            // 01 1D
            // 00 00 00 00
            // 01 00 00
            // 00 01 4D 53 47 00 00 00 00 00
            // 5D AA AE 33
            // 7E 51 1D AA
            // 00 00 00 00 0C 00 86
            // 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91
            // 00 00
            //
            // 01 00 0F 01 00 0C E5 8F 91 E5 9B BE E7 89 87 E7 BE A4


            writeQQ(botQQ)
            writeQQ(targetQQ)
            writeHex("00 00 00 08 00 01 00 04 00 00 00 00")
            writeHex("38 03")
            writeQQ(botQQ)
            writeQQ(targetQQ)
            writeFully(md5(buildPacket { writeQQ(targetQQ); writeFully(sessionKey) }.readBytes()))
            writeHex("00 0B")
            writeRandom(2)
            writeTime()
            writeHex("01 1D" +
                    " 00 00 00 00")

            //消息过多要分包发送
            //如果只有一个
            writeByte(0x01)
            writeByte(0)//第几个包
            writeUByte(0x00u)
            //如果大于一个,
            //writeByte(0x02)//数量
            //writeByte(0)//第几个包
            //writeByte(0x91)//why?

            writeHex("00 01 4D 53 47 00 00 00 00 00")
            writeTime()
            writeRandom(4)
            writeHex("00 00 00 00 0C 00 86")
            writeHex(TIMProtocol.messageConstNewest)
            writeZero(2)

            writePacket(message.toPacket(false))

            /*
                //Plain text
                val bytes = event.toPacket()
                it.writeByte(0x01)
                it.writeShort(bytes.size + 3)
                it.writeByte(0x01)
                it.writeShort(bytes.size)
                it.write(bytes)*/
        }
    }
}

@PacketId(0x00_CDu)
class ServerSendFriendMessageResponsePacket(input: ByteReadPacket) : ServerPacket(input)