/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.io.core.discardExact
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.cleanupRubbishMessageElements
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.joinToMessageChain
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.toVoice
import net.mamoe.mirai.internal.network.protocol.data.proto.CustomFace
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*

internal fun ImMsgBody.SourceMsg.toMessageChainNoSource(
    botId: Long,
    messageSourceKind: MessageSourceKind,
    groupIdOrZero: Long
): MessageChain {
    val elements = this.elems
    return buildMessageChain(elements.size + 1) {
        joinToMessageChain(elements, groupIdOrZero, messageSourceKind, botId, this)
    }.cleanupRubbishMessageElements()
}


internal fun List<MsgComm.Msg>.toMessageChainOnline(
    bot: Bot,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind
): MessageChain {
    return toMessageChain(bot, bot.id, groupIdOrZero, true, messageSourceKind)
}

internal fun List<MsgComm.Msg>.toMessageChainOffline(
    bot: Bot,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind
): MessageChain {
    return toMessageChain(bot, bot.id, groupIdOrZero, false, messageSourceKind)
}

internal fun List<MsgComm.Msg>.toMessageChainNoSource(
    botId: Long,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind
): MessageChain {
    return toMessageChain(null, botId, groupIdOrZero, null, messageSourceKind)
}

private fun List<MsgComm.Msg>.toMessageChain(
    bot: Bot?,
    botId: Long,
    groupIdOrZero: Long,
    onlineSource: Boolean?,
    messageSourceKind: MessageSourceKind
): MessageChain {
    val messageList = this


    val elements = messageList.flatMap { it.msgBody.richText.elems }

    val builder = MessageChainBuilder(elements.size)

    if (onlineSource != null) {
        checkNotNull(bot)
        builder.add(ReceiveMessageTransformer.createMessageSource(bot, onlineSource, messageSourceKind, messageList))
    }

    joinToMessageChain(elements, groupIdOrZero, messageSourceKind, botId, builder)

    for (msg in messageList) {
        msg.msgBody.richText.ptt?.toVoice()?.let { builder.add(it) }
    }

    return builder.build().cleanupRubbishMessageElements()
}

private object ReceiveMessageTransformer {
    fun createMessageSource(
        bot: Bot,
        onlineSource: Boolean,
        messageSourceKind: MessageSourceKind,
        messageList: List<MsgComm.Msg>,
    ): MessageSource {
        return when (onlineSource) {
            true -> {
                when (messageSourceKind) {
                    MessageSourceKind.TEMP -> OnlineMessageSourceFromTempImpl(bot, messageList)
                    MessageSourceKind.GROUP -> OnlineMessageSourceFromGroupImpl(bot, messageList)
                    MessageSourceKind.FRIEND -> OnlineMessageSourceFromFriendImpl(bot, messageList)
                    MessageSourceKind.STRANGER -> OnlineMessageSourceFromStrangerImpl(bot, messageList)
                }
            }
            false -> {
                OfflineMessageSourceImplData(bot, messageList, messageSourceKind)
            }
        }
    }

    fun joinToMessageChain(
        elements: List<ImMsgBody.Elem>,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        botId: Long,
        builder: MessageChainBuilder
    ) {
        // (this._miraiContentToString().soutv())
        for (element in elements) {
            transformElement(element, groupIdOrZero, messageSourceKind, botId, builder)
            when {
                element.richMsg != null -> decodeRichMessage(element.richMsg, builder)
            }
        }
    }

    private fun transformElement(
        element: ImMsgBody.Elem,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        botId: Long,
        builder: MessageChainBuilder
    ) {
        when {
            element.srcMsg != null -> decodeSrcMsg(element.srcMsg, builder, botId, messageSourceKind, groupIdOrZero)
            element.notOnlineImage != null -> builder.add(OnlineFriendImageImpl(element.notOnlineImage))
            element.customFace != null -> decodeCustomFace(element.customFace, builder)
            element.face != null -> builder.add(Face(element.face.index))
            element.text != null -> decodeText(element.text, builder)
            element.marketFace != null -> builder.add(MarketFaceImpl(element.marketFace))
            element.lightApp != null -> decodeLightApp(element.lightApp, builder)
            element.customElem != null -> decodeCustomElem(element.customElem, builder)
            element.commonElem != null -> decodeCommonElem(element.commonElem, builder)

            element.elemFlags2 != null
                    || element.extraInfo != null
                    || element.generalFlags != null -> {
                // ignore
            }
            else -> {
                // println(it._miraiContentToString())
            }
        }
    }

