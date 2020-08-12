/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:OptIn(LowLevelAPI::class)
@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR")

package net.mamoe.mirai.qqandroid.message

import kotlinx.io.core.String
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUInt
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.utils.*
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


private val UNSUPPORTED_MERGED_MESSAGE_PLAIN = PlainText("你的QQ暂不支持查看[转发多条消息]，请期待后续版本。")
private val UNSUPPORTED_POKE_MESSAGE_PLAIN = PlainText("[戳一戳]请使用最新版手机QQ体验新功能。")
private val UNSUPPORTED_FLASH_MESSAGE_PLAIN = PlainText("[闪照]请使用新版手机QQ查看闪照。")

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun MessageChain.toRichTextElems(forGroup: Boolean, withGeneralFlags: Boolean): MutableList<ImMsgBody.Elem> {
    val elements = ArrayList<ImMsgBody.Elem>(this.size)

    if (this.anyIsInstance<QuoteReply>()) {
        when (val source = this[QuoteReply]!!.source) {
            is MessageSourceInternal -> elements.add(ImMsgBody.Elem(srcMsg = source.toJceData()))
            else -> error("unsupported MessageSource implementation: ${source::class.simpleName}. Don't implement your own MessageSource.")
        }
    }

    var longTextResId: String? = null

    fun transformOneMessage(it: Message) {
        if (it is RichMessage) {
            val content = MiraiPlatformUtils.zip(it.content.toByteArray())
            when (it) {
                is ForwardMessageInternal -> {
                    elements.add(
                        ImMsgBody.Elem(
                            richMsg = ImMsgBody.RichMsg(
                                serviceId = it.serviceId, // ok
                                template1 = byteArrayOf(1) + content
                            )
                        )
                    )
                    transformOneMessage(UNSUPPORTED_MERGED_MESSAGE_PLAIN)
                }
                is LongMessage -> {
                    check(longTextResId == null) { "There must be no more than one LongMessage element in the message chain" }
                    elements.add(
                        ImMsgBody.Elem(
                            richMsg = ImMsgBody.RichMsg(
                                serviceId = it.serviceId, // ok
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
                                is ServiceMessage -> it.serviceId
                                else -> error("unsupported RichMessage: ${it::class.simpleName}")
                            },
                            template1 = byteArrayOf(1) + content
                        )
                    )
                )
            }
        }

        when (it) {
            is PlainText -> elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.content)))
            is CustomMessage -> {
                @Suppress("UNCHECKED_CAST")
                elements.add(
                    ImMsgBody.Elem(
                        customElem = ImMsgBody.CustomElem(
                            enumType = MIRAI_CUSTOM_ELEM_TYPE,
                            data = CustomMessage.dump(it.getFactory() as CustomMessage.Factory<CustomMessage>, it)
                        )
                    )
                )
            }
            is At -> {
                elements.add(ImMsgBody.Elem(text = it.toJceData()))
                elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = " ")))
            }
            is PokeMessage -> {
                elements.add(
                    ImMsgBody.Elem(
                        commonElem = ImMsgBody.CommonElem(
                            serviceType = 2,
                            businessType = it.type,
                            pbElem = HummerCommelem.MsgElemInfoServtype2(
                                pokeType = it.type,
                                vaspokeId = it.id,
                                vaspokeMinver = "7.2.0",
                                vaspokeName = it.name
                            ).toByteArray(HummerCommelem.MsgElemInfoServtype2.serializer())
                        )
                    )
                )
                transformOneMessage(UNSUPPORTED_POKE_MESSAGE_PLAIN)
            }
            is @Suppress("DEPRECATION")
            OfflineGroupImage
            -> elements.add(ImMsgBody.Elem(customFace = it.toJceData()))
            is OnlineGroupImageImpl -> elements.add(ImMsgBody.Elem(customFace = it.delegate))
            is OnlineFriendImageImpl -> elements.add(ImMsgBody.Elem(notOnlineImage = it.delegate))
            is @Suppress("DEPRECATION")
            OfflineFriendImage
            -> elements.add(ImMsgBody.Elem(notOnlineImage = it.toJceData()))
            is GroupFlashImage -> elements.add(it.toJceData())
                .also { transformOneMessage(UNSUPPORTED_FLASH_MESSAGE_PLAIN) }
            is FriendFlashImage -> elements.add(it.toJceData())
                .also { transformOneMessage(UNSUPPORTED_FLASH_MESSAGE_PLAIN) }
            is AtAll -> elements.add(atAllData)
            is Face -> elements.add(ImMsgBody.Elem(face = it.toJceData()))
            is QuoteReply -> {
                if (forGroup) {
                    when (val source = it.source) {
                        is OnlineMessageSource.Incoming.FromGroup -> {
                            transformOneMessage(At(source.sender))
                            transformOneMessage(PlainText(" "))
                        }
                    }
                }
            }
            is VipFace -> {
                transformOneMessage(PlainText(it.contentToString()))
            }
            is PttMessage,
            is ForwardMessage,
            is MessageSource, // mirai metadata only
            is RichMessage // already transformed above
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
                        )
                    )
                )
            }
            this.anyIsInstance<RichMessage>() -> {
                // 08 09 78 00 A0 01 81 DC 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00
                elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_RICH_MESSAGE)))
            }
            this.anyIsInstance<FlashImage>() -> {
                elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_DOUTU)))
            }
            this.anyIsInstance<PttMessage>() -> {
                elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_PTT)))
            }
            else -> elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_ELSE)))
        }
    }

    return elements
}

