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
import kotlinx.io.core.readUShort
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.cleanupRubbishMessageElements
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.joinToMessageChain
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.toAudio
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.utils.toLongUnsigned
import net.mamoe.mirai.utils.toUHexString
import net.mamoe.mirai.utils.unzip

/**
 * 只在手动构造 [OfflineMessageSource] 时调用
 */
internal fun ImMsgBody.SourceMsg.toMessageChainNoSource(
    bot: Bot,
    messageSourceKind: MessageSourceKind,
    groupIdOrZero: Long,
    refineContext: RefineContext = EmptyRefineContext,
): MessageChain {
    val elements = this.elems
    return buildMessageChain(elements.size + 1) {
        joinToMessageChain(elements, groupIdOrZero, messageSourceKind, bot, this)
    }.cleanupRubbishMessageElements().refineLight(bot, refineContext)
}


internal suspend fun List<MsgComm.Msg>.toMessageChainOnline(
    bot: Bot,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind,
    refineContext: RefineContext = EmptyRefineContext,
): MessageChain {
    return toMessageChain(bot, groupIdOrZero, true, messageSourceKind).refineDeep(bot, refineContext)
}

internal suspend fun MsgComm.Msg.toMessageChainOnline(
    bot: Bot,
    refineContext: RefineContext = EmptyRefineContext,
): MessageChain {
    fun getSourceKind(c2cCmd: Int): MessageSourceKind {
        return when (c2cCmd) {
            11 -> MessageSourceKind.FRIEND // bot 给其他人发消息
            4 -> MessageSourceKind.FRIEND // bot 给自己作为好友发消息 (非 other client)
            1 -> MessageSourceKind.GROUP
            else -> error("Could not get source kind from c2cCmd: $c2cCmd")
        }
    }

    val kind = getSourceKind(msgHead.c2cCmd)
    val groupId = when (kind) {
        MessageSourceKind.GROUP -> msgHead.groupInfo?.groupCode ?: 0
        else -> 0
    }
    return listOf(this).toMessageChainOnline(bot, groupId, kind, refineContext)
}

//internal fun List<MsgComm.Msg>.toMessageChainOffline(
//    bot: Bot,
//    groupIdOrZero: Long,
//    messageSourceKind: MessageSourceKind
//): MessageChain {
//    return toMessageChain(bot, groupIdOrZero, false, messageSourceKind).refineLight(bot)
//}

internal fun List<MsgComm.Msg>.toMessageChainNoSource(
    bot: Bot,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind,
    refineContext: RefineContext = EmptyRefineContext,
): MessageChain {
    return toMessageChain(bot, groupIdOrZero, null, messageSourceKind).refineLight(bot, refineContext)
}


private fun List<MsgComm.Msg>.toMessageChain(
    bot: Bot,
    groupIdOrZero: Long,
    onlineSource: Boolean?,
    messageSourceKind: MessageSourceKind,
): MessageChain {
    val messageList = this


    val elements = messageList.flatMap { it.msgBody.richText.elems }

    val builder = MessageChainBuilder(elements.size)

    if (onlineSource != null) {
        builder.add(ReceiveMessageTransformer.createMessageSource(bot, onlineSource, messageSourceKind, messageList))
    }

    joinToMessageChain(elements, groupIdOrZero, messageSourceKind, bot, builder)

    for (msg in messageList) {
        msg.msgBody.richText.ptt?.toAudio()?.let { builder.add(it) }
    }

    return builder.build().cleanupRubbishMessageElements()
}

/**
 * 接收消息的解析器. 将 [MsgComm.Msg] 转换为对应的 [SingleMessage]
 * @see joinToMessageChain
 */