    fun MessageChain.cleanupRubbishMessageElements(): MessageChain {
        var previousLast: SingleMessage? = null
        var last: SingleMessage? = null
        return buildMessageChain(initialSize = this.count()) {
            this@cleanupRubbishMessageElements.forEach { element ->
                @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                if (last is LongMessageInternal && element is PlainText) {
                    if (element == UNSUPPORTED_MERGED_MESSAGE_PLAIN) {
                        previousLast = last
                        last = element
                        return@forEach
                    }
                }
                if (last is PokeMessage && element is PlainText) {
                    if (element == UNSUPPORTED_POKE_MESSAGE_PLAIN) {
                        previousLast = last
                        last = element
                        return@forEach
                    }
                }
                if (last is VipFace && element is PlainText) {
                    val l = last as VipFace
                    if (element.content.length == 4 + (l.count / 10) + l.kind.name.length) {
                        previousLast = last
                        last = element
                        return@forEach
                    }
                }
                // 解决tim发送的语音无法正常识别
                if (element is PlainText) {
                    if (element == UNSUPPORTED_VOICE_MESSAGE_PLAIN) {
                        previousLast = last
                        last = element
                        return@forEach
                    }
                }

                if (element is PlainText && last is At && previousLast is QuoteReply
                    && element.content.startsWith(' ')
                ) {
                    // Android QQ 发送, 是 Quote+At+PlainText(" xxx") // 首空格
                    removeLastOrNull() // At
                    val new = PlainText(element.content.substring(1))
                    add(new)
                    previousLast = null
                    last = new
                    return@forEach
                }

                if (element is QuoteReply) {
                    // 客户端为兼容早期不支持 QuoteReply 的客户端而添加的 At
                    removeLastOrNull()?.let { rm ->
                        if ((rm as? PlainText)?.content != " ") add(rm)
                        else removeLastOrNull()?.let { rm2 ->
                            if (rm2 !is At) add(rm2)
                        }
                    }
                }

                if (element is PlainText) { // 处理分片消息
                    append(element.content)
                } else {
                    add(element)
                }

                previousLast = last
                last = element
            }
        }
    }

    private fun decodeText(text: ImMsgBody.Text, list: MessageChainBuilder) {
        if (text.attr6Buf.isEmpty()) {
            list.add(PlainText(text.str))
        } else {
            val id: Long
            text.attr6Buf.read {
                discardExact(7)
                id = readUInt().toLong()
            }
            if (id == 0L) {
                list.add(AtAll)
            } else {
                list.add(At(id)) // element.text.str
            }
        }
    }

    private fun decodeSrcMsg(
        srcMsg: ImMsgBody.SourceMsg,
        list: MessageChainBuilder,
        botId: Long,
        messageSourceKind: MessageSourceKind,
        groupIdOrZero: Long
    ) {
        list.add(QuoteReply(OfflineMessageSourceImplData(srcMsg, botId, messageSourceKind, groupIdOrZero)))
    }

    private fun decodeCustomFace(
        customFace: ImMsgBody.CustomFace,
        builder: MessageChainBuilder,
    ) {
        builder.add(OnlineGroupImageImpl(customFace))
        customFace.pbReserve.let {
            if (it.isNotEmpty() && it.loadAs(CustomFace.ResvAttr.serializer()).msgImageShow != null) {
                builder.add(ShowImageFlag)
            }
        }
    }

    private fun decodeLightApp(
        lightApp: ImMsgBody.LightAppElem,
        list: MessageChainBuilder
    ) {
        val content = runWithBugReport("解析 lightApp",
            { "resId=" + lightApp.msgResid + "data=" + lightApp.data.toUHexString() }) {
            when (lightApp.data[0].toInt()) {
                0 -> lightApp.data.encodeToString(offset = 1)
                1 -> lightApp.data.unzip(1).encodeToString()
                else -> error("unknown compression flag=${lightApp.data[0]}")
            }
        }

        list.add(LightApp(content).refine())
    }

    private fun decodeCustomElem(
        customElem: ImMsgBody.CustomElem,
        list: MessageChainBuilder
    ) {
        customElem.data.read {
            kotlin.runCatching {
                CustomMessage.load(this)
            }.fold(
                onFailure = {
                    if (it is CustomMessage.Companion.CustomMessageFullDataDeserializeInternalException) {
                        throw IllegalStateException(
                            "Internal error: " +
                                    "exception while deserializing CustomMessage head data," +
                                    " data=${customElem.data.toUHexString()}", it
                        )
                    } else {
                        it as CustomMessage.Companion.CustomMessageFullDataDeserializeUserException
                        throw IllegalStateException(
                            "User error: " +
                                    "exception while deserializing CustomMessage body," +
                                    " body=${it.body.toUHexString()}", it
                        )
                    }

                },
                onSuccess = {
                    if (it != null) {
                        list.add(it)
                    }
                }
            )
        }
    }

