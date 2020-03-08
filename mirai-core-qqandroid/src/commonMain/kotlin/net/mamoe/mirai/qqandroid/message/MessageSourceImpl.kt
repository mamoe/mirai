/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.message

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.subscribingGetAsync
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.messageRandom
import net.mamoe.mirai.message.data.sequenceId
import net.mamoe.mirai.qqandroid.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.OnlinePush
import net.mamoe.mirai.utils.MiraiExperimentalAPI

internal class MessageSourceFromServer(
    val delegate: ImMsgBody.SourceMsg
) : MessageSource {
    override val time: Long get() = delegate.time.toLong() and 0xFFFFFFFF

    override val originalMessage: MessageChain by lazy {
        delegate.toMessageChain()
    }

    override val id: Long
        get() = (delegate.origSeqs?.firstOrNull()
            ?: error("cannot find sequenceId from ImMsgBody.SourceMsg")).toLong().shl(32) or
                delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer()).origUids!!.and(0xFFFFFFFF)

    override val toUin: Long get() = delegate.toUin

    override suspend fun ensureSequenceIdAvailable() {
        // nothing to do
    }

    // override val sourceMessage: MessageChain get() = delegate.toMessageChain()
    override val senderId: Long get() = delegate.senderUin
    override val groupId: Long get() = Group.calculateGroupCodeByGroupUin(delegate.toUin)

    override fun toString(): String = ""
}

internal class MessageSourceFromMsg(
    val delegate: MsgComm.Msg
) : MessageSource {
    override val time: Long get() = delegate.msgHead.msgTime.toLong() and 0xFFFFFFFF
    override val id: Long =
        delegate.msgHead.msgSeq.toLong().shl(32) or
                delegate.msgBody.richText.attr!!.random.toLong().and(0xFFFFFFFF)

    override suspend fun ensureSequenceIdAvailable() {
        // nothing to do
    }

    override val toUin: Long get() = delegate.msgHead.toUin
    override val senderId: Long get() = delegate.msgHead.fromUin
    override val groupId: Long get() = delegate.msgHead.groupInfo?.groupCode ?: 0
    override val originalMessage: MessageChain by lazy {
        delegate.toMessageChain()
    }

    fun toJceData(): ImMsgBody.SourceMsg {
        return if (groupId == 0L) {
            toJceDataImplForFriend()
        } else toJceDataImplForGroup()
    }

    val elems by lazy {
        delegate.msgBody.richText.elems.toMutableList().also {
            if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
        }
    }

    private fun toJceDataImplForFriend(): ImMsgBody.SourceMsg {
        return ImMsgBody.SourceMsg(
            origSeqs = listOf(delegate.msgHead.msgSeq),
            senderUin = delegate.msgHead.fromUin,
            toUin = delegate.msgHead.toUin,
            flag = 1,
            elems = delegate.msgBody.richText.elems,
            type = 0,
            time = delegate.msgHead.msgTime,
            pbReserve = SourceMsg.ResvAttr(
                origUids = messageRandom.toLong() and 0xffFFffFF
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = delegate.msgHead.fromUin, // qq
                    toUin = delegate.msgHead.toUin, // group
                    msgType = delegate.msgHead.msgType, // 82?
                    c2cCmd = delegate.msgHead.c2cCmd,
                    msgSeq = delegate.msgHead.msgSeq,
                    msgTime = delegate.msgHead.msgTime,
                    msgUid = messageRandom.toLong() and 0xffFFffFF, // ok
                    // groupInfo = MsgComm.GroupInfo(groupCode = delegate.msgHead.groupInfo.groupCode),
                    isSrcMsg = true
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = elems
                    )
                )
            ).toByteArray(MsgComm.Msg.serializer())
        )
    }

    private fun toJceDataImplForGroup(): ImMsgBody.SourceMsg {

        val groupUin = Group.calculateGroupUinByGroupCode(groupId)

        return ImMsgBody.SourceMsg(
            origSeqs = listOf(delegate.msgHead.msgSeq),
            senderUin = delegate.msgHead.fromUin,
            toUin = groupUin,
            flag = 1,
            elems = delegate.msgBody.richText.elems,
            type = 0,
            time = delegate.msgHead.msgTime,
            pbReserve = SourceMsg.ResvAttr(
                origUids = messageRandom.toLong() and 0xffFFffFF
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = delegate.msgHead.fromUin, // qq
                    toUin = groupUin, // group
                    msgType = delegate.msgHead.msgType, // 82?
                    c2cCmd = delegate.msgHead.c2cCmd,
                    msgSeq = delegate.msgHead.msgSeq,
                    msgTime = delegate.msgHead.msgTime,
                    msgUid = messageRandom.toLong() and 0xffFFffFF, // ok
                    groupInfo = MsgComm.GroupInfo(groupCode = groupId),
                    isSrcMsg = true
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = elems
                    )
                )
            ).toByteArray(MsgComm.Msg.serializer())
        )
    }

    override fun toString(): String = ""
}

