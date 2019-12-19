@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused", "NO_REFLECTION_IN_CLASS_PATH")

package net.mamoe.mirai.timpc.network.packet.action


/*
/**
 * 似乎没有必要. 服务器的返回永远都是 01 00 00 00 02 00 00
 */
@Deprecated("Useless packet")
@AnnotatedId(KnownPacketId.SUBMIT_IMAGE_FILE_NAME)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2 (21173)")
object SubmitImageFilenamePacket : PacketFactory {
    operator fun invoke(
        bot: Long,
        target: Long,
        filename: String,
        sessionKey: SessionKey
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer2)//?
        //writeHex("04 00 00 00 01 2E 01 00 00 69 35")

        encryptAndWrite(sessionKey) {
            writeByte(0x01)
            writeQQ(bot)
            writeQQ(target)
            writeZero(2)
            writeUByte(0x02u)
            writeRandom(1)
            writeHex("00 0A 00 01 00 01")
            val name = "UserDataImage:$filename"
            writeShort(name.length.toShort())
            writeStringUtf8(name)
            writeHex("00 00")
            writeRandom(2)//这个也与是哪个好友有关?
            writeHex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01")//35  02? 最后这个值是与是哪个好友有关

            //this.debugPrintThis("SubmitImageFilenamePacket")
        }

        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1A 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1B 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1C 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 29 37 42 53 4B 48 32 44 35 54 51 28 5A 35 7D 35 24 56 5D 32 35 49 4E 2E 6A 70 67 00 00 03 73 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
    }

    @PacketVersion(date = "2019.10.19", timVersion = "2.3.2 (21173)")
    class Response {
        override fun decode() = with(input) {
            require(readBytes().contentEquals(expecting))
        }

        companion object {
            private val expecting = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00)
        }
    }
}*/
// regiion GroupImageResponse
