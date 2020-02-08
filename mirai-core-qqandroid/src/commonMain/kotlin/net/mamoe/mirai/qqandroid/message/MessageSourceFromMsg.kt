/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.jvm.JvmStatic

internal inline class MessageSourceFromServer(
    val delegate: ImMsgBody.SourceMsg
) : MessageSource {
    override val originalSeq: Int get() = delegate.origSeqs!!.first()
    override val senderId: Long get() = delegate.senderUin
    override val groupId: Long get() = delegate.toUin
    override val time: Int get() = delegate.time

    override fun toString(): String = ""
}

internal inline class MessageSourceFromMsg(
    val delegate: MsgComm.Msg
) : MessageSource {
    override val originalSeq: Int get() = delegate.msgHead.msgSeq
    override val senderId: Long get() = delegate.msgHead.fromUin
    override val groupId: Long get() = delegate.msgHead.toUin
    override val time: Int get() = delegate.msgHead.msgTime

    fun toJceData(): ImMsgBody.SourceMsg {
        return ImMsgBody.SourceMsg(
            origSeqs = listOf(delegate.msgHead.msgSeq),
            senderUin = delegate.msgHead.fromUin,
            toUin = delegate.msgHead.groupInfo!!.groupCode,
            flag = 1,
            elems = delegate.toMessageChain().toRichTextElems(),
            type = 0,
            time = delegate.msgHead.msgTime,
            pbReserve = SourceMsg.ResvAttr(
                origUids = delegate.msgBody.richText.attr!!.random.toLong()//,
                //oriMsgtype = delegate.msgHead.msgType
            ).toByteArray(SourceMsg.ResvAttr.serializer()).also { println("pbReserve=" + it.toUHexString()) },// PbReserve(delegate.msgBody.richText.attr!!.random.toLong()).toByteArray(PbReserve.serializer()).also { println("pbReserve=" + it.toUHexString()) },
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = delegate.msgHead.fromUin, // qq
                    toUin = delegate.msgHead.groupInfo.groupCode, // group
                    msgType = delegate.msgHead.msgType.also { println("msgType=$it") }, // 82?
                    c2cCmd = delegate.msgHead.c2cCmd,
                    msgSeq = delegate.msgHead.msgSeq,
                    msgTime = delegate.msgHead.msgTime,
                    msgUid = delegate.msgBody.richText.attr.random.toLong(), // ok
                    groupInfo = MsgComm.GroupInfo(groupCode = delegate.msgHead.groupInfo.groupCode),
                    isSrcMsg = true
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = delegate.toMessageChain().toRichTextElems().apply { add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2())) }
                    )
                )
            ).toByteArray(MsgComm.Msg.serializer()).also { println("srcMsg=" + it.toUHexString()) } // fucking slow
        ).also { println(it.contentToString()) }
    }

    override fun toString(): String = ""

    companion object {
        @JvmStatic
        val PB_RESERVE_HEAD = byteArrayOf(0x18)

        @JvmStatic
        val PB_RESERVE_TAIL = byteArrayOf(1)
    }
}