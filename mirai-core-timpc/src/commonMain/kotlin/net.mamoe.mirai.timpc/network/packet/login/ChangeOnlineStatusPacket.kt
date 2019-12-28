@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.writeFully
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.cryptor.NoDecrypter
import net.mamoe.mirai.utils.cryptor.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeQQ

/**
 * 改变在线状态: "我在线上", "隐身" 等
 */
internal object ChangeOnlineStatusPacket : PacketFactory<ChangeOnlineStatusPacket.ChangeOnlineStatusResponse, NoDecrypter>(NoDecrypter) {
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey,
        loginStatus: OnlineStatus
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeHex("01 00")
            writeUByte(loginStatus.id.toUByte())
            writeHex("00 01 00 01 00 04 00 00 00 00")
        }
    }

    internal object ChangeOnlineStatusResponse : Packet {
        override fun toString(): String = this::class.simpleName!!
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): ChangeOnlineStatusResponse =
        ChangeOnlineStatusResponse
}