internal object ReceiveMessageTransformer {
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
        bot: Bot,
        builder: MessageChainBuilder,
    ) {
        //        ProtoBuf.encodeToHexString(elements).soutv("join")
        // (this._miraiContentToString().soutv())
        for (element in elements) {
            transformElement(element, groupIdOrZero, messageSourceKind, bot, builder)
            when {
                element.richMsg != null -> decodeRichMessage(element.richMsg, builder)
            }
        }
    }

    private fun transformElement(
        element: ImMsgBody.Elem,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        bot: Bot,
        builder: MessageChainBuilder,
    ) {
        when {
            element.srcMsg != null -> decodeSrcMsg(element.srcMsg, builder, bot, messageSourceKind, groupIdOrZero)
            element.notOnlineImage != null -> builder.add(OnlineFriendImageImpl(element.notOnlineImage))
            element.customFace != null -> decodeCustomFace(element.customFace, builder)
            element.face != null -> builder.add(Face(element.face.index))
            element.text != null -> decodeText(element.text, builder)
            element.marketFace != null -> builder.add(MarketFaceInternal(element.marketFace))
            element.lightApp != null -> decodeLightApp(element.lightApp, builder)
            element.customElem != null -> decodeCustomElem(element.customElem, builder)
            element.commonElem != null -> decodeCommonElem(element.commonElem, builder)
            element.transElemInfo != null -> decodeTransElem(element.transElemInfo, builder)

            element.elemFlags2 != null
                    || element.extraInfo != null
                    || element.generalFlags != null
                    || element.anonGroupMsg != null
            -> {
                // ignore
            }
            else -> {
                UnsupportedMessageImpl(element).takeIf {
                    it.struct.isNotEmpty()
                }?.let(builder::add)
                // println(it._miraiContentToString())
            }
        }
    }

    fun MessageChainBuilder.compressContinuousPlainText() {
        var index = 0
        val builder = StringBuilder()
        while (index + 1 < size) {
            val elm0 = get(index)
            val elm1 = get(index + 1)
            if (elm0 is PlainText && elm1 is PlainText) {
                builder.setLength(0)
                var end = -1
                for (i in index until size) {
                    val elm = get(i)
                    if (elm is PlainText) {
                        end = i
                        builder.append(elm.content)
                    } else break
                }
                set(index, PlainText(builder.toString()))
                // do delete
                val index1 = index + 1
                repeat(end - index) {
                    removeAt(index1)
                }
            }
            index++
        }

        // delete empty plain text
        removeAll { it is PlainText && it.content.isEmpty() }
    }

    fun MessageChain.cleanupRubbishMessageElements(): MessageChain {
        val builder = MessageChainBuilder(initialSize = count()).also {
            it.addAll(this)
        }

        kotlin.run moveQuoteReply@{ // Move QuoteReply after MessageSource
            val exceptedQuoteReplyIndex = builder.indexOfFirst { it is MessageSource } + 1
            val quoteReplyIndex = builder.indexOfFirst { it is QuoteReply }
            if (quoteReplyIndex < 1) return@moveQuoteReply
            if (quoteReplyIndex != exceptedQuoteReplyIndex) {
                val qr = builder[quoteReplyIndex]
                builder.removeAt(quoteReplyIndex)
                builder.add(exceptedQuoteReplyIndex, qr)
            }
        }

        kotlin.run quote@{
            val quoteReplyIndex = builder.indexOfFirst { it is QuoteReply }
            if (quoteReplyIndex >= 0) {
                // QuoteReply + At + PlainText(space 1)
                if (quoteReplyIndex < builder.size - 1) {
                    if (builder[quoteReplyIndex + 1] is At) {
                        builder.removeAt(quoteReplyIndex + 1)
                    }
                    if (quoteReplyIndex < builder.size - 1) {
                        val elm = builder[quoteReplyIndex + 1]
                        if (elm is PlainText && elm.content.startsWith(' ')) {
                            if (elm.content.length == 1) {
                                builder.removeAt(quoteReplyIndex + 1)
                            } else {
                                builder[quoteReplyIndex + 1] = PlainText(elm.content.substring(1))
                            }
                        }
                    }
                    return@quote
                }
            }
        }

        // TIM audios
        if (builder.any { it is Audio }) {
            builder.remove(UNSUPPORTED_VOICE_MESSAGE_PLAIN)
        }

        kotlin.run { // VipFace
            val vipFaceIndex = builder.indexOfFirst { it is VipFace }
            if (vipFaceIndex >= 0 && vipFaceIndex < builder.size - 1) {
                val l = builder[vipFaceIndex] as VipFace
                val text = builder[vipFaceIndex + 1]
                if (text is PlainText) {
                    if (text.content.length == 4 + (l.count / 10) + l.kind.name.length) {
                        builder.removeAt(vipFaceIndex + 1)
                    }
                }
            }
        }

        fun removeSuffixText(index: Int, text: PlainText) {
            if (index >= 0 && index < builder.size - 1) {
                if (builder[index + 1] == text) {
                    builder.removeAt(index + 1)
                }
            }
        }

        removeSuffixText(builder.indexOfFirst { it is LongMessageInternal }, UNSUPPORTED_MERGED_MESSAGE_PLAIN)
        removeSuffixText(builder.indexOfFirst { it is PokeMessage }, UNSUPPORTED_POKE_MESSAGE_PLAIN)

        builder.compressContinuousPlainText()

        return builder.asMessageChain()
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
        bot: Bot,
        messageSourceKind: MessageSourceKind,
        groupIdOrZero: Long,
    ) {
        list.add(QuoteReply(OfflineMessageSourceImplData(srcMsg, bot, messageSourceKind, groupIdOrZero)))
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
        list: MessageChainBuilder,
    ) {
        val content = runWithBugReport("解析 lightApp",
            { "resId=" + lightApp.msgResid + "data=" + lightApp.data.toUHexString() }) {
            when (lightApp.data[0].toInt()) {
                0 -> lightApp.data.decodeToString(startIndex = 1)
                1 -> lightApp.data.unzip(1).decodeToString()
                else -> error("unknown compression flag=${lightApp.data[0]}")
            }
        }

        list.add(LightAppInternal(content))
    }

    private fun decodeCustomElem(
        customElem: ImMsgBody.CustomElem,
        list: MessageChainBuilder,
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

    private fun decodeTransElem(
        transElement: ImMsgBody.TransElem,
        list: MessageChainBuilder,
    ) {
        // file
        // type=24
        when (transElement.elemType) {
            24 -> transElement.elemValue.read {
                // group file feed
                // 01 00 77 08 06 12 0A 61 61 61 61 61 61 2E 74 78 74 1A 06 31 35 42 79 74 65 3A 5F 12 5D 08 66 12 25 2F 64 37 34 62 62 66 33 61 2D 37 62 32 35 2D 31 31 65 62 2D 38 34 66 38 2D 35 34 35 32 30 30 37 62 35 64 39 66 18 0F 22 0A 61 61 61 61 61 61 2E 74 78 74 28 00 3A 00 42 20 61 33 32 35 66 36 33 34 33 30 65 37 61 30 31 31 66 37 64 30 38 37 66 63 33 32 34 37 35 34 39 63
                //                fun getFileRsrvAttr(file: ObjMsg.MsgContentInfo.MsgFile): HummerResv21.ResvAttr? {
                //                    if (file.ext.isEmpty()) return null
                //                    val element = kotlin.runCatching {
                //                        jsonForFileDecode.parseToJsonElement(file.ext) as? JsonObject
                //                    }.getOrNull() ?: return null
                //                    val extInfo = element["ExtInfo"]?.toString()?.decodeBase64() ?: return null
                //                    return extInfo.loadAs(HummerResv21.ResvAttr.serializer())
                //                }

                val var7 = readByte()
                if (var7 == 1.toByte()) {
                    while (remaining > 2) {
                        val proto = readProtoBuf(ObjMsg.ObjMsg.serializer(), readUShort().toInt())
                        // proto.msgType=6

                        val file = proto.msgContentInfo.firstOrNull()?.msgFile ?: continue // officially get(0) only.
                        //                        val attr = getFileRsrvAttr(file) ?: continue
                        //                        val info = attr.forwardExtFileInfo ?: continue

                        list.add(
                            FileMessageImpl(
                                id = file.filePath,
                                busId = file.busId, // path i.e. /a99e95fa-7b2d-11eb-adae-5452007b698a
                                name = file.fileName,
                                size = file.fileSize
                            )
                        )
                    }
                }
            }
        }

    }

    private val jsonForFileDecode = Json {
        isLenient = true
        coerceInputValues = true
    }

    private fun decodeCommonElem(
        commonElem: ImMsgBody.CommonElem,
        list: MessageChainBuilder,
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
        builder: MessageChainBuilder,
    ) {
        val content = runWithBugReport("解析 richMsg", { richMsg.template1.toUHexString() }) {
            when (richMsg.template1[0].toInt()) {
                0 -> richMsg.template1.decodeToString(startIndex = 1)
                1 -> richMsg.template1.unzip(1).decodeToString()
                else -> error("unknown compression flag=${richMsg.template1[0]}")
            }
        }

        fun findStringProperty(name: String): String {
            return content.substringAfter("$name=\"", "").substringBefore("\"", "")
        }

        val serviceId = when (val sid = richMsg.serviceId) {
            0 -> {
                val serviceIdStr = findStringProperty("serviceID")
                if (serviceIdStr.isEmpty() || serviceIdStr.isBlank()) {
                    0
                } else {
                    serviceIdStr.toIntOrNull() ?: 0
                }
            }
            else -> sid
        }
        when (serviceId) {
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

                val resId = findStringProperty("m_resid")
                val fileName = findStringProperty("m_fileName").takeIf { it.isNotEmpty() }

                val msg = if (resId.isEmpty()) {
                    // Nested ForwardMessage
                    if (fileName != null && findStringProperty("action") == "viewMultiMsg") {
                        ForwardMessageInternal(content, null, fileName)
                    } else {
                        SimpleServiceMessage(35, content)
                    }
                } else when (findStringProperty("multiMsgFlag").toIntOrNull()) {
                    1 -> LongMessageInternal(content, resId)
                    0 -> ForwardMessageInternal(content, resId, fileName)
                    else -> {
                        // from PC QQ
                        if (findStringProperty("action") == "viewMultiMsg") {
                            ForwardMessageInternal(content, resId, fileName)
                        } else {
                            SimpleServiceMessage(35, content)
                        }
                    }
                }

                builder.add(msg)
            }

            // 104 新群员入群的消息
            else -> {
                builder.add(SimpleServiceMessage(serviceId, content))
            }
        }
    }

    fun ImMsgBody.Ptt.toAudio() = OnlineAudioImpl(
        filename = fileName.decodeToString(),
        fileMd5 = fileMd5,
        fileSize = fileSize.toLongUnsigned(),
        codec = AudioCodec.fromId(format),
        url = downPara.decodeToString(),
        length = time.toLongUnsigned(),
        originalPtt = this,
    )
}