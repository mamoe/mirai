@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.Face
import net.mamoe.mirai.message.defaults.Image
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.message.defaults.PlainText
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.dataDecode
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toUHexString
import net.mamoe.mirai.utils.toUInt
import java.io.DataInputStream

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

    @PacketId("")
    inner class ResponsePacket(
            val qq: Long,
            val sessionKey: ByteArray
    ) : ClientPacket() {
        override fun encode() {
            this.write(packetId)//packet id 4bytes

            this.writeQQ(qq)
            this.writeHex(TIMProtocol.fixVer2)
            this.encryptAndWrite(sessionKey) {
                write(eventIdentity)
            }
        }

        override fun getFixedId(): String {
            return packetId.toUHexString()
        }
    }
}

/**
 * Unknown event
 */
class UnknownServerEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity) {
    override fun decode() {
        super.decode()
        println("UnknownServerEventPacket data: " + this.input.goto(0).readAllBytes().toUHexString())
    }
}

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
    lateinit var senderName: String
    lateinit var message: MessageChain

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
        qq = this.input.goto(56).readNBytes(4).toUInt().toLong()

        this.input.goto(108)
        this.input.readLVByteArray()
        input.skip(2)//2个0x00
        message = input.readSections()

        val map = input.readTLVMap(true)
        if (map.containsKey(18)) {
            this.senderName = dataDecode(map.getValue(18)) {
                val tlv = it.readTLVMap(true)
                tlv.printTLVMap()

                when {
                    tlv.containsKey(0x01) -> String(tlv.getValue(0x01))
                    tlv.containsKey(0x02) -> String(tlv.getValue(0x02))
                    else -> "null"
                }
            }
        }


        /*
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
        }*/

/*
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
        }*/
    }
}

fun main() {
    println(String("E7 BE A4".hexToBytes()))


    println(".".toByteArray().toUByteArray().toUHexString())
    //长文本 22 96 29 7B B4 DF 94 AA 00 01 9F 8E 09 18 85 5B 1F 40 00 52 00 00 00 1B 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00 01 01 22 96 29 7B 01 3E 03 3F A2 00 03 7E F3 5D 7B 97 57 00 00 F3 32 00 B8 00 01 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 7B 97 56 7F D0 53 BB 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 12 01 00 0F E9 95 BF E6 96 87 E6 9C AC E6 B6 88 E6 81 AF 0E 00 0E 01 00 04 00 00 00 09 07 00 04 00 00 00 01 19 00 35 01 00 32 AA 02 2F 50 03 60 00 68 00 9A 01 26 08 09 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 05 00 04 00 00 00 01 08 00 04 00 00 00 01 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08
    val packet = ServerGroupMessageEventPacket(("" +
            "22 96 29 7B B4 DF 94 AA 00 09 8F 37 0A 65 07 2E 1F 40 00 52 00 00 00 1B 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00 01 01 22 96 29 7B 01 3E 03 3F A2 00 03 7F 67 5D 7B AE D7 00 00 F3 36 02 E7 00 02 02 00 1B 10 00 00 00 00 4D 53 47 00 00 00 00 00 5D 7B AE D6 F4 91 87 BE 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 CB 02 00 2A 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 04 00 04 83 81 3B E2 05 00 04 B8 8B 33 79 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 00 41 16 00 04 00 00 00 34 18 00 04 00 00 03 73 FF 00 5C 15 36 20 39 32 6B 41 31 43 38 33 38 31 33 62 65 32 62 38 38 62 33 33 37 39 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 41 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 77 02 00 2A 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 04 00 04 83 81 3B E2 05 00 04 B8 8B 33 79 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 00 41 16 00 04 00 00 00 34 18 00 04 00 00 03 73 FF 00 08 15 37 20 20 38 41 41 41 02 00 14 01 00 01 AF 0B 00 08 00 01 00 04 52 CC F5 D0 FF 00 02 14 F0 03 00 CE 02 00 2A 7B 31 46 42 34 43 32 35 45 2D 42 34 46 45 2D 31 32 45 34 2D 46 33 42 42 2D 38 31 39 31 33 37 42 44 39 39 30 39 7D 2E 6A 70 67 04 00 04 B8 27 4B C6 05 00 04 79 5C B1 A3 06 00 04 00 00 00 50 07 00 01 41 08 00 00 09 00 01 01 0B 00 00 14 00 04 03 00 00 00 15 00 04 00 00 00 4E 16 00 04 00 00 00 23 18 00 04 00 00 02 A2 FF 00 5F 15 36 20 39 35 6B 44 31 41 62 38 32 37 34 62 63 36 37 39 35 63 62 31 61 33 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 31 46 42 34 43 32 35 45 2D 42 34 46 45 2D 31 32 45 34 2D 46 33 42 42 2D 38 31 39 31 33 37 42 44 39 39 30 39 7D 2E 6A 70 67 41 42 43 41 0E 00 07 01 00 04 00 00 00 09 19 00 38 01 00 35 AA 02 32 50 03 60 00 68 00 9A 01 29 08 09 20 BF 02 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08 05 00 04 00 00 00 01 08 00 04 00 00 00 01" +
            "").hexToBytes().dataInputStream(), byteArrayOf(), byteArrayOf())
    packet.decode()
    println(packet)
}

