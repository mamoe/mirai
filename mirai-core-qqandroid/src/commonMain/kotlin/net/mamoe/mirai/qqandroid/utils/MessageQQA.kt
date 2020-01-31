package net.mamoe.mirai.qqandroid.utils

import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.utils.io.hexToBytes


internal fun MessageChain.toRichTextElems(): MutableList<ImMsgBody.Elem> {
    val elems = mutableListOf<ImMsgBody.Elem>()

    this.forEach {
        when (it) {
            is PlainText -> {
                elems.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.stringValue)))
            }
            is At -> {

            }
            is Image -> {
                elems.add(
                    ImMsgBody.Elem(
                        notOnlineImage = ImMsgBody.NotOnlineImage(
                            filePath = it.id.value, // 错了, 应该是 2B23D705CAD1F2CF3710FE582692FCC4.jpg
                            fileLen = 1149, // 假的
                            downloadPath = it.id.value,
                            imgType = 1000, // 不确定
                            picMd5 = "2B 23 D7 05 CA D1 F2 CF 37 10 FE 58 26 92 FC C4".hexToBytes(),
                            picHeight = 66,
                            picWidth = 66,
                            resId = it.id.value,
                            bizType = 5,
                            pbReserve = ImMsgBody.PbReserve.DEFAULT // 可能还可以改变 `[动画表情]`
                        )
                    )
                )
            }
        }
    }

    return elems
}


internal fun ImMsgBody.RichText.toMessageChain(): MessageChain {
    val message = MessageChain(initialCapacity = elems.size)

    elems.forEach {
        when {
            it.notOnlineImage != null -> message.add(
                Image(
                    ImageIdQQA(
                        it.notOnlineImage.resId,
                        it.notOnlineImage.origUrl
                    )
                )
            )
            it.customFace != null -> message.add(
                Image(
                    ImageIdQQA(
                        it.customFace.filePath,
                        it.customFace.origUrl
                    )
                )
            )
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