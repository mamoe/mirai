/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.message

import kotlinx.io.core.readUInt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.utils.MiraiDebugAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.discardExact
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.toByteArray

private val AT_BUF_1 = byteArrayOf(0x00, 0x01, 0x00, 0x00, 0x00, 0x07, 0x00) // groupCard = 0x07; nick = 0x0A
private val AT_BUF_2 = ByteArray(2)

internal fun At.toJceData(): ImMsgBody.Text {
    return ImMsgBody.Text(
        str = this.toString(),
        attr6Buf = AT_BUF_1 + this.target.toInt().toByteArray() + AT_BUF_2
    )
}

internal fun NotOnlineImageFromFile.toJceData(): ImMsgBody.NotOnlineImage {
    return ImMsgBody.NotOnlineImage(
        filePath = this.filepath,
        resId = this.resourceId,
        oldPicMd5 = false,
        picMd5 = this.md5,
        fileLen = this.fileLength,
        picHeight = this.height,
        picWidth = this.width,
        bizType = this.bizType,
        imgType = this.imageType,
        downloadPath = this.downloadPath,
        original = this.original,
        fileId = this.fileId,
        pbReserve = byteArrayOf(0x78, 0x02)
    )
}

/*
CustomFace#24412994 {
guid=<Empty ByteArray>
filePath={01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png
shortcut=
buffer=<Empty ByteArray>
flag=00 00 00 00
oldData=15 36 20 39 32 6B 41 31 00 38 37 32 66 30 36 36 30 33 61 65 31 30 33 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 30 31 45 39 34 35 31 42 2D 37 30 45 44 2D 45 41 45 33 2D 42 33 37 43 2D 31 30 31 46 31 45 45 42 46 35 42 35 7D 2E 70 6E 67 41
fileId=0x872F0660(-2026961312)
serverIp=0x3AE103B7(987825079)
serverPort=0x00000050(80)
fileType=0x00000000(0)
signature=<Empty ByteArray>
useful=0x00000001(1)
md5=01 E9 45 1B 70 ED EA E3 B3 7C 10 1F 1E EB F5 B5
thumbUrl=/gchatpic_new/1040400290/1041235568-2268005984-01E9451B70EDEAE3B37C101F1EEBF5B5/198?term=2
bigUrl=
origUrl=/gchatpic_new/1040400290/1041235568-2268005984-01E9451B70EDEAE3B37C101F1EEBF5B5/0?term=2
bizType=0x00000000(0)
repeatIndex=0x00000000(0)
repeatImage=0x00000000(0)
imageType=0x00000000(0)
index=0x00000000(0)
width=0x0000015F(351)
height=0x000000EB(235)
source=0x00000000(0)
size=0x0000057C(1404)
origin=0x00000000(0)
thumbWidth=0x000000C6(198)
thumbHeight=0x00000084(132)
showLen=0x00000000(0)
downloadLen=0x00000000(0)
_400Url=/gchatpic_new/1040400290/1041235568-2268005984-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2
_400Width=0x0000015F(351)
_400Height=0x000000EB(235)
pbReserve=<Empty ByteArray>
}
 */
val FACE_BUF = "00 01 00 04 52 CC F5 D0".hexToBytes()

internal fun Face.toJceData(): ImMsgBody.Face {
    return ImMsgBody.Face(
        index = this.id,
        old = (0x1445 - 4 + this.id).toShort().toByteArray(),
        buf = FACE_BUF
    )
}

internal fun CustomFaceFromFile.toJceData(): ImMsgBody.CustomFace {
    return ImMsgBody.CustomFace(
        filePath = this.filepath,
        fileId = this.fileId,
        serverIp = this.serverIp,
        serverPort = this.serverPort,
        fileType = this.fileType,
        signature = this.signature,
        useful = this.useful,
        md5 = this.md5,
        bizType = this.bizType,
        imageType = this.imageType,
        width = this.width,
        height = this.height,
        source = this.source,
        size = this.size,
        origin = this.original,
        pbReserve = this.pbReserve,
        flag = ByteArray(4),
        //_400Height = 235,
        //_400Url = "/gchatpic_new/1040400290/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
        //_400Width = 351,
        oldData = oldData
    )
}

private val oldData: ByteArray =
    "15 36 20 39 32 6B 41 31 00 38 37 32 66 30 36 36 30 33 61 65 31 30 33 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 30 31 45 39 34 35 31 42 2D 37 30 45 44 2D 45 41 45 33 2D 42 33 37 43 2D 31 30 31 46 31 45 45 42 46 35 42 35 7D 2E 70 6E 67 41".hexToBytes()

