/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file: OptIn(MiraiExperimentalAPI::class, MiraiInternalAPI::class, LowLevelAPI::class, ExperimentalUnsignedTypes::class)
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.qqandroid.message

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
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


private val UNSUPPORTED_MERGED_MESSAGE_PLAIN = PlainText("你的QQ暂不支持查看[转发多条消息]，请期待后续版本。")
private val UNSUPPORTED_POKE_MESSAGE_PLAIN = PlainText("[戳一戳]请使用最新版手机QQ体验新功能。")
private val UNSUPPORTED_FLASH_MESSAGE_PLAIN = PlainText("[闪照]请使用新版手机QQ查看闪照。")

@OptIn(MiraiInternalAPI::class, MiraiExperimentalAPI::class)
internal fun MessageChain.toRichTextElems(forGroup: Boolean, withGeneralFlags: Boolean): MutableList<ImMsgBody.Elem> {
    val elements = mutableListOf<ImMsgBody.Elem>()

    if (this.anyIsInstance<QuoteReply>()) {
        when (val source = this[QuoteReply].source) {
            is MessageSourceImpl -> elements.add(ImMsgBody.Elem(srcMsg = source.toJceData()))
            else -> error("unsupported MessageSource implementation: ${source::class.simpleName}. Don't implement your own MessageSource.")
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
            is PokeMessage -> {
                elements.add(
                    ImMsgBody.Elem(
                        commonElem = ImMsgBody.CommonElem(
                            serviceType = 2,
                            businessType = it.type,
                            pbElem = HummerCommelem.MsgElemInfoServtype2(
                                pokeType = it.type,
                                vaspokeId = it.id
                            ).toByteArray(HummerCommelem.MsgElemInfoServtype2.serializer())
                        )
                    )
                )
                transformOneMessage(UNSUPPORTED_POKE_MESSAGE_PLAIN)
            }
            is OfflineGroupImage -> elements.add(ImMsgBody.Elem(customFace = it.toJceData()))
            is OnlineGroupImageImpl -> elements.add(ImMsgBody.Elem(customFace = it.delegate))
            is OnlineFriendImageImpl -> elements.add(ImMsgBody.Elem(notOnlineImage = it.delegate))
            is OfflineFriendImage -> elements.add(ImMsgBody.Elem(notOnlineImage = it.toJceData()))
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
            else -> elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_ELSE)))
        }
    }

    return elements
}

private val PB_RESERVE_FOR_RICH_MESSAGE =
    "08 09 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00".hexToBytes()

@Suppress("SpellCheckingInspection")
private val PB_RESERVE_FOR_DOUTU = "78 00 90 01 01 F8 01 00 A0 02 00 C8 02 00".hexToBytes()
private val PB_RESERVE_FOR_ELSE = "78 00 F8 01 00 C8 02 00".hexToBytes()

