package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.utils.LoggerTextFormat
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.toUHexString


class UnknownServerPacket(input: ByteReadPacket) : ServerPacket(input) {
    override fun decode() {
        val raw = this.input.readBytes()
        MiraiLogger.logDebug("UnknownServerPacket data: " + raw.toUHexString())
    }


    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): UnknownServerPacket = UnknownServerPacket(this.decryptBy(sessionKey)).setId(this.idHex)
    }

    override fun toString(): String {
        return LoggerTextFormat.LIGHT_RED.toString() + super.toString()
    }
}

/*
ID: 00 17

长度 95
76 E4 B8 DD //1994701021
76 E4 B8 DD //1994701021
00 0B B9 A9 09 90 BB 54 1F //类似Event的uniqueId?
40 02
10 00 00 00
18 00
08 00
02 00
01 00
09 00
06 41 4B DA 4C 00 00
00 0A
00 04
01 00 00 00 00 00
00 06 00 00
00 0E
08 02
1A 02 08 49 0A 0C 08 A2 FF 8C F0
03 10 CA EB 8B ED 05

或者

长度63
00 00 27 10 76 E4 B8 DD
00 09 ED 26 64 73 0E CA 1F 40
00 12 00 00
00 08
00 0A
00 04
01 00 00
00 02

值都是一样的.
 */
