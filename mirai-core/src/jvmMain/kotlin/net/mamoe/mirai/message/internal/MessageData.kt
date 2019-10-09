package net.mamoe.mirai.message.internal

import net.mamoe.mirai.message.*
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.*
import java.io.DataInputStream

internal fun ByteArray.parseMessageFace(): Face = dataDecode(this) {
    //00  01  AF  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  F0
    //00  01  0C  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  4D
    it.skip(1)

    val id1 = FaceID.ofId(it.readLVNumber().toInt())//可能这个是id, 也可能下面那个
    it.skip(it.readByte().toLong())
    it.readLVNumber()//某id?
    return@dataDecode Face(id1)
}

internal fun ByteArray.parsePlainText(): PlainText = dataDecode(this) {
    it.skip(1)
    PlainText(it.readLVString())
}

internal fun ByteArray.parseMessageImage0x06(): Image = dataDecode(this) {
    it.skip(1)
    MiraiLogger.debug("好友的图片")
    MiraiLogger.debug(this.toUHexString())
    val filenameLength = it.readShort()
    val suffix = it.readString(filenameLength).substringAfter(".")
    it.skip(this.size - 37 - 1 - filenameLength - 2)
    val imageId = String(it.readNBytes(36))
    MiraiLogger.debug(imageId)
    it.skip(1)//0x41
    return@dataDecode Image("{$imageId}.$suffix")
}

internal fun ByteArray.parseMessageImage0x03(): Image = dataDecode(this) {
    it.skip(1)
    return@dataDecode Image(String(it.readLVByteArray()))
    /*
    println(String(it.readLVByteArray()))
    it.readTLVMap()
    return@dataDecode Image(String(it.readLVByteArray().cutTail(5).getRight(42)))
    /
    it.skip(data.size - 47)
    val imageId = String(it.readNBytes(42))
    it.skip(1)//0x41
    it.skip(1)//0x42
    it.skip(1)//0x43
    it.skip(1)//0x41

    return@dataDecode Image(imageId)*/
}

internal fun ByteArray.parseMessageChain(): MessageChain = dataDecode(this) {
    it.readMessageChain()
}

internal fun DataInputStream.readMessage(): Message? {
    val messageType = this.readByte().toInt()
    val sectionLength = this.readShort().toLong()//sectionLength: short
    val sectionData = this.readNBytes(sectionLength)
    return when (messageType) {
        0x01 -> sectionData.parsePlainText()
        0x02 -> sectionData.parseMessageFace()
        0x03 -> sectionData.parseMessageImage0x03()
        0x06 -> sectionData.parseMessageImage0x06()


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

fun DataInputStream.readMessageChain(): MessageChain {
    val chain = MessageChain()
    var got: Message? = null
    do {
        if (got != null) {
            chain.concat(got)
        }
        if (this.available() == 0) {
            return chain
        }
        got = this.readMessage()
    } while (got != null)
    return chain
}

fun MessageChain.toByteArray(): ByteArray = dataEncode { result ->
    this@toByteArray.list.forEach { message ->
        result.write(with(message) {
            when (this) {
                is Face -> dataEncode { section ->
                    section.writeByte(MessageType.FACE.intValue)

                    section.writeLVByteArray(dataEncode { child ->
                        child.writeShort(1)
                        child.writeByte(this.id.id)

                        child.writeHex("0B 00 08 00 01 00 04 52 CC F5 D0 FF")

                        child.writeShort(2)
                        child.writeByte(0x14)//??
                        child.writeByte(this.id.id + 65)
                    })
                }

                is At -> throw UnsupportedOperationException("At is not supported now but is expecting to be supported")

                is Image -> dataEncode { section ->
                    section.writeByte(MessageType.IMAGE.intValue)

                    section.writeLVByteArray(dataEncode { child ->
                        child.writeByte(0x02)
                        child.writeLVString(this.imageId)
                        child.writeHex("04 00 " +
                                "04 9B 53 B0 08 " +
                                "05 00 " +
                                "04 D9 8A 5A 70 " +
                                "06 00 " +
                                "04 00 00 00 50 " +
                                "07 00 " +
                                "01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 11 00 00 00 15 00 04 00 00 02 BC 16 00 04 00 00 02 BC 18 00 04 00 00 7D 5E FF 00 5C 15 36 20 39 32 6B 41 31 43 39 62 35 33 62 30 30 38 64 39 38 61 35 61 37 30 20")
                        child.writeHex("20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20")
                        child.writeBytes(this.imageId)
                        child.writeByte(0x41)
                    })
                }

                is PlainText -> dataEncode { section ->
                    section.writeByte(MessageType.PLAIN_TEXT.intValue)

                    section.writeLVByteArray(dataEncode { child ->
                        child.writeByte(0x01)
                        child.writeLVString(this.stringValue)
                    })
                }

                else -> throw UnsupportedOperationException("${this::class.simpleName} is not supported")
            }
        })
    }
}