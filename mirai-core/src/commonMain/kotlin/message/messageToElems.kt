/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.safeCast
import net.mamoe.mirai.utils.zip

internal val MIRAI_CUSTOM_ELEM_TYPE = "mirai".hashCode() // 103904510


internal val UNSUPPORTED_MERGED_MESSAGE_PLAIN = PlainText("你的QQ暂不支持查看[转发多条消息]，请期待后续版本。")
internal val UNSUPPORTED_POKE_MESSAGE_PLAIN = PlainText("[戳一戳]请使用最新版手机QQ体验新功能。")
internal val UNSUPPORTED_FLASH_MESSAGE_PLAIN = PlainText("[闪照]请使用新版手机QQ查看闪照。")
internal val UNSUPPORTED_VOICE_MESSAGE_PLAIN = PlainText("收到语音消息，你需要升级到最新版QQ才能接收，升级地址https://im.qq.com")

@OptIn(ExperimentalStdlibApi::class)
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun MessageChain.toRichTextElems(
    messageTarget: ContactOrBot?,
    withGeneralFlags: Boolean,
    isForward: Boolean = false,
): MutableList<ImMsgBody.Elem> {
    val forGroup = messageTarget is Group
    val elements = ArrayList<ImMsgBody.Elem>(this.size)

    if (this.anyIsInstance<QuoteReply>()) {
        when (val source = this[QuoteReply]!!.source) {
            is MessageSourceInternal -> elements.add(ImMsgBody.Elem(srcMsg = source.toJceData()))
            else -> error("unsupported MessageSource implementation: ${source::class.simpleName}. Don't implement your own MessageSource.")
        }
    }

    var longTextResId: String? = null

    fun transformOneMessage(currentMessage: Message) {
        if (currentMessage is RichMessage) {
            val content = currentMessage.content.toByteArray().zip()
            when (currentMessage) {
                is ForwardMessageInternal -> {
                    elements.add(
                        ImMsgBody.Elem(
                            richMsg = ImMsgBody.RichMsg(
                                serviceId = currentMessage.serviceId, // ok
                                template1 = byteArrayOf(1) + content
                            )
                        )
                    )
                    // transformOneMessage(UNSUPPORTED_MERGED_MESSAGE_PLAIN)
                }
                is LongMessageInternal -> {
                    check(longTextResId == null) { "There must be no more than one LongMessage element in the message chain" }
                    elements.add(
                        ImMsgBody.Elem(
                            richMsg = ImMsgBody.RichMsg(
                                serviceId = currentMessage.serviceId, // ok
                                template1 = byteArrayOf(1) + content
                            )
                        )
                    )
                    transformOneMessage(UNSUPPORTED_MERGED_MESSAGE_PLAIN)
                    longTextResId = currentMessage.resId
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
                            serviceId = when (currentMessage) {
                                is ServiceMessage -> currentMessage.serviceId
                                else -> error("unsupported RichMessage: ${currentMessage::class.simpleName}")
                            },
                            template1 = byteArrayOf(1) + content
                        )
                    )
                )
            }
        }

        when (currentMessage) {
            is PlainText -> elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = currentMessage.content)))
            is CustomMessage -> {
                @Suppress("UNCHECKED_CAST")
                elements.add(
                    ImMsgBody.Elem(
                        customElem = ImMsgBody.CustomElem(
                            enumType = MIRAI_CUSTOM_ELEM_TYPE,
                            data = CustomMessage.dump(
                                currentMessage.getFactory() as CustomMessage.Factory<CustomMessage>,
                                currentMessage
                            )
                        )
                    )
                )
            }
            is At -> {
                elements.add(
                    ImMsgBody.Elem(
                        text = currentMessage.toJceData(
                            messageTarget.safeCast(),
                            this[MessageSource],
                            isForward,
                        )
                    )
                )
                // elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = " ")))
                // removed by https://github.com/mamoe/mirai/issues/524
                // 发送 QuoteReply 消息时无可避免的产生多余空格 #524
            }
            is PokeMessage -> {
                elements.add(
                    ImMsgBody.Elem(
                        commonElem = ImMsgBody.CommonElem(
                            serviceType = 2,
                            businessType = currentMessage.pokeType,
                            pbElem = HummerCommelem.MsgElemInfoServtype2(
                                pokeType = currentMessage.pokeType,
                                vaspokeId = currentMessage.id,
                                vaspokeMinver = "7.2.0",
                                vaspokeName = currentMessage.name
                            ).toByteArray(HummerCommelem.MsgElemInfoServtype2.serializer())
                        )
                    )
                )
                transformOneMessage(UNSUPPORTED_POKE_MESSAGE_PLAIN)
            }


            is OfflineGroupImage -> {
                if (messageTarget is User) {
                    elements.add(ImMsgBody.Elem(notOnlineImage = currentMessage.toJceData().toNotOnlineImage()))
                } else {
                    elements.add(ImMsgBody.Elem(customFace = currentMessage.toJceData()))
                }
            }
            is OnlineGroupImageImpl -> {
                if (messageTarget is User) {
                    elements.add(ImMsgBody.Elem(notOnlineImage = currentMessage.delegate.toNotOnlineImage()))
                } else {
                    elements.add(ImMsgBody.Elem(customFace = currentMessage.delegate))
                }
            }
            is OnlineFriendImageImpl -> {
                if (messageTarget is User) {
                    elements.add(ImMsgBody.Elem(notOnlineImage = currentMessage.delegate))
                } else {
                    elements.add(ImMsgBody.Elem(customFace = currentMessage.delegate.toCustomFace()))
                }
            }
            is OfflineFriendImage -> {
                if (messageTarget is User) {
                    elements.add(ImMsgBody.Elem(notOnlineImage = currentMessage.toJceData()))
                } else {
                    elements.add(ImMsgBody.Elem(customFace = currentMessage.toJceData().toCustomFace()))
                }
            }


            is FlashImage -> elements.add(currentMessage.toJceData(messageTarget))
                .also { transformOneMessage(UNSUPPORTED_FLASH_MESSAGE_PLAIN) }


            is AtAll -> elements.add(atAllData)
            is Face -> elements.add(
                if (currentMessage.id >= 260) {
                    ImMsgBody.Elem(commonElem = currentMessage.toCommData())
                } else {
                    ImMsgBody.Elem(face = currentMessage.toJceData())
                }
            )
            is QuoteReply -> {
                if (forGroup) {
                    when (val source = currentMessage.source) {
                        is OnlineMessageSource.Incoming.FromGroup -> {
                            val sender0 = source.sender
                            if (sender0 !is AnonymousMember)
                                transformOneMessage(At(sender0))
                            // transformOneMessage(PlainText(" "))
                            // removed by https://github.com/mamoe/mirai/issues/524
                            // 发送 QuoteReply 消息时无可避免的产生多余空格 #524
                        }
                    }
                }
            }
            is Dice -> transformOneMessage(MarketFaceImpl(currentMessage.toJceStruct()))
            is MarketFace -> {
                if (currentMessage is MarketFaceImpl) {
                    elements.add(ImMsgBody.Elem(marketFace = currentMessage.delegate))
                }
                //兼容信息
                transformOneMessage(PlainText(currentMessage.name))
                if (currentMessage is MarketFaceImpl) {
                    elements.add(
                        ImMsgBody.Elem(
                            extraInfo = ImMsgBody.ExtraInfo(flags = 8, groupMask = 1)
                        )
                    )
                }
            }
            is VipFace -> transformOneMessage(PlainText(currentMessage.contentToString()))
            is PttMessage -> {
                elements.add(
                    ImMsgBody.Elem(
                        extraInfo = ImMsgBody.ExtraInfo(flags = 16, groupMask = 1)
                    )
                )
                elements.add(
                    ImMsgBody.Elem(
                        elemFlags2 = ImMsgBody.ElemFlags2(
                            vipStatus = 1
                        )
                    )
                )
            }
            is MusicShare -> {
                // 只有在 QuoteReply 的 source 里才会进行 MusicShare 转换, 因此可以转 PT.
                // 发送消息时会被特殊处理
                transformOneMessage(PlainText(currentMessage.content))
            }

            is ForwardMessage,
            is MessageSource, // mirai metadata only
            is RichMessage // already transformed above
            -> {

            }
            is InternalFlagOnlyMessage, is ShowImageFlag -> {
                // ignore
            }
            is UnsupportedMessageImpl -> elements.add(currentMessage.structElem)
            else -> {
                // unrecognized types are ignored
                // error("unsupported message type: ${currentMessage::class.simpleName}")
            }
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
            this.anyIsInstance<MarketFaceImpl>() -> {
                elements.add(ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_MARKET_FACE)))
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

internal val PB_RESERVE_FOR_RICH_MESSAGE =
    "08 09 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00".hexToBytes()

internal val PB_RESERVE_FOR_PTT =
    "78 00 F8 01 00 C8 02 00 AA 03 26 08 22 12 22 41 20 41 3B 25 3E 16 45 3F 43 2F 29 3E 44 24 14 18 46 3D 2B 4A 44 3A 18 2E 19 29 1B 26 32 31 31 29 43".hexToBytes()

@Suppress("SpellCheckingInspection")
internal val PB_RESERVE_FOR_DOUTU = "78 00 90 01 01 F8 01 00 A0 02 00 C8 02 00".hexToBytes()
internal val PB_RESERVE_FOR_MARKET_FACE =
    "02 78 80 80 04 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 00 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 04 08 02 10 3B 90 04 80 C0 80 80 04 B8 04 00 C0 04 00 CA 04 00 F8 04 80 80 04 88 05 00".hexToBytes()
internal val PB_RESERVE_FOR_ELSE = "78 00 F8 01 00 C8 02 00".hexToBytes()
