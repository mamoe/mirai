@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.message.internal

import kotlinx.io.core.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*

internal fun IoBuffer.parseMessageFace(): Face {
    //00  01  AF  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  F0
    //00  01  0C  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  4D
    discardExact(1)

    val id1 = FaceID.ofId(readLVNumber().toInt().toUByte())//可能这个是id, 也可能下面那个
    discardExact(readByte().toLong())
    readLVNumber()//某id?
    return Face(id1)
}

internal fun IoBuffer.parsePlainText(): PlainText {
    discardExact(1)
    return PlainText(readLVString())
}

internal fun IoBuffer.parseMessageImage0x06(): Image {
    discardExact(1)
    this.debugPrint("好友的图片")
    //MiraiLogger.logDebug(this.toUHexString())
    val filenameLength = readShort()
    val suffix = readString(filenameLength).substringAfter(".")
    discardExact(this@parseMessageImage0x06.readRemaining - 37 - 1 - filenameLength - 2)
    val imageId = readString(36)
    MiraiLogger.logDebug(imageId)
    discardExact(1)//0x41
    return Image("{$imageId}.$suffix")
}

internal fun IoBuffer.parseMessageImage0x03(): Image {
    discardExact(1)
    return Image(String(readLVByteArray()))
    /*
    println(String(readLVByteArray()))
    readTLVMap()
    return Image(String(readLVByteArray().cutTail(5).getRight(42)))
    /
    discardExact(data.size - 47)
    val imageId = String(readBytes(42))
    discardExact(1)//0x41
    discardExact(1)//0x42
    discardExact(1)//0x43
    discardExact(1)//0x41

    return Image(imageId)*/
}

internal fun ByteReadPacket.parseMessageChain(): MessageChain {
    return readMessageChain()
}

internal fun ByteReadPacket.readMessage(): Message? {
    val messageType = this.readByte().toInt()
    val sectionLength = this.readShort().toLong()//sectionLength: short
    val sectionData = this.readIoBuffer(sectionLength.toInt())//use buffer instead
    return when (messageType) {
        0x01 -> sectionData.parsePlainText()
        0x02 -> sectionData.parseMessageFace()
        0x03 -> sectionData.parseMessageImage0x03()
        0x06 -> sectionData.parseMessageImage0x06()


        0x19 -> {//未知, 可能是长文本?
            //bot手机自己跟自己发消息会出这个
            //sectionData: 01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00
            sectionData.readBytes().debugPrint("sectionData")
            return PlainText("[UNKNOWN(${this.readBytes().toUHexString()})]")
            println()
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
            this.discardExact(7)//几个TLV
            return PlainText(String(value))
        }

        0x0E -> {
            null
        }

        else -> {
            println("未知的messageType=0x${messageType.toByte().toUHexString()}")
            println("后文=${this.readBytes().toUHexString()}")
            null
        }
    }

}

fun ByteReadPacket.readMessageChain(): MessageChain {
    val chain = MessageChain()
    do {
        if (this.remaining == 0L) {
            return chain
        }
    } while (this.readMessage().takeIf { it != null }?.let { chain.concat(it) } != null)
    return chain
}

fun MessageChain.toPacket(): ByteReadPacket = buildPacket {
    this@toPacket.list.forEach { message ->
        writePacket(with(message) {
            when (this) {
                is Face -> buildPacket {
                    writeUByte(MessageType.FACE.value)

                    writeLVPacket {
                        writeShort(1)
                        writeUByte(id.id)

                        writeHex("0B 00 08 00 01 00 04 52 CC F5 D0 FF")

                        writeShort(2)
                        writeByte(0x14)//??
                        writeUByte((id.id + 65u).toUByte())
                    }
                }

                is At -> throw UnsupportedOperationException("At is not supported now but is expecting to be supported")

                is Image -> buildPacket {
                    writeUByte(MessageType.IMAGE.value)

                    writeLVPacket {
                        writeByte(0x02)
                        writeLVString(imageId)
                        writeHex("04 00 " +
                                "04 9B 53 B0 08 " +
                                "05 00 " +
                                "04 D9 8A 5A 70 " +
                                "06 00 " +
                                "04 00 00 00 50 " +
                                "07 00 " +
                                "01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 11 00 00 00 15 00 04 00 00 02 BC 16 00 04 00 00 02 BC 18 00 04 00 00 7D 5E FF 00 5C 15 36 20 39 32 6B 41 31 43 39 62 35 33 62 30 30 38 64 39 38 61 35 61 37 30 20")
                        writeHex("20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20")
                        writeStringUtf8(imageId)
                        writeByte(0x41)
                    }
                }

                is PlainText -> buildPacket {
                    writeUByte(MessageType.PLAIN_TEXT.value)

                    writeLVPacket {
                        writeByte(0x01)
                        writeLVString(stringValue)
                    }
                }

                else -> throw UnsupportedOperationException("${this::class.simpleName} is not supported")
            }
        })
    }
}