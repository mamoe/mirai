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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.internal.MiraiAtomicBoolean
import net.mamoe.mirai.event.subscribingGetAsync
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.OnlinePush
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.MiraiExperimentalAPI


private fun <T> T.toJceDataImpl(): ImMsgBody.SourceMsg
        where T : MessageSourceInternal, T : MessageSource {

    val elements = originalMessage.toRichTextElems(forGroup = false, withGeneralFlags = true)
    val messageUid: Long = sequenceId.toLong().shl(32) or id.toLong().and(0xffFFffFF)
    return ImMsgBody.SourceMsg(
        origSeqs = listOf(sequenceId),
        senderUin = fromId,
        toUin = targetId,
        flag = 1,
        elems = elements,
        type = 0,
        time = time,
        pbReserve = SourceMsg.ResvAttr(
            origUids = messageUid
        ).toByteArray(SourceMsg.ResvAttr.serializer()),
        srcMsg = MsgComm.Msg(
            msgHead = MsgComm.MsgHead(
                fromUin = fromId, // qq
                toUin = targetId, // group
                msgType = 9, // 82?
                c2cCmd = 11,
                msgSeq = sequenceId,
                msgTime = time,
                msgUid = messageUid, // ok
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
    override val sequenceId: Int,
    override val id: Int,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val sender: Bot,
    override val target: QQ
) : OnlineMessageSource.Outgoing.ToFriend(), MessageSourceInternal {
    override val bot: Bot
        get() = sender
    override var isRecalledOrPlanned: MiraiAtomicBoolean = MiraiAtomicBoolean(false)
    private val jceData by lazy { toJceDataImpl() }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

internal class MessageSourceToTempImpl(
    override val sequenceId: Int,
    override val id: Int,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val sender: Bot,
    override val target: Member
) : OnlineMessageSource.Outgoing.ToTemp(), MessageSourceInternal {
    override val bot: Bot
        get() = sender
    override var isRecalledOrPlanned: MiraiAtomicBoolean = MiraiAtomicBoolean(false)
    private val jceData by lazy { toJceDataImpl() }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

internal class MessageSourceToGroupImpl(
    override val id: Int,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val sender: Bot,
    override val target: Group
) : OnlineMessageSource.Outgoing.ToGroup(), MessageSourceInternal {
    override val bot: Bot
        get() = sender
    override var isRecalledOrPlanned: MiraiAtomicBoolean = MiraiAtomicBoolean(false)
    private lateinit var sequenceIdDeferred: Deferred<Int>

    @OptIn(ExperimentalCoroutinesApi::class)
    override val sequenceId: Int
        get() = sequenceIdDeferred.getCompleted()

    @OptIn(MiraiExperimentalAPI::class)
    internal fun startWaitingSequenceId(coroutineScope: CoroutineScope) {
        sequenceIdDeferred =
            coroutineScope.subscribingGetAsync<OnlinePush.PbPushGroupMsg.SendGroupMessageReceipt, Int>(
                timeoutMillis = 3000
            ) {
                if (it.messageRandom == this@MessageSourceToGroupImpl.id) {
                    it.sequenceId
                } else null
            }
    }

    suspend fun ensureSequenceIdAvailable() = sequenceIdDeferred.join()

    private val jceData by lazy {
        val elements = originalMessage.toRichTextElems(forGroup = false, withGeneralFlags = true)
        ImMsgBody.SourceMsg(
            origSeqs = listOf(sequenceId),
            senderUin = fromId,
            toUin = Group.calculateGroupUinByGroupCode(targetId),
            flag = 1,
            elems = elements,
            type = 0,
            time = time,
            pbReserve = SourceMsg.ResvAttr(
                origUids = id.toLong() and 0xffFFffFF // id is actually messageRandom
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = fromId, // qq
                    toUin = Group.calculateGroupUinByGroupCode(targetId), // group
                    msgType = 82, // 82?
                    c2cCmd = 1,
                    msgSeq = sequenceId,
                    msgTime = time,
                    msgUid = id.toLong() and 0xffFFffFF, // ok
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