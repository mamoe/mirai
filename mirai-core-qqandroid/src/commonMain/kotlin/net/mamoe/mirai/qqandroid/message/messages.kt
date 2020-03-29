/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file: OptIn(MiraiExperimentalAPI::class, MiraiInternalAPI::class, LowLevelAPI::class, ExperimentalUnsignedTypes::class)

package net.mamoe.mirai.qqandroid.message

import kotlinx.io.core.*
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.encodeToString
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.toByteArray

internal fun At.toJceData(): ImMsgBody.Text {
    val text = this.toString()
    return ImMsgBody.Text(
        str = text,
        attr6Buf = buildPacket {
            // MessageForText$AtTroopMemberInfo
            writeShort(1) // const
            writeShort(0) // startPos
            writeShort(text.length.toShort()) // textLen
            writeByte(0) // flag, may=1
            writeInt(target.toInt()) // uin
            writeShort(0) // const
        }.readBytes()
    )
}

internal fun OfflineFriendImage.toJceData(): ImMsgBody.NotOnlineImage {
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

internal val FACE_BUF = "00 01 00 04 52 CC F5 D0".hexToBytes()

internal fun Face.toJceData(): ImMsgBody.Face {
    return ImMsgBody.Face(
        index = this.id,
        old = (0x1445 - 4 + this.id).toShort().toByteArray(),
        buf = FACE_BUF
    )
}

internal fun OfflineGroupImage.toJceData(): ImMsgBody.CustomFace {
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


private val atAllData = ImMsgBody.Elem(
    text = ImMsgBody.Text(
        str = "@全体成员",
        attr6Buf = buildPacket {
            // MessageForText$AtTroopMemberInfo
            writeShort(1) // const
            writeShort(0) // startPos
            writeShort("@全体成员".length.toShort()) // textLen
            writeByte(1) // flag, may=1
            writeInt(0) // uin
            writeShort(0) // const
        }.readBytes()
    )
)

private val UNSUPPORTED_MERGED_MESSAGE_PLAIN = PlainText("你的QQ暂不支持查看[转发多条消息]，请期待后续版本。")

@OptIn(MiraiInternalAPI::class, MiraiExperimentalAPI::class)
internal fun MessageChain.toRichTextElems(forGroup: Boolean, withGeneralFlags: Boolean): MutableList<ImMsgBody.Elem> {
    val elements = mutableListOf<ImMsgBody.Elem>()

    if (this.any<QuoteReply>()) {
        when (val source = this[QuoteReply].source) {
            is MessageSourceFromServer -> elements.add(ImMsgBody.Elem(srcMsg = source.delegate))
            is MessageSourceFromMsg -> elements.add(ImMsgBody.Elem(srcMsg = source.toJceData()))
            is MessageSourceFromSend -> elements.add(ImMsgBody.Elem(srcMsg = source.toJceData()))
            else -> error("unsupported MessageSource implementation: ${source::class.simpleName}")
        }
    }

    var longTextResId: String? = null

    fun transformOneMessage(it: Message) {
        if (it is RichMessage) {
            val content = MiraiPlatformUtils.zip(it.content.toByteArray())
            when (it) {
                is LongMessage -> {
                    check(longTextResId == null) { "There must be no more than one LongMessage element in the message chain" }
                    elements.add(
                        ImMsgBody.Elem(
                            richMsg = ImMsgBody.RichMsg(
                                serviceId = 35, // ok
                                template1 = byteArrayOf(1) + content
                            )
                        )
                    )
                    transformOneMessage(UNSUPPORTED_MERGED_MESSAGE_PLAIN)
                    longTextResId = it.resId
                }
                is LightApp -> elements.add(
                    ImMsgBody.Elem(
                        lightApp = ImMsgBody.LightAppElem(
                            data = byteArrayOf(1) + content
                        )
                    )
                )
                else -> elements.add(
                    ImMsgBody.Elem(
                        richMsg = ImMsgBody.RichMsg(
                            serviceId = when (it) {
                                is XmlMessage -> 60
                                is JsonMessage -> 1
                                //   is MergedForwardedMessage -> 35
                                else -> error("unsupported RichMessage: ${it::class.simpleName}")
                            },
                            template1 = byteArrayOf(1) + content
                        )
                    )
                )
            }
        }

        when (it) {
            is PlainText -> elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.stringValue)))
            is At -> {
                elements.add(ImMsgBody.Elem(text = it.toJceData()))
                elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = " ")))
            }
            is OfflineGroupImage -> elements.add(ImMsgBody.Elem(customFace = it.toJceData()))
            is OnlineGroupImageImpl -> elements.add(ImMsgBody.Elem(customFace = it.delegate))
            is OnlineFriendImageImpl -> elements.add(ImMsgBody.Elem(notOnlineImage = it.delegate))
            is OfflineFriendImage -> elements.add(ImMsgBody.Elem(notOnlineImage = it.toJceData()))
            is AtAll -> elements.add(atAllData)
            is Face -> elements.add(ImMsgBody.Elem(face = it.toJceData()))
            is QuoteReplyToSend -> {
                if (forGroup) {
                    check(it is QuoteReplyToSend.ToGroup) {
                        "sending a quote to group using QuoteReplyToSend.ToFriend is prohibited"
                    }
                    if (it.sender is Member) {
                        transformOneMessage(it.createAt())
                    }
                    transformOneMessage(PlainText(" "))
                }
            }
            is QuoteReply, // already transformed above
            is MessageSource, // mirai only
            is RichMessage, // already transformed above
            -> {

            }
            else -> error("unsupported message type: ${it::class.simpleName}")
        }
    }
    this.forEach(::transformOneMessage)

    if (withGeneralFlags) {
        when {
            longTextResId != null -> {
                elements.add(
                    ImMsgBody.Elem(
                        generalFlags = ImMsgBody.GeneralFlags(
                            longTextFlag = 1,
                            longTextResid = longTextResId!!,
                            pbReserve = "78 00 F8 01 00 C8 02 00".hexToBytes()
                        ),
                    )
                )
            }
            this.any<RichMessage>() -> {
                // 08 09 78 00 A0 01 81 DC 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00
                elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_RICH_MESSAGE)))
            }
            else -> elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_ELSE)))
        }
    }

    return elements
}

