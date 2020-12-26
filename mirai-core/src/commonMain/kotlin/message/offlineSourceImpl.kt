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


internal class OfflineMessageSourceImplByMsg(
    // from other sources' originalMessage
    val delegate: List<MsgComm.Msg>,
    override val botId: Long,
) : OfflineMessageSource(), MessageSourceInternal {
    override val kind: MessageSourceKind =
        if (delegate.first().msgHead.groupInfo != null) MessageSourceKind.GROUP else MessageSourceKind.FRIEND
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
            isTemp = delegate.first().msgHead.c2cTmpMsgHead != null
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
