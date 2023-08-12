/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import io.ktor.utils.io.core.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.cleanupRubbishMessageElements
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.toAudio
import net.mamoe.mirai.internal.message.data.LongMessageInternal
import net.mamoe.mirai.internal.message.data.OnlineAudioImpl
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.protocol.impl.PokeMessageProtocol.Companion.UNSUPPORTED_POKE_MESSAGE_PLAIN
import net.mamoe.mirai.internal.message.protocol.impl.RichMessageProtocol.Companion.UNSUPPORTED_MERGED_MESSAGE_PLAIN
import net.mamoe.mirai.internal.message.source.*
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.castOrNull
import net.mamoe.mirai.utils.structureToString
import net.mamoe.mirai.utils.toLongUnsigned
import net.mamoe.mirai.utils.warning

/**
 * 只在手动构造 [OfflineMessageSource] 时调用
 */
internal fun ImMsgBody.SourceMsg.toMessageChainNoSource(
    bot: Bot,
    messageSourceKind: MessageSourceKind,
    groupIdOrZero: Long,
    refineContext: RefineContext = EmptyRefineContext,
    facade: MessageProtocolFacade = MessageProtocolFacade
): MessageChain {
    val elements = this.elems
    return buildMessageChain(elements.size + 1) {
        facade.decode(elements, groupIdOrZero, messageSourceKind, bot, this, null)
    }.cleanupRubbishMessageElements().refineLight(bot, refineContext)
}


internal suspend fun List<MsgComm.Msg>.toMessageChainOnline(
    bot: Bot,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind,
    refineContext: RefineContext = EmptyRefineContext,
    facade: MessageProtocolFacade = MessageProtocolFacade
): MessageChain {
    return toMessageChain(bot, groupIdOrZero, true, messageSourceKind, facade).refineDeep(bot, refineContext)
}

internal fun getMessageSourceKindFromC2cCmdOrNull(c2cCmd: Int): MessageSourceKind? {
    return when (c2cCmd) {
        11 -> MessageSourceKind.FRIEND // bot 给其他人发消息
        4 -> MessageSourceKind.FRIEND // bot 给自己作为好友发消息 (非 other client)
        1 -> MessageSourceKind.GROUP
        else -> null
    }
}

internal fun getMessageSourceKindFromC2cCmd(c2cCmd: Int): MessageSourceKind {
    return getMessageSourceKindFromC2cCmdOrNull(c2cCmd) ?: error("Could not get source kind from c2cCmd: $c2cCmd")
}


internal suspend fun MsgComm.Msg.toMessageChainOnline(
    bot: Bot,
    refineContext: RefineContext = EmptyRefineContext,
    facade: MessageProtocolFacade = MessageProtocolFacade,
): MessageChain {
    val kind = getMessageSourceKindFromC2cCmd(msgHead.c2cCmd)
    val groupId = when (kind) {
        MessageSourceKind.GROUP -> msgHead.groupInfo?.groupCode ?: 0
        else -> 0
    }

    return listOf(this).toMessageChainOnline(
        bot,
        groupId,
        kind,
        refineContext.merge(SimpleRefineContext(
            RefineContextKey.MessageSourceKind to kind,
            RefineContextKey.GroupIdOrZero to groupId
        ), false),
        facade
    )
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
    facade: MessageProtocolFacade = MessageProtocolFacade,
): MessageChain {
    try {
        return toMessageChainImpl(bot, groupIdOrZero, onlineSource, messageSourceKind, facade)
    } catch (e: Exception) {
        throw IllegalStateException(
            "Failed to transform internal message to facade message, msg=${this@toMessageChain.structureToString()}",
            e
        )
    }
}


private fun List<MsgComm.Msg>.toMessageChainImpl(
    bot: Bot,
    groupIdOrZero: Long,
    onlineSource: Boolean?,
    messageSourceKind: MessageSourceKind,
    facade: MessageProtocolFacade = MessageProtocolFacade,
): MessageChain {
    val messageList = this


    val builder = MessageChainBuilder(messageList.sumOf { it.msgBody.richText.elems.size })

    val source = if (onlineSource != null) {
        ReceiveMessageTransformer.createMessageSource(bot, onlineSource, messageSourceKind, messageList)
    } else null
    if (source != null) builder.add(source)

    val fromId = source?.fromId ?: firstOrNull()?.msgHead?.fromUin
    if (fromId == null) {
        bot.logger.warning {
            "Cannot determine fromId from message source and msg elements, " +
                    "source: $source, elements: ${this.joinToString(", ")}"
        }
    }

    messageList.forEach { msg ->
        facade.decode(
            msg.msgBody.richText.elems,
            groupIdOrZero,
            messageSourceKind,
            bot,
            builder,
            msg
        )
    }

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