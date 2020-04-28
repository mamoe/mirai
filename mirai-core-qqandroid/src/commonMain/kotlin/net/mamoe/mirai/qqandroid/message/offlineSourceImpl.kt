/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.internal.MiraiAtomicBoolean
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OfflineMessageSource
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs


internal class OfflineMessageSourceImplByMsg(
    // from other sources' originalMessage
    val delegate: MsgComm.Msg,
    override val bot: Bot
) : OfflineMessageSource(), MessageSourceInternal {
    override val kind: Kind = if (delegate.msgHead.groupInfo != null) Kind.GROUP else Kind.FRIEND
    override val id: Int get() = sequenceId
    override val internalId: Int
        get() = delegate.msgHead.msgUid.toInt()
    override val time: Int
        get() = delegate.msgHead.msgTime
    override val fromId: Long
        get() = delegate.msgHead.fromUin
    override val targetId: Long
        get() = delegate.msgHead.groupInfo?.groupCode ?: delegate.msgHead.toUin
    override val originalMessage: MessageChain by lazy {
        delegate.toMessageChain(bot,
            groupIdOrZero = delegate.msgHead.groupInfo?.groupCode ?: 0,
            onlineSource = false,
            isTemp = delegate.msgHead.c2cTmpMsgHead != null
        )
    }
    override val sequenceId: Int
        get() = delegate.msgHead.msgSeq

    override var isRecalledOrPlanned: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

    override fun toJceData(): ImMsgBody.SourceMsg {
        return ImMsgBody.SourceMsg(
            origSeqs = listOf(delegate.msgHead.msgSeq),
            senderUin = delegate.msgHead.fromUin,
            toUin = 0,
            flag = 1,
            elems = delegate.msgBody.richText.elems,
            type = 0,
            time = delegate.msgHead.msgTime,
            pbReserve = EMPTY_BYTE_ARRAY,
            srcMsg = EMPTY_BYTE_ARRAY
        )
    }
}

internal class OfflineMessageSourceImplBySourceMsg(
    // from others' quotation
    val delegate: ImMsgBody.SourceMsg,
    override val bot: Bot,
    groupIdOrZero: Long
) : OfflineMessageSource(), MessageSourceInternal {
    override val kind: Kind get() = if (delegate.srcMsg == null) Kind.GROUP else Kind.FRIEND

    override var isRecalledOrPlanned: MiraiAtomicBoolean = MiraiAtomicBoolean(false)
    override val sequenceId: Int
        get() = delegate.origSeqs?.first() ?: error("cannot find sequenceId")
    override val internalId: Int
        get() = delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer()).origUids?.toInt() ?: 0
    override val time: Int get() = delegate.time
    override val originalMessage: MessageChain by lazy { delegate.toMessageChain(bot, groupIdOrZero) }
    /*
    override val id: Long
        get() = (delegate.origSeqs?.firstOrNull()
            ?: error("cannot find sequenceId from ImMsgBody.SourceMsg")).toLong().shl(32) or
                delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer()).origUids!!.and(0xFFFFFFFF)
    */

    override val id: Int get() = sequenceId
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
