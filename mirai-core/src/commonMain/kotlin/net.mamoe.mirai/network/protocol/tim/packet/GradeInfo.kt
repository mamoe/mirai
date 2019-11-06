@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeQQ

/**
 * 获取升级天数等.
 *
 * @author Him188moe
 */
@AnnotatedId(KnownPacketId.ACCOUNT_INFO)
object RequestAccountInfoPacket : SessionPacketFactory<RequestAccountInfoPacket.Response>() {
    operator fun invoke(
        qq: UInt,
        sessionKey: SessionKey
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(qq)
        writeHex(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeUByte(0x88u)
            writeQQ(qq)
            writeByte(0x00)
        }
    }

    object Response : Packet {
        //等级
        //升级剩余活跃天数
        //ignored
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): Response = Response
}