private val PB_RESERVE_FOR_RICH_MESSAGE =
    "08 09 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00".hexToBytes()

private val PB_RESERVE_FOR_PTT =
    "78 00 F8 01 00 C8 02 00 AA 03 26 08 22 12 22 41 20 41 3B 25 3E 16 45 3F 43 2F 29 3E 44 24 14 18 46 3D 2B 4A 44 3A 18 2E 19 29 1B 26 32 31 31 29 43".hexToBytes()

@Suppress("SpellCheckingInspection")
private val PB_RESERVE_FOR_DOUTU = "78 00 90 01 01 F8 01 00 A0 02 00 C8 02 00".hexToBytes()
private val PB_RESERVE_FOR_ELSE = "78 00 F8 01 00 C8 02 00".hexToBytes()

internal fun MsgComm.Msg.toMessageChain(
    bot: Bot,
    groupIdOrZero: Long,
    onlineSource: Boolean,
    isTemp: Boolean = false
): MessageChain {
    val elements = this.msgBody.richText.elems
    val ptt = this.msgBody.richText.ptt

    val pptMsg = ptt?.run {
        when(fileType) {
            4 -> Voice(String(fileName), fileMd5, String(downPara))
            else -> null
        }
    }

    return buildMessageChain(elements.size + 1 + if (pptMsg == null) 0 else 1) {
        if (onlineSource) {
            when {
                isTemp -> +MessageSourceFromTempImpl(bot, this@toMessageChain)
                groupIdOrZero != 0L -> +MessageSourceFromGroupImpl(bot, this@toMessageChain)
                else -> +MessageSourceFromFriendImpl(bot, this@toMessageChain)
            }
        } else {
            +OfflineMessageSourceImplByMsg(this@toMessageChain, bot)
        }
        elements.joinToMessageChain(groupIdOrZero, bot, this)
        pptMsg?.let(::add)
    }.cleanupRubbishMessageElements()
}

// These two functions have difference method signature, don't combine.

internal fun ImMsgBody.SourceMsg.toMessageChain(bot: Bot, groupIdOrZero: Long): MessageChain {
    val elements = this.elems!!

    return buildMessageChain(elements.size + 1) {
        +OfflineMessageSourceImplBySourceMsg(delegate = this@toMessageChain, bot = bot, groupIdOrZero = groupIdOrZero)
        elements.joinToMessageChain(groupIdOrZero, bot, this)
    }.cleanupRubbishMessageElements()
}

private fun MessageChain.cleanupRubbishMessageElements(): MessageChain {
    var last: SingleMessage? = null
    return buildMessageChain(initialSize = this.count()) {
        this@cleanupRubbishMessageElements.forEach { element ->
            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
            if (last is LongMessage && element is PlainText) {
                if (element == UNSUPPORTED_MERGED_MESSAGE_PLAIN) {
                    last = element
                    return@forEach
                }
            }
            if (last is PokeMessage && element is PlainText) {
                if (element == UNSUPPORTED_POKE_MESSAGE_PLAIN) {
                    last = element
                    return@forEach
                }
            }
            if (last is VipFace && element is PlainText) {
                val l = last as VipFace
                if (element.content.length == 4 + (l.count / 10) + l.kind.name.length) {
                    last = element
                    return@forEach
                }
            }
            if (last is FlashImage && element is PlainText) {
                if (element == UNSUPPORTED_FLASH_MESSAGE_PLAIN) {
                    last = element
                    return@forEach
                }
            }

            add(element)
            last = element
        }
    }
}

internal inline fun <reified R> Iterable<*>.firstIsInstanceOrNull(): R? {
    for (it in this) {
        if (it is R) {
            return it
        }
    }
    return null
}

