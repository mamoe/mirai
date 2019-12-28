@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.action

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.timpc.network.packet.PacketId

import net.mamoe.mirai.timpc.network.packet.SessionPacketFactory


// 0001
// 已确认 查好友列表的列表

// send
// 20 01 00 00 00 00 01 00 00

// receive
// 20 00 01 03 00 00 00 15 01 01 03
// 00 0C 01 02 [09] E4 BF A1 E7 94 A8 E5 8D A1
// 00 0F 02 03 [0C] E8 BD AF E4 BB B6 E6 B3 A8 E5 86 8C
// 00 09 03 04 [06] E6 BA 90 E7 A0 81
// 00 00


internal inline class FriendListList(val delegate: List<FriendList>): Packet

internal object QueryFriendListListPacket : SessionPacketFactory<FriendList>() {
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): FriendList {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


// 0134
// 这里有好友也有群(为 internal id) 97208217
// 不太确定. 可能是查好友与群列表??
// send
// 00 00 01 5B 5D EB 58 AD 00 00 03 E8 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00

// receive
// 00 00 19 00 00 01 5D 5D EB 5D C6 00 00 00 00 01 00 00 00 19 00 54 E5 06 01 00 00 00 00 00 01 00 00 00 64 B2 1D 01 00 00 00 00 00 01 00 00 00 66 7C C5 01 00 00 00 00 00 01 00 00 01 60 31 EC 01 00 00 00 00 00 01 00 00 01 B3 E6 AC 01 00 00 00 00 00 01 00 00 02 45 16 DF 01 00 00 00 00 00 01 00 00 03 37 67 20 01 00 00 00 00 00 01 00 00 05 B0 F4 6F 01 00 00 00 00 00 01 00 00 0C D9 1F 45 01 00 00 00 00 00 01 00 00 0F 0D 35 E1 01 00 00 00 00 00 01 00 00 10 18 86 83 01 00 00 00 00 00 01 00 00 11 A9 8B F7 01 00 00 00 00 00 01 00 00 31 05 12 1C 01 00 00 00 00 00 01 00 00 37 99 77 D7 01 00 00 00 00 00 01 00 00 37 C8 4D C7 04 00 00 00 00 00 00 37 E9 68 46 01 00 00 00 00 00 01 00 00 37 E9 94 CF 01 00 00 00 00 00 01 00 00 3E 03 3F A2 01 00 00 00 00 00 01 00 00 50 BA 4A 8F 01 00 00 00 00 00 01 00 00 55 7A D6 86 01 00 00 00 00 00 01 00 00 6C 78 B1 E0 01 00 00 00 00 00 01 00 00 78 59 79 87 01 00 00 00 00 00 01 00 00 79 9B 1B 59 04 00 00 00 00 00 00 A6 81 A4 9D 01 00 00 00 00 00 01 00 00 A6 A0 EE EF 01 00 00 00 00 00 01 00 00 00 00


