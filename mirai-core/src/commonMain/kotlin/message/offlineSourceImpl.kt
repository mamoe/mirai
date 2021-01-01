/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OfflineMessageSource
import net.mamoe.mirai.utils.mapToIntArray
import java.util.concurrent.atomic.AtomicBoolean

@Serializable
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
    override val sequenceIds: IntArray get() = ids

    @Transient
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)

    override fun toJceData(): ImMsgBody.SourceMsg {
        return ImMsgBody.SourceMsg(
            origSeqs = sequenceIds,
            senderUin = fromId,
            toUin = 0,
            flag = 1,
            elems = originalMessage.toRichTextElems(
                null, //forGroup = kind == MessageSourceKind.GROUP,
                withGeneralFlags = false
            ),
            type = 0,
            time = time,
            pbReserve = net.mamoe.mirai.internal.EMPTY_BYTE_ARRAY,
            srcMsg = net.mamoe.mirai.internal.EMPTY_BYTE_ARRAY
        )
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

internal class OfflineMessageSourceImplByMsg(
    // from other sources' originalMessage
    bot: Bot?,
    val delegate: List<MsgComm.Msg>,
    override val botId: Long,
) : OfflineMessageSource(), MessageSourceInternal {
    override val kind: MessageSourceKind =
        when {
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
    override val ids: IntArray get() = sequenceIds
    override val internalIds: IntArray = delegate.mapToIntArray { it.msgHead.msgUid.toInt() }
    override val time: Int
        get() = delegate.first().msgHead.msgTime
    override val fromId: Long
        get() = delegate.first().msgHead.fromUin
    override val targetId: Long
        get() = delegate.first().msgHead.groupInfo?.groupCode ?: delegate.first().msgHead.toUin
    override val originalMessage: MessageChain by lazy {
        delegate.toMessageChain(
            null,
            botId,
            groupIdOrZero = delegate.first().msgHead.groupInfo?.groupCode ?: 0,
            onlineSource = false,
            messageSourceKind = kind
        )
    }
    override val sequenceIds: IntArray = delegate.mapToIntArray { it.msgHead.msgSeq }

    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)

    override fun toJceData(): ImMsgBody.SourceMsg {
        return ImMsgBody.SourceMsg(
            origSeqs = delegate.mapToIntArray { it.msgHead.msgSeq },
            senderUin = delegate.first().msgHead.fromUin,
            toUin = 0,
            flag = 1,
            elems = delegate.flatMap { it.msgBody.richText.elems },
            type = 0,
            time = delegate.first().msgHead.msgTime,
            pbReserve = EMPTY_BYTE_ARRAY,
            srcMsg = EMPTY_BYTE_ARRAY
        )
    }
}

internal class OfflineMessageSourceImplBySourceMsg(
    // from others' quotation
    val delegate: ImMsgBody.SourceMsg,
    override val botId: Long,
    groupIdOrZero: Long
) : OfflineMessageSource(), MessageSourceInternal {
    override val kind: MessageSourceKind get() = if (delegate.srcMsg == null) MessageSourceKind.GROUP else MessageSourceKind.FRIEND

    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val sequenceIds: IntArray = delegate.origSeqs
    override val internalIds: IntArray = delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer())
        .origUids?.mapToIntArray { it.toInt() } ?: intArrayOf()
    override val time: Int get() = delegate.time
    override val originalMessage: MessageChain by lazy { delegate.toMessageChain(botId, groupIdOrZero) }
    /*
    override val ids: Long
        get() = (delegate.origSeqs?.firstOrNull()
            ?: error("cannot find sequenceId from ImMsgBody.SourceMsg")).toLong().shl(32) or
                delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer()).origUids!!.and(0xFFFFFFFF)
    */

    override val ids: IntArray get() = sequenceIds
    // delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer()).origUids?.toInt()
    // ?: 0

    // override val sourceMessage: MessageChain get() = delegate.toMessageChain()
    override val fromId: Long get() = delegate.senderUin
    override val targetId: Long by lazy {
        when {
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
        }
    }

    override fun toJceData(): ImMsgBody.SourceMsg {
        return delegate
    }
}
