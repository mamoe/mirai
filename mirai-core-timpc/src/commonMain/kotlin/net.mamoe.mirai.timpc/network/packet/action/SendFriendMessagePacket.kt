@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.action

import kotlinx.io.core.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.utils.NoLog
import net.mamoe.mirai.utils.PacketVersion

import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.md5

@PacketVersion(date = "2019.10.19", timVersion = "2.3.2 (21173)")
internal object SendFriendMessagePacket : SessionPacketFactory<SendFriendMessagePacket.Response>() {
    operator fun invoke(
        botQQ: Long,
        targetQQ: Long,
        sessionKey: SessionKey,
        message: MessageChain
    ): OutgoingPacket = buildSessionPacket(botQQ, sessionKey) {
        writeQQ(botQQ)
        writeQQ(targetQQ)
        writeHex("00 00 00 08 00 01 00 04 00 00 00 00")
        writeHex("38 03")
        writeQQ(botQQ)
        writeQQ(targetQQ)
        writeFully(md5(buildPacket { writeQQ(targetQQ); writeFully(sessionKey.value) }.readBytes()))
        writeHex("00 0B")
        writeRandom(2)
        writeTime()
        writeHex("01 1D 00 00 00 00")

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
        writeFully(TIMProtocol.messageConstNewest)
        writeZero(2)

        writePacket(message.toPacket())

        /*
            //Plain text
            val bytes = event.toPacket()
            it.writeByte(0x01)
            it.writeShort(bytes.size + 3)
            it.writeByte(0x01)
            it.writeShort(bytes.size)
            it.write(bytes)*/
    }

    @NoLog
    internal object Response : Packet {
        override fun toString(): String = "SendFriendMessagePacket.Response"
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): Response = Response
}