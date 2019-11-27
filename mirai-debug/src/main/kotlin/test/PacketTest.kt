@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package test

import DebugNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.UnknownPacketId
import net.mamoe.mirai.network.protocol.tim.packet.action.GroupPacket.decode
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read

suspend fun main() {

    "00 00 00 03 01 41 00 04 01 00 23 04 40 1A F7 2F 11 02 00 00 00 00 00 00 00 00 00 21 00 C8 01 00 00 00 01 00 00 00 9E 1E 32 30 E7 A7 83 E9 A1 B6 32 38 E7 81 AB E8 91 AC 33 30 E9 87 8D E7 94 9F E5 BC 82 E4 B8 96 00 00 00 00 00 38 0D 3F 83 73 6A 9C F1 71 7C 10 DC CE F0 03 EA D9 FC 03 D5 87 E1 CD F5 F4 FA 63 37 26 07 17 9F 98 68 06 1A B6 4E 49 0A 14 DE B1 6B E1 32 99 30 29 AB 11 06 28 D9 0E 8A 3C 00 0A 00 00 00 00 06 00 03 00 02 00 01 00 04 00 04 00 00 00 01 00 05 00 04 5D BF EB 82 00 06 00 04 04 08 00 00 00 07 00 04 00 00 85 84 00 09 00 01 01 9F D6 4B F1 00 1A F7 2F 11 00 00 3E 03 3F A2 00 00 53 12 02 7F 00 00 76 E4 B8 DD 00 00 87 73 86 9D 00 00 87 CF 53 7B 00 00 8A 92 D8 1D 00 00 8C D9 1B 93 00 00 8E E0 1A 8A 00 01 9F D6 4B F1 00 00".hexToBytes()
        .read {
            decode(UnknownPacketId(0u), 0u, DebugNetworkHandler)
        }
}