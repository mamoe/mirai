/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OfflineMessageSource
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.mapToIntArray
import java.util.concurrent.atomic.AtomicBoolean

@Serializable(OfflineMessageSourceImplData.Serializer::class)
internal class OfflineMessageSourceImplData(
    override val kind: MessageSourceKind,
    override val ids: IntArray,
    override val botId: Long,
    override val time: Int,
    override val fromId: Long,
    override val targetId: Long,
    originalMessageLazy: Lazy<MessageChain>,
    override val internalIds: IntArray,
) : OfflineMessageSource(), MessageSourceInternal {
    object Serializer : MessageSourceSerializerImpl("OfflineMessageSource")

    override val sequenceIds: IntArray get() = ids
    override val originalMessage: MessageChain by originalMessageLazy

    // for override.
    // if provided, no need to serialize from message
    @Transient
    var originElems: List<ImMsgBody.Elem>? = null

    // may provided by OfflineMessageSourceImplBySourceMsg
    @Transient
    var jceData: ImMsgBody.SourceMsg? = null

    @Transient
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)

    override fun toJceData(): ImMsgBody.SourceMsg {
        return jceData ?: ImMsgBody.SourceMsg(
            origSeqs = sequenceIds,
            senderUin = fromId,
            toUin = 0,
            flag = 1,
            elems = originElems ?: originalMessage.toRichTextElems(
                null, //forGroup = kind == MessageSourceKind.GROUP,
                withGeneralFlags = false
            ),
            type = 0,
            time = time,
            pbReserve = EMPTY_BYTE_ARRAY,
            srcMsg = EMPTY_BYTE_ARRAY
        ).also { jceData = it }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineMessageSourceImplData

        val originElems = originElems
        if (originElems != null) {
            if (originElems == other.originElems) return true
        }

        if (kind != other.kind) return false
        if (!ids.contentEquals(other.ids)) return false
        if (botId != other.botId) return false
        if (time != other.time) return false
        if (fromId != other.fromId) return false
        if (targetId != other.targetId) return false
        if (originalMessage != other.originalMessage) return false
        if (!internalIds.contentEquals(other.internalIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + ids.contentHashCode()
        result = 31 * result + botId.hashCode()
        result = 31 * result + time
        result = 31 * result + fromId.hashCode()
        result = 31 * result + targetId.hashCode()
        result = 31 * result + originalMessage.hashCode()
        result = 31 * result + internalIds.contentHashCode()
        return result
    }
}

internal fun OfflineMessageSourceImplData(
    bot: Bot,
    delegate: List<MsgComm.Msg>,
    kind: MessageSourceKind
): OfflineMessageSourceImplData {
    val head = delegate.first().msgHead
    return OfflineMessageSourceImplData(
        kind = kind,
        time = head.msgTime,
        fromId = head.fromUin,
        targetId = head.groupInfo?.groupCode ?: head.toUin,
        originalMessage = delegate.toMessageChainNoSource(
            bot,
            groupIdOrZero = head.groupInfo?.groupCode ?: 0,
            messageSourceKind = kind
        ),
        ids = delegate.mapToIntArray { it.msgHead.msgSeq },
        internalIds = delegate.mapToIntArray { it.msgHead.msgUid.toInt() },
        botId = bot.id
    ).apply {
        originElems = delegate.flatMap { it.msgBody.richText.elems }
    }
}

internal fun OfflineMessageSourceImplData(
    kind: MessageSourceKind,
    ids: IntArray,
    botId: Long,
    time: Int,
    fromId: Long,
    targetId: Long,
    originalMessage: MessageChain,
    internalIds: IntArray,
): OfflineMessageSourceImplData = OfflineMessageSourceImplData(
    kind = kind,
    ids = ids,
    botId = botId,
    time = time,
    fromId = fromId,
    targetId = targetId,
    originalMessageLazy = lazyOf(originalMessage),
    internalIds = internalIds
)

internal fun OfflineMessageSourceImplData(
    delegate: ImMsgBody.SourceMsg,
    bot: Bot,
    messageSourceKind: MessageSourceKind,
    groupIdOrZero: Long,
): OfflineMessageSourceImplData {
    return OfflineMessageSourceImplData(
        kind = messageSourceKind,
        ids = delegate.origSeqs,
        internalIds = delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer())
            .origUids?.mapToIntArray { it.toInt() } ?: intArrayOf(),
        time = delegate.time,
        originalMessageLazy = lazy { delegate.toMessageChainNoSource(bot, messageSourceKind, groupIdOrZero) },
        fromId = delegate.senderUin,
        targetId = when {
            groupIdOrZero != 0L -> groupIdOrZero
            delegate.toUin != 0L -> delegate.toUin
            delegate.srcMsg != null -> delegate.srcMsg.loadAs(MsgComm.Msg.serializer()).msgHead.toUin
            else -> 0/*error("cannot find targetId. delegate=${delegate._miraiContentToString()}, delegate.srcMsg=${
            kotlin.runCatching { delegate.srcMsg?.loadAs(MsgComm.Msg.serializer())?._miraiContentToString() }
                .fold(
                    onFailure = { "<error: ${it.message}>" },
                    onSuccess = { it }
                )
            }"
            )*/
        },
        botId = bot.id
    ).apply {
        jceData = delegate
    }
}