//牛逼[图片]牛逼[图片] 22 96 29 7B B4 DF 94 AA 00 08 74 A4 09 18 8D CC 1F 40 00 52 00 00 00 1B 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00 01 01 22 96 29 7B 01 3E 03 3F A2 00 03 7F 64 5D 7B AC BD 00 00 F3 36 02 03 00 02 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 7B AC BD 12 73 DB A2 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 CB 02 00 2A 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 04 00 04 B4 52 77 F1 05 00 04 BC EB 03 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 00 41 16 00 04 00 00 00 34 18 00 04 00 00 03 73 FF 00 5C 15 36 20 39 32 6B 41 31 43 62 34 35 32 37 37 66 31 62 63 65 62 30 33 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 41 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 77 02 00 2A 7B 37 41 41 34 42 33 41 41 2D 38 43 33 43 2D 30 46 34 35 2D 32 44 39 42 2D 37 46 33 30 32 41 30 41 43 45 41 41 7D 2E 6A 70 67 04 00 04 B4 52 77 F1 05 00 04 BC EB 03 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 00 41 16 00 04 00 00 00 34 18 00 04 00 00 03 73 FF 00 08 15 37 20 20 38 41 41 41 0E 00 0E 01 00 04 00 00 00 09 07 00 04 00 00 00 01 19 00 35 01 00 32 AA 02 2F 50 03 60 00 68 00 9A 01 26 08 09 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 05 00 04 00 00 00 01 08 00 04 00 00 00 01 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08
//牛逼[图片]牛逼 22 96 29 7B B4 DF 94 AA 00 0B C1 0A 09 18 89 93 1F 40 00 52 00 00 00 1B 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00 01 01 22 96 29 7B 01 3E 03 3F A2 00 03 7E F5 5D 7B 97 E7 00 00 F3 32 01 8D 00 02 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 7B 97 E6 FA BE 7F DC 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E7 89 9B E9 80 BC 03 00 CF 02 00 2A 7B 39 44 32 44 45 39 31 41 2D 33 39 38 39 2D 39 35 35 43 2D 44 35 42 34 2D 37 46 41 32 37 38 39 37 38 36 30 39 7D 2E 6A 70 67 04 00 04 97 15 7F 03 05 00 04 79 5C B1 A3 06 00 04 00 00 00 50 07 00 01 41 08 00 00 09 00 01 01 0B 00 00 14 00 04 03 00 00 00 15 00 04 00 00 00 3C 16 00 04 00 00 00 40 18 00 04 00 00 03 CC FF 00 60 15 36 20 39 36 6B 45 31 41 39 37 31 35 37 66 30 33 37 39 35 63 62 31 61 33 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 39 44 32 44 45 39 31 41 2D 33 39 38 39 2D 39 35 35 43 2D 44 35 42 34 2D 37 46 41 32 37 38 39 37 38 36 30 39 7D 2E 6A 70 67 31 32 31 32 41 01 00 09 01 00 06 E7 89 9B E9 80 BC 0E 00 0E 01 00 04 00 00 00 09 07 00 04 00 00 00 01 19 00 35 01 00 32 AA 02 2F 50 03 60 00 68 00 9A 01 26 08 09 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 05 00 04 00 00 00 01 08 00 04 00 00 00 01 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08

class ServerFriendMessageEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity) {
    var qq: Long = 0
    lateinit var message: MessageChain


    override fun decode() {
        input.goto(0)
        println("ServerFriendMessageEventPacket.input=" + input.readAllBytes().toUHexString())
        input.goto(0)

        qq = input.readUIntAt(0).toLong()

        val l1 = input.readShortAt(22)
        input.goto(93 + l1)
        input.readLVByteArray()//font
        input.skip(2)//2个0x00
        message = input.readSections()

        val map: Map<Int, ByteArray> = input.readTLVMap(true).withDefault { byteArrayOf() }
        println(map.getValue(18))

        //19 00 38 01 00 35 AA 02 32 50 03 60 00 68 00 9A 01 29 08 09 20 BF 02 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08 05 00 04 00 00 00 01 08 00 04 00 00 00 01

        /*
        val offset = unknownLength0 + fontLength//57
        message = MessageChain(PlainText(let {
            val length = input.readShortAt(101 + offset)//
            input.goto(103 + offset).readString(length.toInt())
        }))*/
    }
}

private fun DataInputStream.readSection(): Message? {
    val messageType = this.readByte().toInt()
    val sectionLength = this.readShort().toLong()//sectionLength: short
    val sectionData = this.readNBytes(sectionLength)
    return when (messageType) {
        0x01 -> PlainText.PacketHelper.ofByteArray(sectionData)
        0x02 -> Face.PacketHelper.ofByteArray(sectionData)
        0x03 -> Image.PacketHelper.ofByteArray0x03(sectionData)
        0x06 -> Image.PacketHelper.ofByteArray0x06(sectionData)


        0x19 -> {//长文本
            val value = readLVByteArray()
            //todo 未知压缩算法
            PlainText(String(value))

            // PlainText(String(GZip.uncompress( value)))
        }


        0x14 -> {//长文本
            val value = readLVByteArray()
            println(value.size)
            println(value.toUHexString())
            //todo 未知压缩算法
            this.skip(7)//几个TLV
            return PlainText(String(value))
        }

        0x0E -> {
            //null
            null
        }

        else -> {
            println("未知的messageType=0x${messageType.toByte().toUHexString()}")
            println("后文=${this.readAllBytes().toUHexString()}")
            null
        }
    }

}

private fun DataInputStream.readSections(): MessageChain {
    val chain = MessageChain()
    var got: Message? = null
    do {
        if (got != null) {
            chain.concat(got)
        }
        if (this.available() == 0) {
            return chain
        }
        got = this.readSection()
    } while (got != null)
    return chain
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

class ServerFriendMessageEventPacket(input: DataInputStream, packetId: ByteArray, eventIdentity: ByteArray) : ServerEventPacket(input, packetId, eventIdentity) {
    var qq: Long = 0
    lateinit var message: String



    override fun dataDecode() {
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