internal val MIRAI_CUSTOM_ELEM_TYPE = "mirai".hashCode() // 103904510

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal fun List<ImMsgBody.Elem>.joinToMessageChain(groupIdOrZero: Long, bot: Bot, list: MessageChainBuilder) {
    // (this._miraiContentToString())
    this.forEach { element ->
        when {
            element.srcMsg != null -> {
                list.add(QuoteReply(OfflineMessageSourceImplBySourceMsg(element.srcMsg, bot, groupIdOrZero)))
            }
            element.notOnlineImage != null -> list.add(OnlineFriendImageImpl(element.notOnlineImage))
            element.customFace != null -> list.add(OnlineGroupImageImpl(element.customFace))
            element.face != null -> list.add(Face(element.face.index))
            element.text != null -> {
                if (element.text.attr6Buf.isEmpty()) {
                    list.add(element.text.str.toMessage())
                } else {
                    val id: Long
                    element.text.attr6Buf.read {
                        discardExact(7)
                        id = readUInt().toLong()
                    }
                    if (id == 0L) {
                        list.add(AtAll)
                    } else {
                        list.add(At._lowLevelConstructAtInstance(id, element.text.str))
                    }
                }
            }
            element.lightApp != null -> {
                val content = runWithBugReport("解析 lightApp",
                    { "resId=" + element.lightApp.msgResid + "data=" + element.lightApp.data.toUHexString() }) {
                    when (element.lightApp.data[0].toInt()) {
                        0 -> element.lightApp.data.encodeToString(offset = 1)
                        1 -> MiraiPlatformUtils.unzip(element.lightApp.data, 1).encodeToString()
                        else -> error("unknown compression flag=${element.lightApp.data[0]}")
                    }
                }
                list.add(LightApp(content))
            }
            element.richMsg != null -> {
                val content = runWithBugReport("解析 richMsg", { element.richMsg.template1.toUHexString() }) {
                    when (element.richMsg.template1[0].toInt()) {
                        0 -> element.richMsg.template1.encodeToString(offset = 1)
                        1 -> MiraiPlatformUtils.unzip(element.richMsg.template1, 1).encodeToString()
                        else -> error("unknown compression flag=${element.richMsg.template1[0]}")
                    }
                }
                when (element.richMsg.serviceId) {
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
                    list.add(ServiceMessage(1, content))
                    /**
                     * [LongMessage], [ForwardMessage]
                     */
                    35 -> {
                        val resId = this.firstIsInstanceOrNull<ImMsgBody.GeneralFlags>()?.longTextResid

                        if (resId != null) {
                            // TODO: 2020/4/29 解析长消息
                            list.add(ServiceMessage(35, content)) // resId
                        } else {
                            // TODO: 2020/4/29 解析合并转发
                            list.add(ServiceMessage(35, content))
                        }
                    }

                    // 104 新群员入群的消息
                    else -> {
                        if (element.richMsg.serviceId == 60 || content.startsWith("<?")) {
                            @Suppress("DEPRECATION_ERROR") // bin comp
                            list.add(ServiceMessage(element.richMsg.serviceId, content))
                        } else list.add(ServiceMessage(element.richMsg.serviceId, content))
                    }
                }
            }
            element.elemFlags2 != null
                    || element.extraInfo != null
                    || element.generalFlags != null -> {

            }
            element.customElem != null -> {
                element.customElem.data.read {
                    kotlin.runCatching {
                        CustomMessage.load(this)
                    }.fold(
                        onFailure = {
                            if (it is CustomMessage.Key.CustomMessageFullDataDeserializeInternalException) {
                                bot.logger.error(
                                    "Internal error: " +
                                            "exception while deserializing CustomMessage head data," +
                                            " data=${element.customElem.data.toUHexString()}", it
                                )
                            } else {
                                it as CustomMessage.Key.CustomMessageFullDataDeserializeUserException
                                bot.logger.error(
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
            element.commonElem != null -> {
                when (element.commonElem.serviceType) {
                    23 -> {
                        val proto = element.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype23.serializer())
                        list.add(VipFace(VipFace.Kind(proto.faceType, proto.faceSummary), proto.faceBubbleCount))
                    }
                    2 -> {
                        val proto = element.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype2.serializer())
                        list.add(PokeMessage(
                            proto.vaspokeName.takeIf { it.isNotEmpty() }
                                ?: PokeMessage.values.firstOrNull { it.id == proto.vaspokeId && it.type == proto.pokeType }?.name
                                    .orEmpty(),
                            proto.pokeType,
                            proto.vaspokeId
                        ))
                    }
                    3 -> {
                        val proto = element.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype3.serializer())
                        if (proto.flashTroopPic != null) {
                            list.add(GroupFlashImage(OnlineGroupImageImpl(proto.flashTroopPic)))
                        }
                        if (proto.flashC2cPic != null) {
                            list.add(FriendFlashImage(OnlineFriendImageImpl(proto.flashC2cPic)))
                        }
                    }
                }
            }
            else -> {
                // println(it._miraiContentToString())
            }
        }
    }

}


internal fun contextualBugReportException(
    context: String,
    forDebug: String,
    e: Throwable? = null,
    additional: String = ""
): IllegalStateException {
    return IllegalStateException("在 $context 时遇到了意料之中的问题. 请完整复制此日志提交给 mirai. $additional 调试信息: $forDebug", e)
}

@OptIn(ExperimentalContracts::class)
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
internal inline fun <R> runWithBugReport(context: String, forDebug: () -> String, block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        callsInPlace(forDebug, InvocationKind.AT_MOST_ONCE)
    }

    return runCatching(block).getOrElse {
        throw contextualBugReportException(context, forDebug(), it)
    }
}