@OptIn(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
internal fun MsgComm.Msg.toMessageChain(
    bot: Bot,
    groupIdOrZero: Long,
    onlineSource: Boolean,
    isTemp: Boolean = false
): MessageChain {
    val elements = this.msgBody.richText.elems

    return buildMessageChain(elements.size + 1) {
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
    }.cleanupRubbishMessageElements()
}

// These two functions have difference method signature, don't combine.

@OptIn(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
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

@OptIn(MiraiInternalAPI::class, LowLevelAPI::class)
internal fun List<ImMsgBody.Elem>.joinToMessageChain(groupIdOrZero: Long, bot: Bot, message: MessageChainBuilder) {
    // (this._miraiContentToString())
    this.forEach { element ->
        when {
            element.srcMsg != null ->
                message.add(QuoteReply(OfflineMessageSourceImplBySourceMsg(element.srcMsg, bot, groupIdOrZero)))
            element.notOnlineImage != null -> message.add(OnlineFriendImageImpl(element.notOnlineImage))
            element.customFace != null -> message.add(OnlineGroupImageImpl(element.customFace))
            element.face != null -> message.add(Face(element.face.index))
            element.text != null -> {
                if (element.text.attr6Buf.isEmpty()) {
                    message.add(element.text.str.toMessage())
                } else {
                    val id: Long
                    element.text.attr6Buf.read {
                        discardExact(7)
                        id = readUInt().toLong()
                    }
                    if (id == 0L) {
                        message.add(AtAll)
                    } else {
                        message.add(At._lowLevelConstructAtInstance(id, element.text.str))
                    }
                }
            }
            element.lightApp != null -> {
                val content = runWithBugReport("解析 lightApp", { element.lightApp.data.toUHexString() }) {
                    MiraiPlatformUtils.unzip(element.lightApp.data, 1).encodeToString()
                }
                message.add(LightApp(content))
            }
            element.richMsg != null -> {
                val content = runWithBugReport("解析 richMsg", { element.richMsg.template1.toUHexString() }) {
                    MiraiPlatformUtils.unzip(element.richMsg.template1, 1).encodeToString()
                }
                when (element.richMsg.serviceId) {
                    1 -> message.add(JsonMessage(content))
                    60 -> message.add(XmlMessage(content))
                    35 -> {
                        /*
                        Mirai 19:09:29 : cannot find longTextResid. isGroup=false elems=
[Elem#2041089331 {
            richMsg=RichMsg#1822931063 {
                    flags=0x00000000(0)
                    msgResid=<Empty ByteArray>
                    rand=0x00000000(0)
                    seq=0x00000000(0)
                    serviceId=0x00000023(35)
                    template1=01 78 9C A5 91 C1 6E D3 40 14 45 7F E5 69 36 5E 15 DB D8 A6 11 B2 5D 51 85 8A 22 05 55 0D 4D 52 AA 0A 4D EC 17 67 C8 8C 1D 66 C6 29 59 76 05 B4 8B AA 8B 6E 0A 08 21 95 45 A5 0A 58 B1 2B 7F E3 34 9F C1 D8 01 F1 01 EC 66 E6 8E DE 3B F7 DE 70 E3 8D E0 30 43 A9 58 91 47 96 7B CF B1 00 F3 A4 48 59 9E 45 D6 DE F3 AD B5 96 05 4A D3 3C A5 BC C8 31 B2 E6 A8 2C D8 88 43 A1 32 50 28 67 2C C1 ED 76 44 BC 80 80 46 31 E5 54 37 77 97 00 4D 74 3D 93 CC 18 1E 75 4A AE 59 47 65 04 86 92 E1 28 22 07 CB DB 9B EA EC BC BA BA 5C 7C FA B2 F8 F9 6E 71 FC FD 90 80 78 29 51 B1 34 22 FB 03 87 1F 15 4F 53 D5 DF 79 D1 6B 0F 86 F7 BB 3D DE F7 65 87 ED 04 FE AB 5E 2B 79 3D 50 E5 7E 32 7C B2 97 EC 6E E2 44 79 8F C4 66 B7 3D 9E 60 DF 5F 0F 1E 7B C3 71 3D 6A C4 38 3E A3 02 23 F2 A0 E5 FA 8E D7 0A 5C C7 0B 9C 75 3F F0 3D 8F 80 2A 4A 99 A0 61 DA 36 FB 1C 02 A5 E4 11 21 30 E2 34 33 76 0C 7E 6A 52 E9 B2 2C 6F 54 F1 C7 C0 56 23 3B 24 0E 99 B1 0B 9C CE 8B 52 D7 76 E3 50 33 CD 31 9E 14 05 CA 87 00 07 D5 87 5F 77 EF DF 1E 86 F6 EA FD 3F E5 B1 84 31 4B 53 34 34 23 CA 15 1A 7E 3D E7 D8 B0 D9 71 A8 4A 21 A8 9C C7 8B CF 5F EF 3E 9E FA 26 D3 55 BE AB 64 43 FB AF 1E DA 35 B6 F9 DF 98 87 BC 89 67 79 7C 52 5D 5D 2F BF FD A8 6E 2F 08 B0 A4 2E ED 5F 7D E6 34 9D D6 9D AC B9 CD 2A DB 34 1F FF 06 A5 57 D1 F0
            }
    }, Elem#2041089331 {
            text=Text#677417722 {
                    attr6Buf=<Empty ByteArray>
                    attr7Buf=<Empty ByteArray>
                    buf=<Empty ByteArray>
                    link=
                    pbReserve=<Empty ByteArray>
                    str=你的TIM暂不支持查看[转发多条消息]，请期待后续版本。
            }
    }, Elem#2041089331 {
            elemFlags2=ElemFlags2#1722087666 {
                    colorTextId=0x00000000(0)
                    compatibleId=0x00000000(0)
                    crmFlags=0x00000000(0)
                    customFont=0x00000000(0)
                    latitude=0x00000000(0)
                    longtitude=0x00000000(0)
                    msgId=0x0000000000000000(0)
                    msgRptCnt=0x00000000(0)
                    pttChangeBit=0x00000000(0)
                    vipStatus=0x00000000(0)
                    whisperSessionId=0x00000000(0)
            }
    }, Elem#2041089331 {
            generalFlags=GeneralFlags#707534137 {
                    babyqGuideMsgCookie=<Empty ByteArray>
                    bubbleDiyTextId=0x00000000(0)
                    bubbleSubId=0x00000000(0)
                    glamourLevel=0x00000002(2)
                    groupFlagNew=0x00000000(0)
                    groupRankSeq=0x0000000000000000(0)
                    groupType=0x00000000(0)
                    longTextFlag=0x00000000(0)
                    longTextResid=
                    memberLevel=0x00000000(0)
                    olympicTorch=0x00000000(0)
                    pbReserve=08 08 20 BF 50 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 09 B8 04 00 C0 04 00
                    pendantId=0x0000000000000000(0)
                    prpFold=0x00000000(0)
                    rpId=<Empty ByteArray>
                    rpIndex=<Empty ByteArray>
                    toUinFlag=0x00000000(0)
                    uin=0x0000000000000000(0)
                    uin32ExpertFlag=0x00000000(0)
            }
    }, Elem#2041089331 {
            extraInfo=ExtraInfo#913448337 {
                    apnsSoundType=0x00000000(0)
                    apnsTips=<Empty ByteArray>
                    flags=0x00000008(8)
                    groupCard=E3 81 82 E3 81 BE E3 81 A4 E6 A7 98 E5 8D 95 E6 8E A8 E4 BA BA EF BC 88 E4 B8 93 E4 B8 9A E6 8F 92 E7 94 BB E5 B8 88 EF BC 89
                    groupMask=0x00000001(1)
                    level=0x00000004(4)
                    msgStateFlag=0x00000000(0)
                    msgTailId=0x00000000(0)
                    newGroupFlag=0x00000000(0)
                    nick=<Empty ByteArray>
                    senderTitle=<Empty ByteArray>
                    uin=0x0000000000000000(0)
            }
    }]
                         */
                        val resId = this.firstIsInstanceOrNull<ImMsgBody.GeneralFlags>()?.longTextResid

                        if (resId != null) {
                            message.add(LongMessage(content, resId))
                        } else {
                            message.add(ForwardMessage(content))
                        }
                    }
                    else -> {
                        throw contextualBugReportException("richMsg.serviceId",
                            "richMsg.serviceId: ${element.richMsg.serviceId}, content=${element.richMsg.template1.contentToString()}, \n" + "tryUnzip=${content}")
                    }
                }
            }
            element.elemFlags2 != null
                    || element.extraInfo != null
                    || element.generalFlags != null -> {

            }
            element.commonElem != null -> {
                when (element.commonElem.serviceType) {
                    2 -> {
                        val proto = element.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype2.serializer())
                        message.add(PokeMessage(proto.pokeType, proto.vaspokeId))
                    }
                    3 -> {
                        val proto = element.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype3.serializer())
                        if (proto.flashTroopPic != null) {
                            message.add(GroupFlashImage(OnlineGroupImageImpl(proto.flashTroopPic)))
                        }
                        if (proto.flashC2cPic != null) {
                            message.add(FriendFlashImage(OnlineFriendImageImpl(proto.flashC2cPic)))
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
    e: Throwable? = null
): IllegalStateException {
    return IllegalStateException("在 $context 时遇到了意料之中的问题. 请完整复制此日志提交给 mirai. 调试信息: $forDebug", e)
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