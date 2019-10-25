@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.message.internal

import kotlinx.io.core.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.toUHexString

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
    discardExact(1)//0x01
    return PlainText(readLVString())
}

internal fun IoBuffer.parseLongText0x19(): PlainText {
    //01  00  59  AA  02  56  30  01  3A  40  6E  35  46  4F  62  68  75  4B  6F  65  31  4E  63  45  41  6B  77  4B  51  5A  5A  4C  47  54  57  43  68  30  4B  56  7A  57  44  38  67  58  70  37  62  77  6A  67  51  69  66  66  53  4A  63  4F  69  78  4F  75  37  36  49  49  4F  37  48  32  55  63  9A  01  0F  80  01  01  C8  01  00  F0  01  00  F8  01  00  90  02  00  14  01  75  01  01  6B  01  78  9C  CD  92  BB  4E  C3  30  14  86  77  9E  C2  32  73  DA  A4  21  24  48  4E  AA  F4  06  A5  B4  51  55  A0  A8  0B  4A  5D  27  35  E4  82  72  69  4B  B7  6E  08  06  C4  C0  06  42  48  30  20  21  60  62  EB  E3  34  F4  31  70  4A  11  23  23  FC  96  2C  F9  D8  BF  CF  F1  77  8C  F2  23  D7  01  03  12  84  D4  F7  54  28  64  78  08  88  87  FD  1E  F5  6C  15  C6  91  C5  29  30  AF  AD  00  26  E4  86  36  E8  06  94  58  2A  CC  FC  73  41  E0  1E  5A  D4  21  0D  D3  25  2A  2C  55  0A  1B  D2  BA  5E  E0  24  91  D7  B9  B5  72  41  E1  74  B9  5C  E0  78  25  27  8B  92  28  14  45  45  FF  76  B4  E8  98  39  18  05  13  47  0B  24  03  4A  86  F5  D8  89  68  3D  B4  21  B0  1C  93  71  11  21  08  49  30  A0  98  54  4B  6C  25  A5  E6  80  84  B4  A7  42  4F  AA  18  DD  7E  5C  F3  89  D0  C0  65  FD  78  58  6B  76  3A  3B  9B  BB  ED  62  9F  AF  ED  8F  DB  25  C5  3E  38  91  BB  C3  23  BB  49  2D  AB  B5  8D  0D  3A  32  62  79  BD  5A  35  E4  AD  DC  1E  86  40  03  88  46  C4  05  8E  79  EA  C7  11  EB  09  64  91  88  46  0E  D1  C0  5F  73  FD  4D  00  65  97  95  02  D4  0F  34  94  65  D3  B2  78  80  7D  C7  0F  54  B8  AA  F0  E9  60  8F  4A  EE  1E  3F  6E  2E  84  E4  F6  7E  3E  7D  9E  5D  5E  25  EF  67  C9  E4  15  FC  DC  81  B2  29  08  0D  85  7E  1C  60  02  BC  45  33  E7  93  F3  D9  C3  D3  FC  E5  6D  36  BD  86  2C  C3  D7  66  7A  98  FD  4F  ED  13  9B  C7  C1  78  02  00  04  00  00  00  23  0E  00  07  01  00  04  00  00  00  09
    discardExact(1)//0x01
    val raw = readLVByteArray()
    println("parseLongText0x19.raw=${raw.toUHexString()}")
    return PlainText(raw.toUHexString())
}

internal fun IoBuffer.parseMessageImage0x06(): Image {
    discardExact(1)
    //MiraiLogger.logDebug(this.toUHexString())
    val filenameLength = readShort()

    discardExact(filenameLength.toInt())
    discardExact(8)//03 00 04 00 00 02 9C 04
    val length = readShort()//=27
    return Image(ImageId(readString(length)))
    //return Image("{${readString(length)}}.$suffix")
}


