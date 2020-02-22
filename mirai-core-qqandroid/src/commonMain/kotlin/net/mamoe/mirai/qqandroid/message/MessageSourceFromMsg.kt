/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.qqandroid.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.SourceMsg

internal inline class MessageSourceFromServer(
    val delegate: ImMsgBody.SourceMsg
) : MessageSource {
    override val time: Long get() = delegate.time.toLong() and 0xFFFFFFFF
    override val sequenceId: Int get() = delegate.origSeqs?.firstOrNull() ?: error("cannot find sequenceId from ImMsgBody.SourceMsg")

    override suspend fun ensureSequenceIdAvailable() {
        // nothing to do
    }

    override val messageUid: Long get() = delegate.pbReserve.loadAs(SourceMsg.ResvAttr.serializer()).origUids!!
    // override val sourceMessage: MessageChain get() = delegate.toMessageChain()
    override val senderId: Long get() = delegate.senderUin
    override val groupId: Long get() = Group.calculateGroupCodeByGroupUin(delegate.toUin)

    override fun toString(): String = ""
}

internal inline class MessageSourceFromMsg(
    val delegate: MsgComm.Msg
) : MessageSource {
    override val time: Long get() = delegate.msgHead.msgTime.toLong() and 0xFFFFFFFF
    override val sequenceId: Int get() = delegate.msgHead.msgSeq
    override suspend fun ensureSequenceIdAvailable() {
        // nothing to do
    }

    override val messageUid: Long get() = delegate.msgBody.richText.attr!!.random.toLong()
    // override val sourceMessage: MessageChain get() = delegate.toMessageChain()
    override val senderId: Long get() = delegate.msgHead.fromUin
    override val groupId: Long get() = delegate.msgHead.groupInfo!!.groupCode

    fun toJceData(): ImMsgBody.SourceMsg {

        val groupUin = Group.calculateGroupUinByGroupCode(delegate.msgHead.groupInfo!!.groupCode)

        return ImMsgBody.SourceMsg(
            origSeqs = listOf(delegate.msgHead.msgSeq),
            senderUin = delegate.msgHead.fromUin,
            toUin = groupUin,
            flag = 1,
            elems = delegate.msgBody.richText.elems,
            type = 0,
            time = delegate.msgHead.msgTime,
            pbReserve = SourceMsg.ResvAttr(
                origUids = messageUid
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = delegate.msgHead.fromUin, // qq
                    toUin = groupUin, // group
                    msgType = delegate.msgHead.msgType, // 82?
                    c2cCmd = delegate.msgHead.c2cCmd,
                    msgSeq = delegate.msgHead.msgSeq,
                    msgTime = delegate.msgHead.msgTime,
                    msgUid = messageUid, // ok
                    groupInfo = MsgComm.GroupInfo(groupCode = delegate.msgHead.groupInfo.groupCode),
                    isSrcMsg = true
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = delegate.msgBody.richText.elems.also {
                            if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
                        }
                    )
                )
            ).toByteArray(MsgComm.Msg.serializer())
        )
    }

    override fun toString(): String = ""
}