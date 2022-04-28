/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.internal.message.data.UnsupportedMessageImpl
import net.mamoe.mirai.internal.message.flags.InternalFlagOnlyMessage
import net.mamoe.mirai.internal.message.image.OfflineFriendImage
import net.mamoe.mirai.internal.message.image.OfflineGroupImage
import net.mamoe.mirai.internal.message.image.OnlineFriendImageImpl
import net.mamoe.mirai.internal.message.image.OnlineGroupImageImpl
import net.mamoe.mirai.internal.message.source.MessageSourceInternal
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.hexToBytes

internal val MIRAI_CUSTOM_ELEM_TYPE = "mirai".hashCode() // 103904510


internal val UNSUPPORTED_MERGED_MESSAGE_PLAIN = PlainText("你的QQ暂不支持查看[转发多条消息]，请期待后续版本。")
internal val UNSUPPORTED_POKE_MESSAGE_PLAIN = PlainText("[戳一戳]请使用最新版手机QQ体验新功能。")
internal val UNSUPPORTED_FLASH_MESSAGE_PLAIN = PlainText("[闪照]请使用新版手机QQ查看闪照。")
internal val UNSUPPORTED_VOICE_MESSAGE_PLAIN = PlainText("收到语音消息，你需要升级到最新版QQ才能接收，升级地址https://im.qq.com")

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun MessageChain.toRichTextElems(
    messageTarget: ContactOrBot?,
    withGeneralFlags: Boolean,
    isForward: Boolean = false,
): MutableList<ImMsgBody.Elem> {
    val forGroup = messageTarget is Group
    val elements = ArrayList<ImMsgBody.Elem>(this.size)

    var longTextResId: String? = null

    fun transformOneMessage(currentMessage: Message) {
        if (currentMessage is RichMessage) {
            // removed
        }

        when (currentMessage) {
            is PlainText -> {
                // removed
            }
            is CustomMessage -> {
                // removed
            }
            is At -> {
                // removed
            }
            is PokeMessage -> {
                // removed
            }


            is OfflineGroupImage -> {
                // removed
            }
            is OnlineGroupImageImpl -> {
                // removed
            }
            is OnlineFriendImageImpl -> {
                // removed
            }
            is OfflineFriendImage -> {
                // removed
            }


            is FlashImage -> {
                // removed
            }

            is AtAll -> {
                // removed
            }
            is Face -> {
                // removed
            }
            is QuoteReply -> { // transformed
            }
            is Dice -> {
                // removed
            }
            is MarketFace -> {
                // removed
            }
            is VipFace -> {
                // removed
            }
            is PttMessage -> {
                // removed
            }
            is MusicShare -> {
                // removed
            }

            is ForwardMessage,
            is MessageSource, // mirai metadata only
            is RichMessage, // already transformed above
            -> {

            }
            is InternalFlagOnlyMessage, is ShowImageFlag -> {
                // ignore
            }
            is UnsupportedMessageImpl -> {
                // removed
            }
            else -> {
                // unrecognized types are ignored
                // error("unsupported message type: ${currentMessage::class.simpleName}")
            }
        }
    }

    if (this.anyIsInstance<QuoteReply>()) {
        when (val source = this[QuoteReply]!!.source) {
            is MessageSourceInternal -> {
                elements.add(ImMsgBody.Elem(srcMsg = source.toJceData()))
                if (forGroup) {
                    if (source is OnlineMessageSource.Incoming.FromGroup) {
                        val sender0 = source.sender
                        if (sender0 !is AnonymousMember)
                            transformOneMessage(At(sender0))
                        // transformOneMessage(PlainText(" "))
                        // removed by https://github.com/mamoe/mirai/issues/524
                        // 发送 QuoteReply 消息时无可避免的产生多余空格 #524
                    }
                }
            }
            else -> error("unsupported MessageSource implementation: ${source::class.simpleName}. Don't implement your own MessageSource.")
        }
    }

    this.forEach(::transformOneMessage)

    if (withGeneralFlags) {
        when {
            longTextResId != null -> {
                // removed
            }
            this.anyIsInstance<MarketFaceImpl>() -> {
                // removed
            }
            this.anyIsInstance<RichMessage>() -> {
                // removed
            }
            this.anyIsInstance<FlashImage>() -> {
                // removed
            }
            this.anyIsInstance<PttMessage>() -> {
                // removed
            }
            else -> {
                // removed
            }
        }
    }

    return elements
}

@Suppress("SpellCheckingInspection")
internal val PB_RESERVE_FOR_ELSE = "78 00 F8 01 00 C8 02 00".hexToBytes()
