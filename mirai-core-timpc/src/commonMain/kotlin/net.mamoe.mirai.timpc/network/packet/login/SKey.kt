@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "FunctionName")

package net.mamoe.mirai.timpc.network.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.writeFully
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.cryptor.encryptAndWrite
import net.mamoe.mirai.utils.io.*

internal inline class SKey(
    val value: String
) : Packet

/**
 * 请求 `SKey`
 * SKey 用于 http api
 */
internal object RequestSKeyPacket : SessionPacketFactory<SKey>() {
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): SKey {
        //11 00 97 D7 0F 1C FD 50 7A 41 DD 4D 66 93 EF 8C 85 D1 84 3D 66 95 9D E5 B4 96 A5 E3 92 37 28 D8 80 DA EF 8C 85 D1 84 3D 66 95 9D E5 B4 96 A5 E3 92 37 28 D8 80 DA

        discardExact(4)
        return SKey(readString(10)).also {
            DebugLogger.warning("SKey 包后面${readRemainingBytes().toUHexString()}")
        }
    }

    override suspend fun BotNetworkHandler.handlePacket(packet: SKey) {
        // _sKey = packet.value
        // _cookies = "uin=o$qqAccount;skey=$sKey;"

        // TODO: 2019/11/27 SKEY 实现
        /*
        if (sKeyRefresherJob?.isActive != true) {
            sKeyRefresherJob = NetworkScope.launch {
                while (isOpen) {
                    delay(1800000)
                    try {
                        requestSKey()
                    } catch (e: Throwable) {
                        bot.logger.error(e)
                    }
                }
            }
        }*/
    }
}