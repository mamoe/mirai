package net.mamoe.mirai.qqandroid.utils

import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody


internal fun MessageChain.toRichTextElems(): MutableList<ImMsgBody.Elem> {
    val elems = mutableListOf<ImMsgBody.Elem>()

    this.forEach {
        when (it) {
            is PlainText -> {
                elems.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.stringValue)))
            }
            is At -> {

            }
        }
    }

    return elems
}


internal fun ImMsgBody.RichText.toMessageChain() : MessageChain{
    val message = MessageChain(initialCapacity = elems.size)

    elems.forEach {
        when {
            it.notOnlineImage != null -> message.add(Image(
                ImageIdQQA(
                    it.notOnlineImage.resId,
                    it.notOnlineImage.origUrl
                )
            ))
            it.customFace != null -> message.add(Image(
                ImageIdQQA(
                    it.customFace.filePath,
                    it.customFace.origUrl
                )
            ))
            it.text != null -> message.add(it.text.str.toMessage())
        }
    }

    return message
}

internal class ImageIdQQA(
    override val value: String,
    originalLink: String
) : ImageId {
    val link: ImageLink =
        ImageLinkQQA("http://gchat.qpic.cn$originalLink")
}

internal inline class ImageLinkQQA(override val original: String) : ImageLink