//00 1B filenameLength
// 43 37 46 29 5F 34 32 34 4E 33 55 37 7B 4C 47 36 7D 4F 25 5A 51 58 51 2E 6A 70 67 get suffix
// 03 00 04 00 00 02 9C 04
// 00 25 2F 32 65 37 61 65 33 36 66 2D 61 39 31 63 2D 34 31 32 39 2D 62 61 34 32 2D 37 65 30 31 32 39 37 37 35 63 63 38 14
// 00 04 03 00 00 00 18
// 00 25 2F 32 65 37 61 65 33 36 66 2D 61 39 31 63 2D 34 31 32 39 2D 62 61 34 32 2D 37 65 30 31 32 39 37 37 35 63 63 38 19
// 00 04 00 00 00 2E 1A 00 04 00 00 00 2E FF
// 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 36 36 38 65 35 43 36 38 45 36 42 44 32 46 35 38 34 31 42 30 39 37 39 45 37 46 32 35 34 33 38 38 31 33 43 33 2E 6A 70 67 66 2F 32 65 37 61 65 33 36 66 2D 61 39 31 63 2D 34 31 32 39 2D 62 61 34 32 2D 37 65 30 31 32 39 37 37 35 63 63 38 41

fun main() {
    println(".".repeat(1000))
}

internal fun IoBuffer.parseMessageImage0x03(): Image {
    discardExact(1)
    return Image(ImageId(String(readLVByteArray())))
}

