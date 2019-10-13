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
    override fun toString(): String = "EPI(from=$from, to=$to)"
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
abstract class ServerEventPacket(input: ByteReadPacket, packetId: ByteArray, val eventIdentity: EventPacketIdentity) : ServerPacket(input) {
    override val idByteArray: ByteArray = packetId
    override var idHex: String = packetId.toUHexString()

    class Raw(input: ByteReadPacket, private val packetId: ByteArray) : ServerPacket(input) {

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
                        ServerAndroidOnlineEventPacket(input, packetId, eventIdentity)
                    } else {
                        ServerAndroidOfflineEventPacket(input, packetId, eventIdentity)
                    }
                }
                "00 2D" -> ServerGroupUploadFileEventPacket(input, packetId, eventIdentity)

                "00 52" -> ServerGroupMessageEventPacket(input, packetId, eventIdentity)

                "00 A6" -> ServerFriendMessageEventPacket(input, packetId, eventIdentity)

                //"02 10", "00 12" -> ServerUnknownEventPacket(input, packetId, eventIdentity)

                else -> UnknownServerEventPacket(input, packetId, eventIdentity)
            }.setId(idHex)
        }

        class Encrypted(input: ByteReadPacket, private val packetId: ByteArray) : ServerPacket(input) {
            fun decrypt(sessionKey: ByteArray): Raw = Raw(this.decryptBy(sessionKey), packetId).setId(this.idHex)
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
class UnknownServerEventPacket(input: ByteReadPacket, packetId: ByteArray, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, packetId, eventIdentity) {
    override fun decode() {
        println("UnknownServerEventPacket data: " + this.input.readBytes().toUHexString())
    }
}

/**
 * Android 客户端上线
 */
class ServerAndroidOnlineEventPacket(input: ByteReadPacket, packetId: ByteArray, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, packetId, eventIdentity)

/**
 * Android 客户端下线
 */
class ServerAndroidOfflineEventPacket(input: ByteReadPacket, packetId: ByteArray, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, packetId, eventIdentity)

/**
 * 群文件上传
 */
class ServerGroupUploadFileEventPacket(input: ByteReadPacket, packetId: ByteArray, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, packetId, eventIdentity) {
    private lateinit var xmlMessage: String

    override fun decode() {
        this.input.discardExact(60)
        val size = this.input.readShort().toInt()
        this.input.discardExact(3)
        xmlMessage = this.input.readString(size)
    }//todo test
}

@Suppress("EXPERIMENTAL_API_USAGE")
class ServerGroupMessageEventPacket(input: ByteReadPacket, packetId: ByteArray, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, packetId, eventIdentity) {
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
        if (map.containsKey(0x18)) {
            senderName = map.getValue(0x18).read {
                val tlv = readTLVMap(true)
                tlv.printTLVMap("子map")

                when {
                    tlv.containsKey(0x01) -> String(tlv.getValue(0x01))
                    tlv.containsKey(0x02) -> String(tlv.getValue(0x02))
                    else -> "null"
                }
            }
        }
    }
}

//牛逼[图片]牛逼[图片] 22 96 29 7B B4 DF 94 AA 00 08 74 A4 09 18 8D CC 1F 40 00 52 00 00 00 1B 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00 01 01 22 96 29 7B 01 3E 03 3F A2 00 03 7F 64 5D 7B AC BD 00 00 F3 36 02 03 00 02 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 7B AC BD 12 73 DB A2 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 CB 02 00 2A 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 04 00 04 B4 52 77 F1 05 00 04 BC EB 03 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 00 41 16 00 04 00 00 00 34 18 00 04 00 00 03 73 FF 00 5C 15 36 20 39 32 6B 41 31 43 62 34 35 32 37 37 66 31 62 63 65 62 30 33 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 41 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 77 02 00 2A 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 04 00 04 B4 52 77 F1 05 00 04 BC EB 03 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 00 41 16 00 04 00 00 00 34 18 00 04 00 00 03 73 FF 00 08 15 37 20 20 38 41 41 41 0E 00 0E 01 00 04 00 00 00 09 07 00 04 00 00 00 01 19 00 35 01 00 32 AA 02 2F 50 03 60 00 68 00 9A 01 26 08 09 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 05 00 04 00 00 00 01 08 00 04 00 00 00 01 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08
//牛逼[图片]牛逼 22 96 29 7B B4 DF 94 AA 00 0B C1 0A 09 18 89 93 1F 40 00 52 00 00 00 1B 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00 01 01 22 96 29 7B 01 3E 03 3F A2 00 03 7E F5 5D 7B 97 E7 00 00 F3 32 01 8D 00 02 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 7B 97 E6 FA BE 7F DC 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 CF 02 00 2A 7B 39 44 32 44 45 39 31 41 2D 33 39 38 39 2D 39 35 35 43 2D 44 35 42 34 2D 37 46 41 32 37 38 39 37 38 36 30 39 7D 2E 6A 70 67 04 00 04 97 15 7F 03 05 00 04 79 5C B1 A3 06 00 04 00 00 00 50 07 00 01 41 08 00 00 09 00 01 01 0B 00 00 14 00 04 03 00 00 00 15 00 04 00 00 00 3C 16 00 04 00 00 00 40 18 00 04 00 00 03 CC FF 00 60 15 36 20 39 36 6B 45 31 41 39 37 31 35 37 66 30 33 37 39 35 63 62 31 61 33 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 39 44 32 44 45 39 31 41 2D 33 39 38 39 2D 39 35 35 43 2D 44 35 42 34 2D 37 46 41 32 37 38 39 37 38 36 30 39 7D 2E 6A 70 67 31 32 31 32 41 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 0E 01 00 04 00 00 00 09 07 00 04 00 00 00 01 19 00 35 01 00 32 AA 02 2F 50 03 60 00 68 00 9A 01 26 08 09 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 05 00 04 00 00 00 01 08 00 04 00 00 00 01 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08

class ServerFriendMessageEventPacket(input: ByteReadPacket, packetId: ByteArray, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, packetId, eventIdentity) {
    val group: UInt get() = eventIdentity.from
    val qq: UInt get() = eventIdentity.to

    lateinit var message: MessageChain

    //00 00 00 25 00 08 00 02 00 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 76 E4 B8 DD E7 86 74 F2 64 55 AD 9A EB 2F B9 DF F1 7F 8C 28 00 0B 78 14 5D A2 F5 CB 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D A2 F5 CA 9D 26 CB 5E 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E4 BD A0 E5 A5 BD 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

    override fun decode() = with(input) {
        input.discardExact(2)
        val l1 = readShort()
        discardExact(l1.toInt())
        discardExact(69)
        readLVByteArray()//font
        discardExact(2)//2个0x00
        message = readMessageChain()

        val map: Map<Int, ByteArray> = readTLVMap(true).withDefault { byteArrayOf() }
        println("map.getValue(18)=" + map.getValue(18))

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

class ServerFriendMessageEventPacket(input: ByteReadPacket, packetId: ByteArray, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, packetId, eventIdentity) {
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