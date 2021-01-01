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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.asyncFromEventOrNull
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.OnlinePushPbPushGroupMsg.SendGroupMessageReceipt
import net.mamoe.mirai.internal.network.protocol.packet.chat.toLongUnsigned
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import java.util.concurrent.atomic.AtomicBoolean


private fun <T> T.toJceDataImpl(subject: ContactOrBot?): ImMsgBody.SourceMsg
        where T : MessageSourceInternal, T : MessageSource {

    val elements = originalMessage.toRichTextElems(subject, withGeneralFlags = true)

    val pdReserve = SourceMsg.ResvAttr(
        origUids = sequenceIds.zip(internalIds)
            .map { (seq, internal) -> seq.toLong().shl(32) or internal.toLongUnsigned() }
    )

    return ImMsgBody.SourceMsg(
        origSeqs = sequenceIds,
        senderUin = fromId,
        toUin = targetId,
        flag = 1,
        elems = elements,
        type = 0,
        time = time,
        pbReserve = pdReserve.toByteArray(SourceMsg.ResvAttr.serializer()),
        srcMsg = MsgComm.Msg(
            msgHead = MsgComm.MsgHead(
                fromUin = fromId, // qq
                toUin = targetId, // group
                msgType = 9, // 82?
                c2cCmd = 11,
                msgSeq = sequenceIds.single(), // TODO !!
                msgTime = time,
                msgUid = pdReserve.origUids!!.single(), // TODO !!
                // groupInfo = MsgComm.GroupInfo(groupCode = delegate.msgHead.groupInfo.groupCode),
                isSrcMsg = true
            ),
            msgBody = ImMsgBody.MsgBody(
                richText = ImMsgBody.RichText(
                    elems = elements.toMutableList().also {
                        if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
                    }
                )
            )
        ).toByteArray(MsgComm.Msg.serializer())
    )
}

internal class MessageSourceToFriendImpl(
    override val sequenceIds: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val sender: Bot,
    override val target: Friend
) : OnlineMessageSource.Outgoing.ToFriend(), MessageSourceInternal {
    override val bot: Bot
        get() = sender
    override val ids: IntArray
        get() = sequenceIds
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    private val jceData by lazy { toJceDataImpl(subject) }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

internal class MessageSourceToStrangerImpl(
    override val sequenceIds: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val sender: Bot,
    override val target: Stranger
) : OnlineMessageSource.Outgoing.ToStranger(), MessageSourceInternal {
    override val bot: Bot
        get() = sender
    override val ids: IntArray
        get() = sequenceIds
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    private val jceData by lazy { toJceDataImpl(subject) }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

internal class MessageSourceToTempImpl(
    override val sequenceIds: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val sender: Bot,
    override val target: Member
) : OnlineMessageSource.Outgoing.ToTemp(), MessageSourceInternal {
    override val bot: Bot
        get() = sender
    override val ids: IntArray
        get() = sequenceIds
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    private val jceData by lazy { toJceDataImpl(subject) }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

internal class MessageSourceToGroupImpl(
    coroutineScope: CoroutineScope,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val sender: Bot,
    override val target: Group
) : OnlineMessageSource.Outgoing.ToGroup(), MessageSourceInternal {
    override val ids: IntArray
        get() = sequenceIds
    override val bot: Bot
        get() = sender
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)

    private val sequenceIdDeferred: Deferred<Int?> =
        coroutineScope.asyncFromEventOrNull<SendGroupMessageReceipt, Int>(
            timeoutMillis = 3000
        ) {
            if (it.messageRandom in this@MessageSourceToGroupImpl.internalIds) {
                it.sequenceId
            } else null
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val sequenceIds: IntArray
        get() = intArrayOf(
            when {
                sequenceIdDeferred.isCompleted -> sequenceIdDeferred.getCompleted() ?: -1
                !sequenceIdDeferred.isActive -> -1
                else -> error("sequenceIds not yet available")
            }
        )

    suspend fun ensureSequenceIdAvailable() = kotlin.run { sequenceIdDeferred.await() }

    private val jceData by lazy {
        val elements = originalMessage.toRichTextElems(subject, withGeneralFlags = true)
        ImMsgBody.SourceMsg(
            origSeqs = sequenceIds,
            senderUin = fromId,
            toUin = Mirai.calculateGroupUinByGroupCode(targetId),
            flag = 1,
            elems = elements,
            type = 0,
            time = time,
            pbReserve = SourceMsg.ResvAttr(
                origUids = internalIds.map { it.toLongUnsigned() } // ids is actually messageRandom
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = fromId, // qq
                    toUin = Mirai.calculateGroupUinByGroupCode(targetId), // group
                    msgType = 82, // 82?
                    c2cCmd = 1,
                    msgSeq = sequenceIds.single(), // TODO !!
                    msgTime = time,
                    msgUid = internalIds.single().toLongUnsigned(), //  TODO !!
                    groupInfo = MsgComm.GroupInfo(groupCode = targetId),
                    isSrcMsg = true
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = elements.toMutableList().also {
                            if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
                        }
                    )
                )
            ).toByteArray(MsgComm.Msg.serializer())
        )
    }

    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}