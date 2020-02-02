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

/*
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
            is PlainText -> {
                elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.stringValue)))
            }
            is At -> {

            }
            is NotOnlineImageFromServer -> {
                elements.add(ImMsgBody.Elem(notOnlineImage = it.delegate))
                elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = "78 00 90 01 01 F8 01 00 A0 02 00 C8 02 00".hexToBytes())))
            }
            is NotOnlineImageFromFile -> {
                elements.add(ImMsgBody.Elem(notOnlineImage = it.toJceData()))
                elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = "78 00 90 01 01 F8 01 00 A0 02 00 C8 02 00".hexToBytes())))
            }
        }
    }

    return elements
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
            it.notOnlineImage != null -> message.add(
                NotOnlineImageFromServer(it.notOnlineImage)
            )
            it.customFace != null -> message.add(
                NotOnlineImageFromFile(
                    it.customFace.filePath,
                    it.customFace.md5,
                    it.customFace.origUrl,
                    it.customFace.downloadLen,
                    it.customFace.height,
                    it.customFace.width,
                    it.customFace.bizType,
                    it.customFace.imageType,
                    it.customFace.filePath
                )
            )
            it.text != null -> message.add(it.text.str.toMessage())
        }
    }

    return message
}


internal inline class ImageLinkQQA(override val original: String) : ImageLink