internal fun ByteReadPacket.readMessage(): Message? {
    val messageType = this.readByte().toInt()
    val sectionLength = this.readShort()
    val sectionData = this.readIoBuffer(sectionLength.toInt())

    return try {
        when (messageType) {
            //todo 在每个parse里面都 discard 了第一 byte.
            0x01 -> sectionData.parsePlainText()
            0x02 -> sectionData.parseMessageFace()
            0x03 -> sectionData.parseMessageImage0x03()
            0x06 -> sectionData.parseMessageImage0x06()


            0x19 -> {//长文本前一部分? 可能不是长文本, 总长度为 0x5C=92, body长度为0x59=89
                //19 00  5C / 01  00  59  AA  02  56  30  01  3A  40  6E  35  46  4F  62  68  75  4B  6F  65  31  4E  63  45  41  6B  77  4B  51  5A  5A  4C  47  54  57  43  68  30  4B  56  7A  57  44  38  67  58  70  37  62  77  6A  67  51  69  66  66  53  4A  63  4F  69  78  4F  75  37  36  49  49  4F  37  48  32  55  63  9A  01  0F  80  01  01  C8  01  00  F0  01  00  F8  01  00  90  02  00  14  01  75  01  01  6B  01  78  9C  CD  92  BB  4E  C3  30  14  86  77  9E  C2  32  73  DA  A4  21  24  48  4E  AA  F4  06  A5  B4  51  55  A0  A8  0B  4A  5D  27  35  E4  82  72  69  4B  B7  6E  08  06  C4  C0  06  42  48  30  20  21  60  62  EB  E3  34  F4  31  70  4A  11  23  23  FC  96  2C  F9  D8  BF  CF  F1  77  8C  F2  23  D7  01  03  12  84  D4  F7  54  28  64  78  08  88  87  FD  1E  F5  6C  15  C6  91  C5  29  30  AF  AD  00  26  E4  86  36  E8  06  94  58  2A  CC  FC  73  41  E0  1E  5A  D4  21  0D  D3  25  2A  2C  55  0A  1B  D2  BA  5E  E0  24  91  D7  B9  B5  72  41  E1  74  B9  5C  E0  78  25  27  8B  92  28  14  45  45  FF  76  B4  E8  98  39  18  05  13  47  0B  24  03  4A  86  F5  D8  89  68  3D  B4  21  B0  1C  93  71  11  21  08  49  30  A0  98  54  4B  6C  25  A5  E6  80  84  B4  A7  42  4F  AA  18  DD  7E  5C  F3  89  D0  C0  65  FD  78  58  6B  76  3A  3B  9B  BB  ED  62  9F  AF  ED  8F  DB  25  C5  3E  38  91  BB  C3  23  BB  49  2D  AB  B5  8D  0D  3A  32  62  79  BD  5A  35  E4  AD  DC  1E  86  40  03  88  46  C4  05  8E  79  EA  C7  11  EB  09  64  91  88  46  0E  D1  C0  5F  73  FD  4D  00  65  97  95  02  D4  0F  34  94  65  D3  B2  78  80  7D  C7  0F  54  B8  AA  F0  E9  60  8F  4A  EE  1E  3F  6E  2E  84  E4  F6  7E  3E  7D  9E  5D  5E  25  EF  67  C9  E4  15  FC  DC  81  B2  29  08  0D  85  7E  1C  60  02  BC  45  33  E7  93  F3  D9  C3  D3  FC  E5  6D  36  BD  86  2C  C3  D7  66  7A  98  FD  4F  ED  13  9B  C7  C1  78  02  00  04  00  00  00  23  0E  00  07  01  00  04  00  00  00  09
                //1000个 "." 被压缩为上面这条消息.

                //bot手机自己跟自己发消息会出这个
                //似乎手机发消息就会有这个?
                //sectionData: 01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00
                //             01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00
                //return null

                sectionData.parseLongText0x19()
            }


            0x14 -> {//长文本的后一部分? 总长度 0x0175=373, body长度=0x016B=363
//14  01  75  01  01  6B  01  78  9C  CD  92  4D  4F  C2  30  18  C7  EF  7E  8A  A6  1E  C9  64  83  B1  CD  A4  1B  E1  4D  19  8A  C6  20  11  BC  98  39  3A  A8  EE  C5  74  1D  20  37  6E  46  0F  C6  83  37  8D  31  D1  83  89  51  4F  DE  F8  38  4C  3E  86  1D  62  3C  7A  D4  7F  93  26  7D  DA  7F  9F  A7  BF  A7  28  3F  F4  5C  D0  C7  34  24  81  AF  43  69  45  84  00  FB  76  D0  21  7E  57  87  11  73  04  0D  E6  8D  25  C0  85  BC  B0  0B  0E  29  C1  8E  0E  57  FE  B9  20  F0  0E  1C  E2  E2  2D  CB  C3  3A  D4  A4  D5  72  A9  58  58  13  24  AD  A4  0A  B2  22  E5  84  42  59  91  85  9C  52  C8  2A  62  46  14  65  59  FD  76  34  C8  88  3B  38  05  CB  66  73  24  7D  82  07  F5  C8  65  A4  1E  76  21  70  5C  8B  73  C9  42  10  62  DA  27  36  36  CB  7C  95  4B  CC  14  87  A4  A3  C3  AA  B4  CE  82  63  B5  46  54  8D  A4  EC  F6  20  F4  68  D8  21  C5  ED  7D  B5  B9  A1  0D  46  4D  33  E5  8F  86  47  AD  DA  6E  73  2F  D3  3B  91  2B  55  53  A9  6C  B6  76  AC  96  DD  20  38  DB  36  21  30  00  22  0C  7B  C0  B5  4E  83  88  F1  9E  40  1E  61  84  B9  D8  00  7F  CD  F5  37  01  94  5E  54  0A  50  8F  1A  28  CD  A7  45  F1  C0  0E  DC  80  EA  70  59  13  93  C1  1F  15  DF  3D  7E  DC  5C  48  F1  ED  FD  6C  F2  3C  BD  BC  8A  DF  CF  E2  F1  2B  F8  B9  03  A5  13  10  06  0A  83  88  DA  18  F8  F3  66  CE  C6  E7  D3  87  A7  D9  CB  DB  74  72  0D  79  86  AF  CD  E4  30  FF  9F  C6  27  23  A2  C1  36  02  00  04  00  00  00  23  0E  00  07  01  00  04  00  00  00  09

                //是否要用 sectionData.read?
                discardExact(1)
                val value = readLVByteArray()
                println(value.size)
                println("0x14的未知压缩的data=" + value.toUHexString())
                //todo 未知压缩算法

                //后面似乎还有一节?
                //discardExact(7)//02  00  04  00  00  00  23
                return PlainText(value.toUHexString())
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
    } finally {
        sectionData.release(IoBuffer.Pool)
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

fun MessageChain.toPacket(forGroup: Boolean): ByteReadPacket = buildPacket {
    this@toPacket.forEach { message ->
        writePacket(with(message) {
            when (this) {
                is Face -> buildPacket {
                    writeUByte(MessageType.FACE.value)

                    writeShortLVPacket {
                        writeShort(1)
                        writeUByte(id.value)

                        writeHex("0B 00 08 00 01 00 04 52 CC F5 D0 FF")

                        writeShort(2)
                        writeByte(0x14)//??
                        writeUByte((id.value + 65u).toUByte())
                    }
                }

                is At -> throw UnsupportedOperationException("At is not supported now but is expecting to be supported")

                is Image -> buildPacket {
                    if (forGroup) {
                        writeUByte(MessageType.GROUP_IMAGE.value)

                        //00 00 03 00 CB 02 00 2A 7B 46 36 31 35 39 33 42 35 2D 35 42 39 38 2D 31 37 39 38 2D 33 46 34 37 2D 32 41 39 31 44 33 32 45 44 32 46 43 7D 2E 6A 70 67
                        // 04 00 04 87 E5 09 3B 05 00 04 D2 C4 C0 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 01 ED 16 00 04 00 00 02 17 18 00 04 00 00 EB 34 FF 00 5C 15 36 20 39 32 6B 41 31 43 38 37 65 35 30 39 33 62 64 32 63 34 63 30 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20
                        // 7B 46 36 31 35 39 33 42 35 2D 35 42 39 38 2D 31 37 39 38 2D 33 46 34 37 2D 32 41 39 31 44 33 32 45 44 32 46 43 7D 2E 6A 70 67 41
                        /*
                         * 00 00 06 00 F3 02 00 1B 28 52 49 5F 36 31 28 32 52 59 4B 59 43 40 37 59 29 58 29 39 29 42 49 2E 67 69 66 03 00 04 00 00 11 90 04 00 25 2F 35 37 34 63 34 32 34 30 2D 30 31 33 66 2D 34 35 39 38 2D 61 37 32 34 2D 30 36 65 66 35 36 39 39 30 64 30 62 14 00 04 03 00 00 00 0B 00 00 18 00 25 2F 35 37 34 63 34 32 34 30 2D 30 31 33 66 2D 34 35 39 38 2D 61 37 32 34 2D 30 36 65 66 35 36 39 39 30 64 30 62 19 00 04 00 00 00 2D 1A 00 04 00 00 00 2D FF 00 63 16 20 20 39 39 31 30 20 38 38 31 44 42 20 20 20 20 20 20 34 34 39 36 65 33 39 46 37 36 35 33 32 45 31 41 42 35 43 41 37 38 36 44 37 41 35 31 33 38 39 32 32 35 33 38 35 2E 67 69 66 66 2F 35 37 34 63 34 32 34 30 2D 30 31 33 66 2D 34 35 39 38 2D 61 37 32 34 2D 30 36 65 66 35 36 39 39 30 64 30 62 41
                         * 00 00 06 00 F3 02 00 1B 46 52 25 46 60 30 59 4F 4A 5A 51 48 31 46 4A 53 4C 51 4C 4A 33 46 31 2E 6A 70 67 03 00 04 00 00 02 A2 04 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 14 00 04 03 00 00 00 0B 00 00 18 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67 66 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 41
                         */

                        writeShortLVPacket {
                            //todo
                            writeUByte(0x02u)
                            writeShortLVString(id.value)
                            writeHex("04 00 04 87 E5 09 3B 05 00 04 D2 C4 C0 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 01 ED 16 00 04 00 00 02 17 18 00 04 00 00 EB 34 FF 00 5C 15 36 20 39 32 6B 41 31 43 38 37 65 35 30 39 33 62 64 32 63 34 63 30 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20")
                            writeStringUtf8(id.value)
                            writeUByte(0x41u)
                        }
                    } else {
                        writeUByte(MessageType.FRIEND_IMAGE.value)

                        // 00 00 06 00 F3 02
                        // 00 1B 24 5B 56 54 4A 38 60 4C 5A 4E 46 7D 53 39 4F 52 36 25 45 60 42 55 53 2E 6A 70 67
                        // 03 00 04 00 01 41 1B 04
                        // 00 25 2F 65 61 37 30 30 61 38 33 2D 38 38 38 62 2D 34 66 37 31 2D 61 62 64 31 2D 63 33 38 64 63 62 64 31 61 65 36 31
                        // 14 00 04 00 00 00 00 0B 00 00 18
                        // 00 25 2F 65 61 37 30 30 61 38 33 2D 38 38 38 62 2D 34 66 37 31 2D 61 62 64 31 2D 63 33 38 64 63 62 64 31 61 65 36 31
                        // 19 00 04 00 00 03 74 1A 00 04 00 00 02 BE FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20
                        ///38 32 32 30 33 65 39 36 30 46 42 35 44 37 46 42 33 39 46 34 39 39 31 37 46 34 37 33 44 41 45 31 37 30 32 46 44 31 2E 6A 70 67 66
                        // 2F 65 61 37 30 30 61 38 33 2D 38 38 38 62 2D 34 66 37 31 2D 61 62 64 31 2D 63 33 38 64 63 62 64 31 61 65 36 31 41

                        // 00 00 06 00 F3 02
                        // 00 1B 7B 48 29 47 52 53 31 29 50 24 5A 42 28 4F 35 43 44 4B 45 31 35 7B 37 2E 70 6E 67
                        // 03 00 04 00 00 6F 36 04
                        // 00 25 2F 35 65 63 32 34 63 37 62 2D 34 30 32 39 2D 34 61 39 33 2D 62 63 66 35 2D 34 31 38 34 35 32 65 39 32 33 31 64
                        // 14 00 04 00 00 00 00 0B 00 00 18
                        // 00 25 2F 35 65 63 32 34 63 37 62 2D 34 30 32 39 2D 34 61 39 33 2D 62 63 66 35 2D 34 31 38 34 35 32 65 39 32 33 31 64
                        // 19 00 04 00 00 04 14 1A 00 04 00 00 02 77 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 41 42 20 20 20 20 20
                        ///32 38 34 37 30 65 35 42 37 44 45 37 36 41 34 41 44 44 31 37 43 46 39 32 39 38 33 43 46 30 41 43 45 35 42 34 33 39 2E 70 6E 67 66
                        // 2F 35 65 63 32 34 63 37 62 2D 34 30 32 39 2D 34 61 39 33 2D 62 63 66 35 2D 34 31 38 34 35 32 65 39 32 33 31 64 41
                        /*
                         * 00 00 06 00 F3 02
                         * 00 1B 46 52 25 46 60 30 59 4F 4A 5A 51 48 31 46 4A 53 4C 51 4C 4A 33 46 31 2E 6A 70 67
                         * 03 00 04 00 00 02 A2 04
                         * 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66
                         * 14 00 04 03 00 00 00 0B 00 00 18
                         * 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66
                         * 19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20
                         **36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67 66
                         * 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 41
                         */
                        writeShortLVPacket {
                            writeByte(0x02)
                            //"46 52 25 46 60 30 59 4F 4A 5A 51 48 31 46 4A 53 4C 51 4C 4A 33 46 31 2E 6A 70 67".hexToBytes().stringOfWitch()
                            //   writeShortLVString(filename)//图片文件名 FR%F`0YOJZQH1FJSLQLJ3F1.jpg
                            writeShortLVString(id.value.substring(1..24) + ".gif")//图片文件名. 后缀不影响. 但无后缀会导致 PC QQ 无法显示这个图片
                            writeHex("03 00 04 00 00 02 A2 04")
                            writeShortLVString(id.value)
                            writeHex("14 00 04 03 00 00 00 0B 00 00 18")
                            writeShortLVString(id.value)

                            writeHex("19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 ")

                            writeStringUtf8("674e")//没有 "e" 服务器就不回复

                            writeStringUtf8(id.value.substring(1..id.value.lastIndex - 4))//这一串文件名决定手机 QQ 保存的图片. 可以随意
                            writeStringUtf8(".gif")//后缀要有
                            writeUByte(0x66u)

                            //有时候 PC QQ 看不到发这些消息, 但手机可以. 可能是 ID 过期了, 手机有缓存而电脑没有

                            /*
                            * writeHex("19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 ")

                            writeStringUtf8(id.value.subSequence(1..id.value.lastIndex))//674e1FB4C25EB4FE12E4F3BB819137BD9909.jpg
                            writeStringUtf8(".jpg")// "36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67

                            writeUByte(0x66u)
                            * */

                            //36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67 66
                            writeStringUtf8(id.value)
                            writeUByte(0x41u)
                        }
                    }
                }

                is PlainText -> buildPacket {
                    writeUByte(MessageType.PLAIN_TEXT.value)

                    writeShortLVPacket {
                        writeByte(0x01)
                        writeShortLVString(stringValue)
                    }
                }

                else -> throw UnsupportedOperationException("${this::class.simpleName} is not supported(Full MessageChain=${this@toPacket})")
            }
        })
    }
}