/*
customFace=CustomFace#2050019814 {
        guid=<Empty ByteArray>
        filePath=5F6C522DEAC4F36C0ED8EF362660EFD6.png
        shortcut=
        buffer=<Empty ByteArray>
        flag=<Empty ByteArray>
        oldData=<Empty ByteArray>
        fileId=0xB40AF10E(-1274351346)
        serverIp=0xB703E13A(-1224482502)
        serverPort=0x00000050(80)
        fileType=0x00000042(66)
        signature=6B 44 61 76 72 79 68 79 57 67 70 52 41 45 78 49
        useful=0x00000001(1)
        md5=5F 6C 52 2D EA C4 F3 6C 0E D8 EF 36 26 60 EF D6
        thumbUrl=
        bigUrl=
        origUrl=
        bizType=0x00000005(5)
        repeatIndex=0x00000000(0)
        repeatImage=0x00000000(0)
        imageType=0x000003E9(1001)
        index=0x00000000(0)
        width=0x0000005F(95)
        height=0x00000054(84)
        source=0x00000067(103)
        size=0x000006E2(1762)
        origin=0x00000000(0)
        thumbWidth=0x00000000(0)
        thumbHeight=0x00000000(0)
        showLen=0x00000000(0)
        downloadLen=0x00000000(0)
        _400Url=
        _400Width=0x00000000(0)
        _400Height=0x00000000(0)
        pbReserve=08 01 10 00 32 00 4A 0E 5B E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 5D 50 00 78 05
}

notOnlineImage=NotOnlineImage#2050019814 {
        filePath=41AEF2D4B5BD24CF3791EFC5FEB67D60.jpg
        fileLen=0x00000350(848)
        downloadPath=/f2b7e5c0-acb3-4e83-aa5c-c8383840cc91
        oldVerSendFile=<Empty ByteArray>
        imgType=0x000003E8(1000)
        previewsImage=<Empty ByteArray>
        picMd5=41 AE F2 D4 B5 BD 24 CF 37 91 EF C5 FE B6 7D 60
        picHeight=0x00000032(50)
        picWidth=0x00000033(51)
        resId=/f2b7e5c0-acb3-4e83-aa5c-c8383840cc91
        flag=<Empty ByteArray>
        thumbUrl=
        original=0x00000000(0)
        bigUrl=
        origUrl=
        bizType=0x00000005(5)
        result=0x00000000(0)
        index=0x00000000(0)
        opFaceBuf=<Empty ByteArray>
        oldPicMd5=false
        thumbWidth=0x00000000(0)
        thumbHeight=0x00000000(0)
        fileId=0x00000000(0)
        showLen=0x00000000(0)
        downloadLen=0x00000000(0)
        _400Url=
        _400Width=0x00000000(0)
        _400Height=0x00000000(0)
        pbReserve=08 01 10 00 32 00 42 0E 5B E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 5D 50 00 78 05
}
 */

private val atAllData = ImMsgBody.Elem(
    text = ImMsgBody.Text(
        str = "@全体成员",
        attr6Buf = "00 01 00 00 00 05 01 00 00 00 00 00 00".hexToBytes()
    )
)

internal fun MessageChain.toRichTextElems(): MutableList<ImMsgBody.Elem> {
    val elements = mutableListOf<ImMsgBody.Elem>()

    if (this.any<QuoteReply>()) {
        when (val source = this[QuoteReply].source) {
            is MessageSourceFromServer -> elements.add(ImMsgBody.Elem(srcMsg = source.delegate))
            is MessageSourceFromMsg -> elements.add(ImMsgBody.Elem(srcMsg = source.toJceData()))
            else -> error("unsupported MessageSource implementation: ${source::class.simpleName}")
        }
    }

    this.forEach {
        when (it) {
            is PlainText -> elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.stringValue)))
            is At -> elements.add(ImMsgBody.Elem(text = it.toJceData()))
            is CustomFaceFromFile -> elements.add(ImMsgBody.Elem(customFace = it.toJceData()))
            is CustomFaceFromServer -> elements.add(ImMsgBody.Elem(customFace = it.delegate))
            is NotOnlineImageFromServer -> elements.add(ImMsgBody.Elem(notOnlineImage = it.delegate))
            is NotOnlineImageFromFile -> elements.add(ImMsgBody.Elem(notOnlineImage = it.toJceData()))
            is AtAll -> elements.add(atAllData)
            is Face -> elements.add(ImMsgBody.Elem(face = it.toJceData()))
            is QuoteReply,
            is MessageSource -> {

            }
            else -> error("unsupported message type: ${it::class.simpleName}")
        }
    }

    // if(this.any<QuoteReply>()){
    elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = "78 00 F8 01 00 C8 02 00".hexToBytes())))
    // }

    return elements
}

