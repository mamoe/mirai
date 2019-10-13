@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.*
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.*
import kotlin.properties.Delegates

data class EventPacketIdentity(
        val from: UInt,//对于好友消息, 这个是发送人
        val to: UInt,//对于好友消息, 这个是bot
        internal val uniqueId: IoBuffer//8
) {
    override fun toString(): String = "(from=$from, to=$to)"
}

fun BytePacketBuilder.writeEventPacketIdentity(identity: EventPacketIdentity) = with(identity) {
    writeUInt(from)
    writeUInt(to)
    writeFully(uniqueId)
}

/**
 * Packet id: `00 CE` or `00 17`
 *
 * @author Him188moe
 */
abstract class ServerEventPacket(input: ByteReadPacket, val eventIdentity: EventPacketIdentity) : ServerPacket(input) {
    class Raw(input: ByteReadPacket) : ServerPacket(input) {

        fun distribute(): ServerEventPacket = with(input) {
            val eventIdentity = EventPacketIdentity(
                    from = readUInt(),
                    to = readUInt(),
                    uniqueId = readIoBuffer(8)
            )
            readBytes(2).takeIf { it[0].toUInt() != 0x1Fu && it[1].toUInt() != 0x40u }?.debugPrint("type前面2个byte")
            val type = readBytes(2)
            return when (type.toUHexString()) {
                "00 C4" -> {
                    discardExact(13)
                    if (readBoolean()) {
                        ServerAndroidOfflineEventPacket(input, eventIdentity)
                    } else {
                        ServerAndroidOnlineEventPacket(input, eventIdentity)
                    }
                }
                "00 2D" -> ServerGroupUploadFileEventPacket(input, eventIdentity)

                "00 52" -> ServerGroupMessageEventPacket(input, eventIdentity)

                "00 A6" -> ServerFriendMessageEventPacket(input.debugPrint("好友消息事件"), eventIdentity)


                //00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 16 00 00 00 37 08 02 1A 12 08 95 02 10 90 04 40 98 E1 8C ED 05 48 AF 96 C3 A4 03 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 1A 29 08 00 10 05 18 98 E1 8C ED 05 20 01 28 FF FF FF FF 0F 32 15 E5 AF B9 E6 96 B9 E6 AD A3 E5 9C A8 E8 BE 93 E5 85 A5 2E 2E 2E
                "02 10" -> {
                    discardExact(19)
                    if (readUByte().toUInt() == 0x37u) ServerFriendTypingStartedPacket(input, eventIdentity)
                    else /*0x22*/ ServerFriendTypingCanceledPacket(input, eventIdentity)
                }

                //"02 10", "00 12" -> ServerUnknownEventPacket(input, eventIdentity)

                else -> {
                    MiraiLogger.logDebug("UnknownEvent type = ${type.toUHexString()}")
                    UnknownServerEventPacket(input, eventIdentity)
                }
            }.setId(idHex)
        }

        class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
            fun decrypt(sessionKey: ByteArray): Raw = Raw(this.decryptBy(sessionKey)).setId(this.idHex)
        }
    }

    inner class ResponsePacket(
            val bot: Long,
            val sessionKey: ByteArray
    ) : ClientPacket() {
        override val idHex: String = this@ServerEventPacket.idHex
        override val idByteArray: ByteArray = this@ServerEventPacket.idByteArray
        override val fixedId: String = idHex

        override fun encode(builder: BytePacketBuilder) = with(builder) {
            this.writeQQ(bot)
            this.writeHex(TIMProtocol.fixVer2)
            this.encryptAndWrite(sessionKey) {
                writeEventPacketIdentity(eventIdentity)
            }
        }

    }
}

/**
 * Unknown event
 */
class UnknownServerEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    override fun decode() {
        MiraiLogger.logDebug("UnknownServerEventPacket data: " + this.input.readBytes().toUHexString())
    }
}


sealed class ServerFriendTypingPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    val qq get() = eventIdentity.from

}

/**
 * 对方正在输入
 */
class ServerFriendTypingStartedPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerFriendTypingPacket(input, eventIdentity)

/**
 * 对方取消了输入
 */
class ServerFriendTypingCanceledPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerFriendTypingPacket(input, eventIdentity)



/**
 * Android 客户端上线
 */
class ServerAndroidOnlineEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity)

