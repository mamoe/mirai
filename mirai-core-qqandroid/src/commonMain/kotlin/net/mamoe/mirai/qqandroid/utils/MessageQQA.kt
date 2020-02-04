package net.mamoe.mirai.qqandroid.utils

import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.utils.io.hexToBytes

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
        downloadPath = this.downloadPath
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
        pbReserve = this.pbReserve
    )
}

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
internal fun MessageChain.toRichTextElems(): MutableList<ImMsgBody.Elem> {
    val elements = mutableListOf<ImMsgBody.Elem>()

    this.forEach {
        when (it) {
            is PlainText -> elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.stringValue)))
            is At -> {

            }
            is CustomFaceFromFile -> elements.add(ImMsgBody.Elem(customFace = it.toJceData()))
            is CustomFaceFromServer -> elements.add(ImMsgBody.Elem(customFace = it.delegate))
            is NotOnlineImageFromServer -> elements.add(ImMsgBody.Elem(notOnlineImage = it.delegate))
            is NotOnlineImageFromFile -> elements.add(
                ImMsgBody.Elem(
                    notOnlineImage = it.toJceData(), generalFlags = ImMsgBody.GeneralFlags(
                        pbReserve = "78 00 F8 01 00 C8 02 00".hexToBytes()
                    )
                )
            )
        }
    }

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
    override val pbReserve: ByteArray get() = delegate.pbReserve
}

internal class NotOnlineImageFromServer(
    internal val delegate: ImMsgBody.NotOnlineImage
) : NotOnlineImage() {
    override val resourceId: String
        get() = delegate.resId
    override val md5: ByteArray
        get() = delegate.picMd5
    override val filepath: String
        get() = delegate.filePath
    override val fileLength: Int
        get() = delegate.fileLen
    override val height: Int
        get() = delegate.picHeight
    override val width: Int
        get() = delegate.picWidth
    override val bizType: Int
        get() = delegate.bizType
    override val imageType: Int
        get() = delegate.imgType
    override val downloadPath: String
        get() = delegate.downloadPath

}

internal fun ImMsgBody.RichText.toMessageChain(): MessageChain {
    val message = MessageChain(initialCapacity = elems.size)

    elems.forEach {
        when {
            it.notOnlineImage != null -> message.add(NotOnlineImageFromServer(it.notOnlineImage))
            it.customFace != null -> message.add(CustomFaceFromServer(it.customFace))
            it.text != null -> message.add(it.text.str.toMessage())
        }
    }

    return message
}


internal inline class ImageLinkQQA(override val original: String) : ImageLink