internal abstract class MessageSourceFromSend : MessageSource {

    abstract override val originalMessage: MessageChain

    fun toJceData(): ImMsgBody.SourceMsg {
        return if (groupId == 0L) {
            toJceDataImplForFriend()
        } else toJceDataImplForGroup()
    }

    private val elems by lazy {
        originalMessage.toRichTextElems(groupId != 0L)
    }

    private fun toJceDataImplForFriend(): ImMsgBody.SourceMsg {
        val messageUid: Long = 262144L.shl(32) or messageRandom.toLong().and(0xffFFffFF)
        return ImMsgBody.SourceMsg(
            origSeqs = listOf(sequenceId),
            senderUin = senderId,
            toUin = toUin,
            flag = 1,
            elems = elems,
            type = 0,
            time = time.toInt(),
            pbReserve = SourceMsg.ResvAttr(
                origUids = messageUid
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = senderId, // qq
                    toUin = toUin, // group
                    msgType = 9, // 82?
                    c2cCmd = 11,
                    msgSeq = sequenceId,
                    msgTime = time.toInt(),
                    msgUid = messageUid, // ok
                    // groupInfo = MsgComm.GroupInfo(groupCode = delegate.msgHead.groupInfo.groupCode),
                    isSrcMsg = true
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = elems.toMutableList().also {
                            if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
                        }
                    )
                )
            ).toByteArray(MsgComm.Msg.serializer())
        )
    }

    private fun toJceDataImplForGroup(): ImMsgBody.SourceMsg {
        return ImMsgBody.SourceMsg(
            origSeqs = listOf(sequenceId),
            senderUin = senderId,
            toUin = toUin,
            flag = 1,
            elems = elems,
            type = 0,
            time = time.toInt(),
            pbReserve = SourceMsg.ResvAttr(
                origUids = messageRandom.toLong() and 0xffFFffFF
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = senderId, // qq
                    toUin = toUin, // group
                    msgType = 82, // 82?
                    c2cCmd = 1,
                    msgSeq = sequenceId,
                    msgTime = time.toInt(),
                    msgUid = messageRandom.toLong() and 0xffFFffFF, // ok
                    groupInfo = MsgComm.GroupInfo(groupCode = groupId),
                    isSrcMsg = true
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = elems.toMutableList().also {
                            if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
                        }
                    )
                )
            ).toByteArray(MsgComm.Msg.serializer())
        )
    }

}


internal class MessageSourceFromSendFriend(
    val messageRandom: Int,
    override val time: Long,
    override val senderId: Long,
    override val toUin: Long,
    override val groupId: Long,
    val sequenceId: Int,
    override val originalMessage: MessageChain
) : MessageSourceFromSend() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val id: Long
        get() = sequenceId.toLong().shl(32) or
                messageRandom.toLong().and(0xFFFFFFFF)

    override suspend fun ensureSequenceIdAvailable() {
        // nothing to do
    }

    override fun toString(): String {
        return ""
    }
}

internal class MessageSourceFromSendGroup(
    val messageRandom: Int,
    override val time: Long,
    override val senderId: Long,
    override val toUin: Long,
    override val groupId: Long,
    override val originalMessage: MessageChain
) : MessageSourceFromSend() {
    private lateinit var sequenceIdDeferred: Deferred<Int>

    @OptIn(ExperimentalCoroutinesApi::class)
    override val id: Long
        get() = sequenceIdDeferred.getCompleted().toLong().shl(32) or
                messageRandom.toLong().and(0xFFFFFFFF)

    @OptIn(MiraiExperimentalAPI::class)
    internal fun startWaitingSequenceId(coroutineScope: CoroutineScope) {
        sequenceIdDeferred =
            coroutineScope.subscribingGetAsync<OnlinePush.PbPushGroupMsg.SendGroupMessageReceipt, Int>(
                timeoutMillis = 3000
            ) {
                if (it.messageRandom == this@MessageSourceFromSendGroup.messageRandom) {
                    it.sequenceId
                } else null
            }
    }

    override suspend fun ensureSequenceIdAvailable() {
        sequenceIdDeferred.join()
    }

    override fun toString(): String {
        return ""
    }
}