internal class CustomFaceFromServer(
    internal val delegate: ImMsgBody.CustomFace
) : CustomFace() {
    override val filepath: String get() = delegate.filePath
    override val fileId: Int get() = delegate.fileId
    override val serverIp: Int get() = delegate.serverIp
    override val serverPort: Int get() = delegate.serverPort
    override val fileType: Int get() = delegate.fileType
    override val signature: ByteArray get() = delegate.signature
    override val useful: Int get() = delegate.useful
    override val md5: ByteArray get() = delegate.md5
    override val bizType: Int get() = delegate.bizType
    override val imageType: Int get() = delegate.imageType
    override val width: Int get() = delegate.width
    override val height: Int get() = delegate.height
    override val source: Int get() = delegate.source
    override val size: Int get() = delegate.size
    override val original: Int get() = delegate.origin
    override val pbReserve: ByteArray get() = delegate.pbReserve
    override val imageId: String get() = delegate.filePath

    override fun equals(other: Any?): Boolean {
        return other is CustomFaceFromServer && other.filepath == this.filepath && other.md5.contentEquals(this.md5)
    }

    override fun hashCode(): Int {
        return filepath.hashCode() + 31 * md5.hashCode()
    }
}

internal class NotOnlineImageFromServer(
    internal val delegate: ImMsgBody.NotOnlineImage
) : NotOnlineImage() {
    override val resourceId: String get() = delegate.resId
    override val md5: ByteArray get() = delegate.picMd5
    override val filepath: String get() = delegate.filePath
    override val fileLength: Int get() = delegate.fileLen
    override val height: Int get() = delegate.picHeight
    override val width: Int get() = delegate.picWidth
    override val bizType: Int get() = delegate.bizType
    override val imageType: Int get() = delegate.imgType
    override val downloadPath: String get() = delegate.downloadPath
    override val fileId: Int get() = delegate.fileId
    override val original: Int get() = delegate.original

    override fun equals(other: Any?): Boolean {
        return other is NotOnlineImageFromServer && other.resourceId == this.resourceId && other.md5.contentEquals(this.md5)
    }

    override fun hashCode(): Int {
        return resourceId.hashCode() + 31 * md5.hashCode()
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
internal fun MsgComm.Msg.toMessageChain(): MessageChain {
    val elements = this.msgBody.richText.elems

    val message = MessageChain(initialCapacity = elements.size + 1)
    message.add(MessageSourceFromMsg(delegate = this))
    elements.joinToMessageChain(message)
    return message
}

// These two functions are not the same.

@UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
internal fun ImMsgBody.SourceMsg.toMessageChain(): MessageChain {
    val elements = this.elems!!

    val message = MessageChain(initialCapacity = elements.size + 1)
    message.add(MessageSourceFromServer(delegate = this))
    elements.joinToMessageChain(message)
    return message
}


@UseExperimental(MiraiInternalAPI::class, ExperimentalUnsignedTypes::class, MiraiDebugAPI::class)
internal fun List<ImMsgBody.Elem>.joinToMessageChain(message: MessageChain) {
    this.forEach {
        when {
            it.srcMsg != null -> message.add(QuoteReply(MessageSourceFromServer(it.srcMsg)))
            it.notOnlineImage != null -> message.add(NotOnlineImageFromServer(it.notOnlineImage))
            it.customFace != null -> message.add(CustomFaceFromServer(it.customFace))
            it.face != null -> message.add(Face(it.face.index))
            it.text != null -> {
                if (it.text.attr6Buf.isEmpty()) {
                    message.add(it.text.str.toMessage())
                } else {
                    // 00 01 00 00 00 05 01 00 00 00 00 00 00 all
                    // 00 01 00 00 00 0A 00 3E 03 3F A2 00 00 one/nick
                    // 00 01 00 00 00 07 00 44 71 47 90 00 00 one/groupCard
                    val id: Long
                    it.text.attr6Buf.read {
                        discardExact(7)
                        id = readUInt().toLong()
                    }
                    if (id == 0L) {
                        message.add(AtAll)
                    } else {
                        message.add(At(id, it.text.str))
                    }
                }
            }
        }
    }

}