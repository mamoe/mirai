@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.ReceiveFriendAddRequestEvent
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.io.readQQ
import net.mamoe.mirai.utils.io.readUShortLVString


@PacketVersion(date = "2019.11.20", timVersion = "2.3.2 (21173)")
internal object FriendAddRequestEventPacket : KnownEventParserAndHandler<ReceiveFriendAddRequestEvent>(0x02DFu) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): ReceiveFriendAddRequestEvent = with(bot) {
        // 00 00 00 08 00 0A 00 04 01 00
        // 00 00 00 01
        // 76 E4 B8 DD
        // 00 00 00 01
        // 2D 5C 53 A6
        // 76 E4 B8 DD
        // 02 00 00
        // 00 0B BC 00 0B 5D D5 2E A3 04 7C 00 02 00 0C E6 88 91 E6 98 AF E6 A2 A8 E5 A4 B4 00 00
        //    有验证消息

        // 00 00 00 08 00 0A 00 04 01 00
        // 00 00 00 01
        // 76 E4 B8 DD
        // 00 00 00 01
        // 2D 5C 53 A6
        // 76 E4 B8 DD
        // 02 00 00
        // 09 0B BD 00 02 5D D5 32 50 04 7C 00 02 00 00 00 00
        //    无验证消息

        // 00 00 00 08 00 0A 00 04 01 00
        // 00 00 00 01
        // 76 E4 B8 DD
        // 00 00 00 01
        // 2D 5C 53 A6
        // 76 E4 B8 DD
        // 02 00 00
        // 09 0B BD 00 02 5D D5 33 0C 04 7C 00 02 00 0C E6 88 91 E6 98 AF E6 A2 A8 E5 A4 B4 00 00
        //    有验证消息

        /*

Mirai 20:35:23 : Packet received: UnknownEventPacket(id=02 10, identity=(761025446->1994701021))
= 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 4C 08 02 1A 02 08 23 0A 4A 08 DD F1 92 B7 07 10 A6 A7 F1 EA 02 18 02 20 00 28 01 30 09 38 BD 17 40 02 48 8C E6 D4 EE 05 52 0C E6 88 91 E6 98 AF E6 A2 A8 E5 A4 B4 5A 0F E6 9D A5 E8 87 AA E8 AE A8 E8 AE BA E7 BB 84 62 00 6A 06 08 A5 CE 85 8A 06 72 00
Mirai 20:35:23 : Packet received: UnknownEventPacket(id=02 DF, identity=(761025446->1994701021))
= 00 00 00 08 00 0A 00 04 01 00 00 00 00 01 76 E4 B8 DD 00 00 00 01 2D 5C 53 A6 76 E4 B8 DD 02 00 00 09 0B BD 00 02 5D D5 33 0C 04 7C 00 02 00 0C E6 88 91 E6 98 AF E6 A2 A8 E5 A4 B4 00 00
Mirai 20:35:23 : Packet received: UnknownEventPacket(id=00 BB, identity=(761025446->1994701021))
= 00 00 00 08 00 0A 00 04 01 00 00 00 01 0C E6 88 91 E6 98 AF E6 A2 A8 E5 A4 B4 01 0B BD 00 02 00 00 00 5E 00 00 00 00 00 00 00 00 01 04 03 EF 00 06 08 A5 CE 85 8A 06 03 F0 00 02 08 01 03 F2 00 14 00 00 00 82 00 00 00 6D 2F AF 0B ED 20 02 EB 94 00 00 00 00 03 ED 00 28 08 01 12 18 68 69 6D 31 38 38 E7 9A 84 E8 80 81 E5 85 AC E7 9A 84 E6 9B BF E8 BA AB 18 00 22 06 E6 A2 A8 E5 A4 B4 28 01

         */
        //Mirai 20:32:15 : Packet received: UnknownEventPacket(id=02 DF, identity=(761025446->1994701021))
        // = 00 00 00 08 00 0A 00 04 01 00 00 00 00 01 76 E4 B8 DD 00 00 00 01 2D 5C 53 A6 76 E4 B8 DD 02 00 00 09 0B BD 00 02 5D D5 32 50 04 7C 00 02 00 00 00 00
        //Mirai 20:32:15 : Packet received: UnknownEventPacket(id=02 10, identity=(761025446->1994701021))
        // = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 40 08 02 1A 02 08 23 0A 3E 08 DD F1 92 B7 07 10 A6 A7 F1 EA 02 18 02 20 00 28 01 30 09 38 BD 17 40 02 48 D0 E4 D4 EE 05 52 00 5A 0F E6 9D A5 E8 87 AA E8 AE A8 E8 AE BA E7 BB 84 62 00 6A 06 08 A5 CE 85 8A 06 72 00
        //Mirai 20:32:15 : Packet received: UnknownEventPacket(id=00 BB, identity=(761025446->1994701021))
        // = 00 00 00 08 00 0A 00 04 01 00 00 00 01 00 01 0B BD 00 02 00 00 00 5E 00 00 00 00 00 00 00 00 01 04 03 EF 00 06 08 A5 CE 85 8A 06 03 F0 00 02 08 01 03 F2 00 14 00 00 00 82 00 00 00 6D 2F AF 0B ED 20 02 EB 94 00 00 00 00 03 ED 00 28 08 01 12 18 68 69 6D 31 38 38 E7 9A 84 E8 80 81 E5 85 AC E7 9A 84 E6 9B BF E8 BA AB 18 00 22 06 E6 A2 A8 E5 A4 B4 28 01
        discardExact(10 + 4) // 00 00 00 08 00 0A 00 04 01 00  00 00 00 01
        discardExact(4) // bot account uint
        discardExact(4) // 00 00 00 01
        val qq = readQQ().qq()
        discardExact(4) // bot  account uint
        discardExact(3) // 02 00 00 恒定

        discardExact(11) // 不确定. 以下为可能的值
        // 00 00 01 00 01 5D D5 3C 57 00 A8  , 1994701021 添加 761025446
        // 09 0B BD 00 02 5D D5 33 0C 04 7C  有验证, 761025446 添加 1994701021
        // 09 0B BD 00 02 5D D5 32 50 04 7C  无验证, 761025446 添加 1994701021
        // 00 0B BC 00 0B 5D D5 2E A3 04 7C  有验证

        val message = readUShortLVString()
        discardExact(2) // 00 01

        return ReceiveFriendAddRequestEvent(qq, message)
    }
}

/*

1994701021 向 761025446 发出好友请求, 761025446 收到 0x02DF 事件, body=
00 00 00 08 00 0A 00 04 01 00
00 00 00 01
2D 5C 53 A6
00 00 00 01
76 E4 B8 DD
2D 5C 53 A6
02 00 00
00 00 01 00 01 5D D5 3C 57 00 A8 00 02 00 00 00 00

 */
