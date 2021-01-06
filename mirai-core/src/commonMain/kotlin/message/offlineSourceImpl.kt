/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

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
import net.mamoe.mirai.utils.mapToIntArray
import java.util.concurrent.atomic.AtomicBoolean

@Serializable(OfflineMessageSourceImplData.Serializer::class)
internal data class OfflineMessageSourceImplData(
    override val kind: MessageSourceKind,
    override val ids: IntArray,
    override val botId: Long,
    override val time: Int,
    override val fromId: Long,
    override val targetId: Long,
    override val originalMessage: MessageChain,
    override val internalIds: IntArray,
) : OfflineMessageSource(), MessageSourceInternal {
    object Serializer : MessageSourceSerializerImpl("OfflineMessageSource")

    override val sequenceIds: IntArray get() = ids

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
            pbReserve = net.mamoe.mirai.internal.EMPTY_BYTE_ARRAY,
            srcMsg = net.mamoe.mirai.internal.EMPTY_BYTE_ARRAY
        ).also { jceData = it }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineMessageSourceImplData

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
    bot: Bot?,
    delegate: List<MsgComm.Msg>,
    botId: Long,
): OfflineMessageSourceImplData {
    val kind = when {
        delegate.first().msgHead.groupInfo != null -> {
            MessageSourceKind.GROUP
        }
        delegate.first().msgHead.c2cTmpMsgHead != null -> {
            MessageSourceKind.TEMP
        }
        bot?.getStranger(delegate.first().msgHead.fromUin) != null -> {
            MessageSourceKind.STRANGER
        }
        else -> {
            MessageSourceKind.FRIEND
        }
    }
    return OfflineMessageSourceImplData(
        kind = kind,
        time = delegate.first().msgHead.msgTime,
        fromId = delegate.first().msgHead.fromUin,
        targetId = delegate.first().msgHead.groupInfo?.groupCode ?: delegate.first().msgHead.toUin,
        originalMessage = delegate.toMessageChain(
            null,
            botId,
            groupIdOrZero = delegate.first().msgHead.groupInfo?.groupCode ?: 0,
            onlineSource = false,
            messageSourceKind = kind
        ),
        ids = delegate.mapToIntArray { it.msgHead.msgSeq },
        internalIds = delegate.mapToIntArray { it.msgHead.msgUid.toInt() },
        botId = botId
    ).apply {
        originElems = delegate.flatMap { it.msgBody.richText.elems }
    }
}


internal fun OfflineMessageSourceImplData(
    delegate: ImMsgBody.SourceMsg,
    botId: Long,
    groupIdOrZero: Long,
): OfflineMessageSourceImplData {
    return OfflineMessageSourceImplData(
        kind = if (delegate.srcMsg == null) MessageSourceKind.GROUP else MessageSourceKind.FRIEND,
        ids = delegate.origSeqs,
        internalIds = delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer())
            .origUids?.mapToIntArray { it.toInt() } ?: intArrayOf(),
        time = delegate.time,
        originalMessage = delegate.toMessageChain(botId, groupIdOrZero),
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
        botId = botId
    ).apply {
        jceData = delegate
    }
}