/**
 * Android 客户端下线
 */
class ServerAndroidOfflineEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity)


/**
 * 群文件上传
 */
class ServerGroupUploadFileEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    private lateinit var xmlMessage: String

    override fun decode() {
        this.input.discardExact(60)
        val size = this.input.readShort().toInt()
        this.input.discardExact(3)
        xmlMessage = this.input.readString(size)
    }//todo test
}

@Suppress("EXPERIMENTAL_API_USAGE")
class ServerGroupMessageEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    var groupNumber: UInt by Delegates.notNull()
    var qq: UInt by Delegates.notNull()
    lateinit var senderName: String
    lateinit var message: MessageChain

    override fun decode() = with(input) {
        discardExact(31)
        groupNumber = readUInt()
        discardExact(1)
        qq = readUInt()

        discardExact(48)
        readLVByteArray()
        discardExact(2)//2个0x00
        message = readMessageChain()

        val map = readTLVMap(true)
        map.printTLVMap("父map")
        if (map.containsKey(18)) {
            senderName = map.getValue(18).read {
                val tlv = readTLVMap(true)
                tlv.printTLVMap("子map")
                //群主的18: 05 00 04 00 00 00 03 08 00 04 00 00 00 04 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08

                when {
                    tlv.containsKey(0x01) -> String(tlv.getValue(0x01))
                    tlv.containsKey(0x02) -> String(tlv.getValue(0x02))
                    else -> "null"
                }
            }
        }
    }
}

//
//以前的消息: 00 00 00 25 00 08 00 02 00 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 76 E4 B8 DD 58 2C 60 86 35 3A 30 B3 C7 63 4A 80 E7 CD 5B 64 00 0B 78 16 5D A3 0A FD 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D A3 0A FD AB 77 16 02 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 04 01 00 01 36 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00
//刚刚的消息: 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 76 E4 B8 DD 11 F4 B2 F2 1A E7 1F C4 F1 3F 23 FB 74 80 42 64 00 0B 78 1A 5D A3 26 C1 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D A3 26 C1 AA 34 08 42 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E4 BD A0 E5 A5 BD 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

fun main() {
    println("08 02 1A 12 08 95 02 10 90 04 40 D6 DE 8C ED 05 48 CF B5 90 D6 02 08 DD F1 92 B7 07 10 DD F1 92 B7 07 1A 14 08 00 10 05 18 D6 DE 8C ED 05 20 02 28 FF FF FF FF 0F 32 00".hexToBytes().stringOf())
}

fun main2() {
    val data = "00 00 00 20 00 05 00 02 00 06 00 06 00 04 00 01 01 07 00 09 00 06 03 E9 20 02 EB 94 00 0A 00 04 01 00 00 00 0C 17 76 E4 B8 DD 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B A6 D2 5D A3 2A 3F 00 00 5D A3 2A 3F 01 00 00 00 00 4D 53 47 00 00 00 00 00 5D A3 2A 3F 0C 8A 59 3D 00 00 00 00 0A 00 86 02 00 06 E5 AE 8B E4 BD 93 00 00 01 00 06 01 00 03 31 32 33 19 00 1F 01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 0E 00 0E 01 00 04 00 00 00 00 0A 00 04 00 00 00 00".hexToBytes()
    val packet = ServerFriendMessageEventPacket(data.toReadPacket(), EventPacketIdentity(0u, 0u, IoBuffer.Empty))
    packet.decode()
    println(packet)
}

class ServerFriendMessageEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    val qq: UInt get() = eventIdentity.from

    /**
     * 是否是在这次登录之前的消息, 即消息记录
     */
    var isPrevious: Boolean = false

    lateinit var message: MessageChain

    //来自自己发送给自己
    //00 00 00 20 00 05 00 02 00 06 00 06 00 04 00 01 01 07 00 09 00 06 03 E9 20 02 EB 94 00 0A 00 04 01 00 00 00 0C 17 76 E4 B8 DD 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B A6 D2 5D A3 2A 3F 00 00 5D A3 2A 3F 01 00 00 00 00 4D 53 47 00 00 00 00 00 5D A3 2A 3F 0C 8A 59 3D 00 00 00 00 0A 00 86 02 00 06 E5 AE 8B E4 BD 93 00 00 01 00 06 01 00 03 31 32 33 19 00 1F 01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 0E 00 0E 01 00 04 00 00 00 00 0A 00 04 00 00 00 00

    override fun decode() = with(input) {
        input.discardExact(2)
        val l1 = readShort()
        discardExact(1)//0x00
        isPrevious = readByte().toInt() == 0x08
        discardExact(l1.toInt() - 2)
        discardExact(69)
        readLVByteArray()//font
        discardExact(2)//2个0x00
        message = readMessageChain()

        val map: Map<Int, ByteArray> = readTLVMap(true).withDefault { byteArrayOf() }
        map.printTLVMap("readTLVMap")
        //println("map.getValue(18)=" + map.getValue(18).toUHexString())

        //19 00 38 01 00 35 AA 02 32 50 03 60 00 68 00 9A 01 29 08 09 20 BF 02 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08 05 00 04 00 00 00 01 08 00 04 00 00 00 01

        /*
        val offset = unknownLength0 + fontLength//57
        event = MessageChain(PlainText(let {
            val length = input.readShortAt(101 + offset)//
            input.goto(103 + offset).readString(length.toInt())
        }))*/
    }
}

/*

牛逼   (10404
3E 03 3F A2 8F 00 1A E5 00 0B 53 3B 64 6B 91 17 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 8F 00 1A E5 3B DF D8 CE 2B 2E 96 D0 12 CF 0D 44 CF C9 22 A0 00 0B 32 40 5D 73 AF A1 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 73 AF A1 1F EE 24 55 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

牛逼   (10404
3E 03 3F A2 8F 00 1A E5 00 00 86 F3 09 18 83 47 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 8F 00 1A E5 3B DF D8 CE 2B 2E 96 D0 12 CF 0D 44 CF C9 22 A0 00 0B 32 41 5D 73 B3 21 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 73 B3 20 94 B0 82 BC 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

牛逼   (19947
76 E4 B8 DD 8F 00 1A E5 00 0D A2 8A 0A 65 7C D0 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 76 E4 B8 DD 8F 00 1A E5 4E 35 88 98 FE 64 7C E9 33 F7 2F B1 32 5D 5F A9 00 0B 77 FC 5D 73 B4 38 02 5B 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 73 B4 38 A6 60 C7 9A 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 0E 00 07 01 00 04 00 00 00 00

牛逼   (jiahua
B1 89 BE 09 8F 00 1A E5 00 0D EB CB 09 90 BA CF 1F 40 00 A6 00 00 00 20 00 05 00 02 00 01 00 06 00 04 00 01 05 0F 00 09 00 06 03 E9 20 02 E5 B3 00 0A 00 04 01 00 00 00 25 15 B1 89 BE 09 8F 00 1A E5 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 77 A3 5D 73 B4 7D 00 00 5D 73 B4 7D 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 73 B4 7D 0E A3 93 E3 00 00 00 00 09 00 86 00 00 09 48 65 6C 76 65 74 69 63 61 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 0E 01 00 04 00 00 00 00 0A 00 04 00 00 00 00 19 00 1C 01 00 19 AA 02 16 08 00 88 01 00 9A 01 0E 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00

牛逼   (jiahua
B1 89 BE 09 8F 00 1A E5 00 0B 03 A2 09 90 BB 7A 1F 40 00 A6 00 00 00 20 00 05 00 02 00 01 00 06 00 04 00 01 05 0F 00 09 00 06 03 E9 20 02 E5 B3 00 0A 00 04 01 00 00 00 25 15 B1 89 BE 09 8F 00 1A E5 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 77 A5 5D 73 B6 33 00 00 5D 73 B6 33 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 73 B6 33 22 DE A7 56 00 00 00 00 09 00 86 00 00 09 48 65 6C 76 65 74 69 63 61 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 0E 01 00 04 00 00 00 00 0A 00 04 00 00 00 00 19 00 1C 01 00 19 AA 02 16 08 00 88 01 00 9A 01 0E 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00

牛逼[EMOJI表情1]牛逼   (10404
3E 03 3F A2 8F 00 1A E5 00 0B 59 A3 64 6B 91 17 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 8F 00 1A E5 3B DF D8 CE 2B 2E 96 D0 12 CF 0D 44 CF C9 22 A0 00 0B 32 44 5D 73 BA D9 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 73 BA D9 12 C7 FC CD 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 02 00 14 01 00 01 0C 0B 00 08 00 01 00 04 52 CC F5 D0 FF 00 02 14 4D 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

牛逼[EMOJI表情2]牛逼   (10404
3E 03 3F A2 8F 00 1A E5 00 0D 4D 4A 09 18 83 47 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 8F 00 1A E5 3B DF D8 CE 2B 2E 96 D0 12 CF 0D 44 CF C9 22 A0 00 0B 32 45 5D 73 BF A0 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 73 BF A0 68 31 43 A2 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 02 00 14 01 00 01 AF 0B 00 08 00 01 00 04 52 CC F5 D0 FF 00 02 14 F0 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

牛逼[EMOJI表情1]牛逼   (10404
3E 03 3F A2 8F 00 1A E5 00 02 3C 8E 64 6B 91 17 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 8F 00 1A E5 3B DF D8 CE 2B 2E 96 D0 12 CF 0D 44 CF C9 22 A0 00 0B 32 47 5D 73 C3 76 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 73 C3 75 41 3B 97 72 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 0F 01 00 0C E7 89 9B E9 80 BC E7 89 9B E9 80 BC 02 00 14 01 00 01 0C 0B 00 08 00 01 00 04 52 CC F5 D0 FF 00 02 14 4D 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

[图片]  (10404
3E 03 3F A2 8F 00 1A E5 00 0E 02 CF 64 6B A0 0C 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 8F 00 1A E5 3B DF D8 CE 2B 2E 96 D0 12 CF 0D 44 CF C9 22 A0 00 0B 32 4B 5D 73 D1 1C 01 1D 00 00 00 00 01 00 00 00 0C 4D 53 47 00 00 00 00 00 5D 73 D1 1C F5 78 37 16 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 06 00 F0 02 00 1B 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 03 00 04 00 00 06 E2 04 00 25 2F 65 64 33 39 30 66 38 34 2D 34 66 38 37 2D 34 36 64 63 2D 62 33 38 35 2D 34 35 35 36 62 35 31 30 61 61 35 33 14 00 04 03 00 00 00 18 00 25 2F 65 64 33 39 30 66 38 34 2D 34 66 38 37 2D 34 36 64 63 2D 62 33 38 35 2D 34 35 35 36 62 35 31 30 61 61 35 33 19 00 04 00 00 00 38 1A 00 04 00 00 00 34 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 31 37 36 32 65 42 39 45 32 37 32 31 43 39 36 44 37 39 41 38 32 31 36 45 30 41 44 34 30 42 35 39 35 39 31 38 36 2E 6A 70 67 66 2F 65 64 33 39 30 66 38 34 2D 34 66 38 37 2D 34 36 64 63 2D 62 33 38 35 2D 34 35 35 36 62 35 31 30 61 61 35 33 41 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

 */


