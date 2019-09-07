package net.mamoe.mirai.network.packet

import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.message.defaults.PlainText
import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.toUHexString
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.util.zip.GZIPInputStream

/**
 * Packet id: `00 CE` or `00 17`
 *
 * @author Him188moe
 */
open class ServerEventPacket(input: DataInputStream, val packetId: ByteArray, val eventIdentity: ByteArray) : ServerPacket(input) {
    @PacketId("00 17")
    class Raw(input: DataInputStream, private val packetId: ByteArray) : ServerPacket(input) {
        @ExperimentalUnsignedTypes
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

    @ExperimentalUnsignedTypes
    override fun decode() {
        groupNumber = this.input.goto(51).readInt().toLong()
        qq = this.input.goto(56).readLong().toUInt().toLong()
        val fontLength = this.input.goto(108).readShort()
        //println(this.input.goto(110 + fontLength).readNBytesAt(2).toUHexString())//always 00 00

        messageType = when (val id = this.input.goto(110 + fontLength + 2).readByte().toInt()) {
            19 -> MessageType.NORMAL
            14 -> MessageType.XML
            6 -> MessageType.AT


            1 -> MessageType.PLAIN_TEXT
            2 -> MessageType.FACE
            3 -> MessageType.IMAGE
            25 -> MessageType.ANONYMOUS

            else -> {
                MiraiLogger debug ("ServerGroupMessageEventPacket id=$id")
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

    @ExperimentalUnsignedTypes
    override fun decode() {
        //start at Sep1.0:27
        input.goto(0)
        println(input.readAllBytes().toUHexString())
        input.goto(0)

        qq = input.readIntAt(0).toLong()
        val msgLength = input.readShortAt(22)
        val fontLength = input.readShortAt(93 + msgLength)
        val offset = msgLength + fontLength
        message = MessageChain(PlainText(let {
            val offset2 = input.readShortAt(101 + offset)
            input.goto(103 + offset).readVarString(offset2.toInt())
        }))
    }

}


/**
 * 告知服务器已经收到数据
 */
@PacketId("")//随后写入
@ExperimentalUnsignedTypes
class ClientMessageResponsePacket(
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


    @ExperimentalUnsignedTypes
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
            input.goto(103 + offset).readVarString(offset2.toInt())
        }
    }
}
 */