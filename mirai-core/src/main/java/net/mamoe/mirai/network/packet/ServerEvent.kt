@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.packet

import net.mamoe.mirai.message.FaceID
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.Face
import net.mamoe.mirai.message.defaults.Image
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.message.defaults.PlainText
import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toUHexString
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.util.zip.GZIPInputStream

/**
 * Packet id: `00 CE` or `00 17`
 *
 * @author Him188moe
 */
abstract class ServerEventPacket(input: DataInputStream, val packetId: ByteArray, val eventIdentity: ByteArray) : ServerPacket(input) {
    @PacketId("00 17")
    class Raw(input: DataInputStream, private val packetId: ByteArray) : ServerPacket(input) {

        fun distribute(): ServerEventPacket {
            val eventIdentity = this.input.readNBytes(16)
            val type = this.input.goto(18).readNBytes(2)

            return when (type.toUHexString()) {
                "00 C4" -> {
                    if (this.input.goto(33).readBoolean()) {
                        ServerAndroidOnlineEventPacket(this.input, packetId, eventIdentity)
                    } else {
                        ServerAndroidOfflineEventPacket(this.input, packetId, eventIdentity)
                    }
                }
                "00 2D" -> ServerGroupUploadFileEventPacket(this.input, packetId, eventIdentity)

                "00 52" -> ServerGroupMessageEventPacket(this.input, packetId, eventIdentity)

                "00 A6" -> ServerFriendMessageEventPacket(this.input, packetId, eventIdentity)

                //"02 10", "00 12" -> ServerUnknownEventPacket(this.input, packetId, eventIdentity)

                else -> UnknownServerEventPacket(this.input, packetId, eventIdentity)
            }.setId(this.idHex)
        }

        @PacketId("00 17")
        class Encrypted(input: DataInputStream, private val packetId: ByteArray) : ServerPacket(input) {
            fun decrypt(sessionKey: ByteArray): Raw = Raw(decryptBy(sessionKey), packetId).setId(this.idHex)
        }
    }
}

/**
 * Unknown event
 */
class UnknownServerEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity)

/**
 * Android 客户端上线
 */
class ServerAndroidOnlineEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity)

/**
 * Android 客户端下线
 */
class ServerAndroidOfflineEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity)

/**
 * 群文件上传
 */
class ServerGroupUploadFileEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity) {
    private lateinit var xmlMessage: String

    override fun decode() {
        xmlMessage = String(this.input.goto(65).readNBytes(this.input.goto(60).readShort().toInt()))
    }//todo test
}

@Suppress("EXPERIMENTAL_API_USAGE")
class ServerGroupMessageEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity) {
    var groupNumber: Long = 0
    var qq: Long = 0
    lateinit var message: String
    lateinit var messageType: MessageType

    enum class MessageType {
        NORMAL,
        XML,
        AT,
        FACE,//qq自带表情 [face107.gif]

        PLAIN_TEXT, //纯文本
        IMAGE, //自定义图片 {F50C5235-F958-6DF7-4EFA-397736E125A4}.gif

        ANONYMOUS,//匿名用户发出的消息

        OTHER,
    }


    override fun decode() {
        println(this.input.goto(0).readAllBytes().toUHexString())
        groupNumber = this.input.goto(51).readInt().toLong()
        qq = this.input.goto(56).readLong()
        val fontLength = this.input.goto(108).readShort()
        //println(this.input.goto(110 + fontLength).readNBytesAt(2).toUHexString())//always 00 00

        messageType = when (val id = this.input.goto(110 + fontLength + 2).readByte().toInt()) {
            0x13 -> MessageType.NORMAL
            0x0E -> MessageType.XML
            0x06 -> MessageType.AT


            0x01 -> MessageType.PLAIN_TEXT
            0x02 -> MessageType.FACE
            0x03 -> MessageType.IMAGE
            0x19 -> MessageType.ANONYMOUS

            else -> {
                MiraiLogger.debug("ServerGroupMessageEventPacket id=$id")
                MessageType.OTHER
            }
        }


        when (messageType) {
            MessageType.NORMAL -> {
                val gzippedMessage = this.input.goto(110 + fontLength + 16).readNBytes(this.input.goto(110 + fontLength + 3).readShort().toInt() - 11)
                ByteArrayOutputStream().let {
                    GZIPInputStream(gzippedMessage.inputStream()).transferTo(it)
                    message = String(it.toByteArray())
                }
            }

            MessageType.XML -> {
                val gzippedMessage = this.input.goto(110 + fontLength + 9).readNBytes(this.input.goto(110 + fontLength + 3).readShort().toInt() - 4)
                ByteArrayOutputStream().let {
                    GZIPInputStream(gzippedMessage.inputStream()).transferTo(it)
                    message = String(it.toByteArray())
                }
            }

            MessageType.FACE -> {
                val faceId = this.input.goto(110 + fontLength + 8).readByte()
                message = "[face${faceId}.gif]"
            }

            MessageType.AT, MessageType.OTHER, MessageType.PLAIN_TEXT, MessageType.IMAGE, MessageType.ANONYMOUS -> {
                var messageLength: Int = this.input.goto(110 + fontLength + 6).readShort().toInt()
                message = String(this.input.goto(110 + fontLength + 8).readNBytes(messageLength))

                val oeLength: Int
                if (this.input.readByte().toInt() == 6) {
                    oeLength = this.input.readShort().toInt()
                    this.input.skip(4)
                    val messageLength2 = this.input.readShort().toInt()
                    val message2 = String(this.input.readNBytes(messageLength2))
                    message += message2
                    messageLength += messageLength2
                } else {
                    oeLength = this.input.readShort().toInt()
                }

                //读取 nick, ignore.
                /*
                when (this.input.goto(110 + fontLength + 3 + oeLength).readByteAt().toInt()) {
                    12 -> {
                        this.input.skip(4)//maybe 5?

                    }
                    19 -> {

                    }
                    0x0E -> {

                    }
                    else -> {
                    }
                }*/
            }
        }
    }
}

class ServerFriendMessageEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity) {
    var qq: Long = 0
    lateinit var message: MessageChain


    override fun decode() {
        input.goto(0)
        println()
        println(input.readAllBytes().toUHexString())
        input.goto(0)

        qq = input.readUIntAt(0).toLong()

        val l1 = input.readShortAt(22)
        input.goto(93 + l1)
        input.readVarByteArray()//font
        input.skip(2)//2个0x00
        message = input.readSections()
        println(message.toObjectString())

        /*
        val offset = unknownLength0 + fontLength//57
        message = MessageChain(PlainText(let {
            val length = input.readShortAt(101 + offset)//
            input.goto(103 + offset).readString(length.toInt())
        }))*/
    }

    private fun DataInputStream.readSection(): Message? {
        val messageType = this.readByte().toInt()
        val sectionLength = this.readShort().toLong()//sectionLength: short
        this.skip(1)//message和face是 0x01, image是0x06
        return when (messageType) {
            0x01 -> PlainText(readVarString())
            0x02 -> {
                //00  01  AF  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  F0
                //00  01  0C  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  4D

                val id1 = FaceID.ofId(readLVNumber().toInt())//可能这个是id, 也可能下面那个
                this.skip(this.readByte().toLong())
                this.readLVNumber()//某id?
                return Face(id1)
            }
            0x06 -> {
                this.skip(sectionLength - 37 - 1)
                val imageId = String(this.readNBytes(36))
                this.skip(1)//0x41
                return Image("{$imageId}.jpg")//todo 如何确定文件后缀??
            }
            else -> null
        }

    }

    private fun DataInputStream.readSections(): MessageChain {
        val chain = MessageChain()
        var got: Message? = null
        do {
            if (got != null) {
                chain.concat(got)
            }
            got = this.readSection()
        } while (got != null)
        return chain
    }
}

fun main() {
    println(String("16  20  20  39  39  31  30  20  38  38  31  43  42  20  20  20  20  20  20  31  37  36  32  65  42  39  45  32  37  32  31  43  39  36  44  37  39  41  38  32  31  36  45  30  41  44  34  30  42  35  39  35  39  31  38  36  2E  6A  70  67  66  2F  65  64  33  39  30  66  38  34  2D  34  66  38  37  2D  34  36  64  63  2D  62  33  38  35  2D  34  35  35  36  62  35  31  30  61  61  35  33  41".replace("  ", " ").hexToBytes()))
    println(".jpg".toByteArray().size)
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


/**
 * 告知服务器已经收到数据
 */
@PacketId("")//随后写入
class ClientEventResponsePacket(
        private val qq: Long,
        private val packetIdFromServer: ByteArray,//4bytes
        private val sessionKey: ByteArray,
        private val eventIdentity: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.write(packetIdFromServer)//packet id 4bytes

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            it.write(eventIdentity)
        }
    }

    override fun getFixedId(): String {
        return packetIdFromServer.toUHexString()
    }
}

/*
3E 03 3F A2 76 E4 B8 DD 00 09 7C 3F 64 5C 2A 60 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 02 38 03 3E 03 3F A2 76 E4 B8 DD 01 10 9D D6 12 EA BC 07 91 EF DC 29 75 67 A9 1E 00 0B 2F E4 5D 6B A8 F6 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 6B A8 F6 08 7E 90 CE 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00
3E 03 3F A2 76 E4 B8 DD 00 03 5F 85 64 5C 2A A4 1F 40 00 A6 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 02 38 03 3E 03 3F A2 76 E4 B8 DD 01 10 9D D6 12 EA BC 07 91 EF DC 29 75 67 A9 1E 00 0B 2F E5 5D 6B A9 16 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D 6B A9 17 1B B3 4D D7 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

 */

/*


backup

class ServerFriendMessageEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity) {
    var qq: Long = 0
    lateinit var message: String



    override fun decode() {
        //start at Sep1.0:27
        qq = input.readIntAt(0)
        val msgLength = input.readShortAt(22)
        val fontLength = input.readShortAt(93+msgLength)
        val offset = msgLength+fontLength
        message = if(input.readByteAt(97+offset).toUHexString() == "02"){
            "[face" + input.goto(103+offset).readByteAt(1).toInt().toString() + ".gif]"
            //.gif
        }else {
            val offset2 = input.readShortAt(101 + offset)
            input.goto(103 + offset).readString(offset2.toInt())
        }
    }
}
 */