private val PB_RESERVE_FOR_RICH_MESSAGE =
    "08 09 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00".hexToBytes()
private val PB_RESERVE_FOR_ELSE = "78 00 F8 01 00 C8 02 00".hexToBytes()

internal class OnlineGroupImageImpl(
    internal val delegate: ImMsgBody.CustomFace
) : OnlineGroupImage() {
    override val filepath: String = delegate.filePath
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
    override val imageId: String = ExternalImage.generateImageId(delegate.md5, imageType)
    override val originUrl: String
        get() = "http://gchat.qpic.cn" + delegate.origUrl

    override fun equals(other: Any?): Boolean {
        return other is OnlineGroupImageImpl && other.filepath == this.filepath && other.md5.contentEquals(this.md5)
    }

    override fun hashCode(): Int {
        return imageId.hashCode() + 31 * md5.hashCode()
    }
}

internal class OnlineFriendImageImpl(
    internal val delegate: ImMsgBody.NotOnlineImage
) : OnlineFriendImage() {
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
    override val originUrl: String
        get() = "http://c2cpicdw.qpic.cn" + this.delegate.origUrl

    override fun equals(other: Any?): Boolean {
        return other is OnlineFriendImageImpl && other.resourceId == this.resourceId && other.md5
            .contentEquals(this.md5)
    }

    override fun hashCode(): Int {
        return imageId.hashCode() + 31 * md5.hashCode()
    }
}

@OptIn(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
internal fun MsgComm.Msg.toMessageChain(): MessageChain {
    val elements = this.msgBody.richText.elems

    return buildMessageChain(elements.size + 1) {
        +MessageSourceFromMsg(delegate = this@toMessageChain)
        elements.joinToMessageChain(this)
    }.cleanupRubbishMessageElements()
}

// These two functions are not identical, dont combine.
@OptIn(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
internal fun ImMsgBody.SourceMsg.toMessageChain(): MessageChain {
    val elements = this.elems!!

    return buildMessageChain(elements.size + 1) {
        +MessageSourceFromServer(delegate = this@toMessageChain)
        elements.joinToMessageChain(this)
    }.cleanupRubbishMessageElements()
}

private fun MessageChain.cleanupRubbishMessageElements(): MessageChain {
    var last: SingleMessage? = null
    return buildMessageChain(initialSize = this.count()) {
        this@cleanupRubbishMessageElements.forEach { element ->
            if (last == null) {
                last = element
                return@forEach
            } else {
                if (last is LongMessage && element is PlainText) {
                    if (element == UNSUPPORTED_MERGED_MESSAGE_PLAIN) {
                        last = element
                        return@forEach
                    }
                }
            }

            add(element)
            last = element
        }
    }
}

internal inline fun <reified R> Iterable<*>.firstIsInstance(): R {
    this.forEach {
        if (it is R) {
            return it
        }
    }
    throw NoSuchElementException("Collection contains no element matching the predicate.")
}

internal fun List<ImMsgBody.Elem>.joinToMessageChain(message: MessageChainBuilder) {
    this.forEach {
        when {
            it.srcMsg != null -> message.add(QuoteReply(MessageSourceFromServer(it.srcMsg)))
            it.notOnlineImage != null -> message.add(OnlineFriendImageImpl(it.notOnlineImage))
            it.customFace != null -> message.add(OnlineGroupImageImpl(it.customFace))
            it.face != null -> message.add(Face(it.face.index))
            it.text != null -> {
                if (it.text.attr6Buf.isEmpty()) {
                    message.add(it.text.str.toMessage())
                } else {
                    val id: Long
                    it.text.attr6Buf.read {
                        discardExact(7)
                        id = readUInt().toLong()
                    }
                    if (id == 0L) {
                        message.add(AtAll)
                    } else {
                        message.add(At._lowLevelConstructAtInstance(id, it.text.str))
                    }
                }
            }
            it.lightApp != null -> {
                val content = MiraiPlatformUtils.unzip(it.lightApp.data, 1).encodeToString()
                message.add(LightApp(content))
            }
            it.richMsg != null -> {
                val content = MiraiPlatformUtils.unzip(it.richMsg.template1, 1).encodeToString()
                when (it.richMsg.serviceId) {
                    1 -> message.add(JsonMessage(content))
                    60 -> message.add(XmlMessage(content))
                    35 -> message.add(
                        LongMessage(
                            content,
                            this.firstIsInstance<ImMsgBody.GeneralFlags>().longTextResid
                        )
                    )
                    else -> {
                        @Suppress("DEPRECATION")
                        MiraiLogger.debug {
                            "unknown richMsg.serviceId: ${it.richMsg.serviceId}, content=${it.richMsg.template1.contentToString()}, \ntryUnzip=${content}"
                        }
                    }
                }
            }
        }
    }

}