/*
3E 03 3F A2 76 E4 B8 DD 00 09 7C 3F 64 5C 2A 60 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 02 38 03 3E 03 3F A2 76 E4 B8 DD 01 10 9D D6 12 EA BC 07 91 EF DC 29 75 67 A9 1E 00 0B 2F E4 5D 6B A8 F6 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 6B A8 F6 08 7E 90 CE 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00
3E 03 3F A2 76 E4 B8 DD 00 03 5F 85 64 5C 2A A4 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 02 38 03 3E 03 3F A2 76 E4 B8 DD 01 10 9D D6 12 EA BC 07 91 EF DC 29 75 67 A9 1E 00 0B 2F E5 5D 6B A9 16 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 6B A9 17 1B B3 4D D7 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

 */

/*


backup

class ServerFriendMessageEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    var qq: Long = 0
    lateinit var event: String



    override fun dataDecode() {
        //start at Sep1.0:27
        qq = input.readIntAt(0)
        val msgLength = input.readShortAt(22)
        val fontLength = input.readShortAt(93+msgLength)
        val offset = msgLength+fontLength
        event = if(input.readByteAt(97+offset).toUHexString() == "02"){
            "[face" + input.goto(103+offset).readByteAt(1).toInt().toString() + ".gif]"
            //.gif
        }else {
            val offset2 = input.readShortAt(101 + offset)
            input.goto(103 + offset).readString(offset2.toInt())
        }
    }
}
 */