    private fun decodeCommonElem(
        commonElem: ImMsgBody.CommonElem,
        list: MessageChainBuilder
    ) {
        when (commonElem.serviceType) {
            23 -> {
                val proto =
                    commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype23.serializer())
                list.add(VipFace(VipFace.Kind(proto.faceType, proto.faceSummary), proto.faceBubbleCount))
            }
            2 -> {
                val proto =
                    commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype2.serializer())
                list.add(PokeMessage(
                    proto.vaspokeName.takeIf { it.isNotEmpty() }
                        ?: PokeMessage.values.firstOrNull { it.id == proto.vaspokeId && it.pokeType == proto.pokeType }?.name
                            .orEmpty(),
                    proto.pokeType,
                    proto.vaspokeId
                )
                )
            }
            3 -> {
                val proto =
                    commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype3.serializer())
                if (proto.flashTroopPic != null) {
                    list.add(FlashImage(OnlineGroupImageImpl(proto.flashTroopPic)))
                }
                if (proto.flashC2cPic != null) {
                    list.add(FlashImage(OnlineFriendImageImpl(proto.flashC2cPic)))
                }
            }
            33 -> {
                val proto =
                    commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype33.serializer())
                list.add(Face(proto.index))

            }
        }
    }

    private fun decodeRichMessage(
        richMsg: ImMsgBody.RichMsg,
        builder: MessageChainBuilder
    ) {
        val content = runWithBugReport("解析 richMsg", { richMsg.template1.toUHexString() }) {
            when (richMsg.template1[0].toInt()) {
                0 -> richMsg.template1.encodeToString(offset = 1)
                1 -> richMsg.template1.unzip(1).encodeToString()
                else -> error("unknown compression flag=${richMsg.template1[0]}")
            }
        }
        when (richMsg.serviceId) {
            // 5: 使用微博长图转换功能分享到QQ群
            /*
                        <?xml version="1.0" encoding="utf-8"?><msg serviceID="5" templateID="12345" brief="[分享]想要沐浴阳光，就别钻进
    阴影。 ???" ><item layout="0"><image uuid="{E5F68BD5-05F8-148B-9DA7-FECD026D30AD}.jpg" md5="E5F68BD505F8148B9DA7FECD026D
    30AD" GroupFiledid="2167263882" minWidth="120" minHeight="120" maxWidth="180" maxHeight="180" /></item><source name="新
    浪微博" icon="http://i.gtimg.cn/open/app_icon/00/73/69/03//100736903_100_m.png" appid="100736903" action="" i_actionData
    ="" a_actionData="" url=""/></msg>
                         */
            /**
             * json?
             */
            1 -> @Suppress("DEPRECATION_ERROR")
            builder.add(SimpleServiceMessage(1, content))
            /**
             * [LongMessageInternal], [ForwardMessage]
             */
            35 -> {
                fun findStringProperty(name: String): String {
                    return content.substringAfter("$name=\"", "").substringBefore("\"", "")
                }

                val resId = findStringProperty("m_resid")

                val msg = if (resId.isEmpty()) {
                    SimpleServiceMessage(35, content)
                } else when (findStringProperty("multiMsgFlag").toIntOrNull()) {
                    1 -> LongMessageInternal(content, resId)
                    0 -> ForwardMessageInternal(content, resId)
                    else -> {
                        // from PC QQ
                        if (findStringProperty("action") == "viewMultiMsg") {
                            ForwardMessageInternal(content, resId)
                        } else {
                            SimpleServiceMessage(35, content)
                        }
                    }
                }

                builder.add(msg)
            }

            // 104 新群员入群的消息
            else -> {
                builder.add(SimpleServiceMessage(richMsg.serviceId, content))
            }
        }
    }

    fun ImMsgBody.Ptt.toVoice() = Voice(
        kotlinx.io.core.String(fileName),
        fileMd5,
        fileSize.toLong(),
        format,
        kotlinx.io.core.String(downPara)
    )
}

/**
 * 解析 [ForwardMessageInternal], [LongMessageInternal]
 */
internal suspend fun MessageChain.refine(contact: Contact): MessageChain {
    if (none { it is RefinableMessage }) return this
    val builder = MessageChainBuilder(this.size)
    for (singleMessage in this) {
        if (singleMessage is RefinableMessage) {
            val v = singleMessage.refine(contact, this)
            if (v != null) builder.add(v)
        } else {
            builder.add(singleMessage)
        }
    }
    return builder.build()
}
