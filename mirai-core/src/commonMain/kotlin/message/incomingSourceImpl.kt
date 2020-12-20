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

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.encodeToBase64
import net.mamoe.mirai.internal.utils.encodeToString
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import java.util.concurrent.atomic.AtomicBoolean

internal interface MessageSourceInternal {
    val sequenceIds: IntArray
    val internalIds: IntArray // randomId

    @Deprecated("don't use this internally. Use sequenceId or random instead.", level = DeprecationLevel.ERROR)
    val ids: IntArray

    val isRecalledOrPlanned: AtomicBoolean

    fun toJceData(): ImMsgBody.SourceMsg
}

@Suppress("RedundantSuspendModifier", "unused")
internal suspend inline fun MessageSource.ensureSequenceIdAvailable() {
    // obsolete but keep for future
    return
    /*
    if (this is MessageSourceToGroupImpl) {
        this.ensureSequenceIdAvailable()
    }*/
}

@Suppress("RedundantSuspendModifier", "unused")
internal suspend inline fun Message.ensureSequenceIdAvailable() {
    // no suspend.

    // obsolete but keep for future
    return
    /*
    if (this is MessageSourceToGroupImpl) {
        this.ensureSequenceIdAvailable()
    }*/
}

internal class MessageSourceFromFriendImpl(
    override val bot: Bot,
    val msg: MsgComm.Msg
) : OnlineMessageSource.Incoming.FromFriend(), MessageSourceInternal {
    override val sequenceIds: IntArray get() = intArrayOf(msg.msgHead.msgSeq)
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val ids: IntArray get() = sequenceIds// msg.msgBody.richText.attr!!.random
    override val internalIds: IntArray get() = intArrayOf(msg.msgBody.richText.attr!!.random)
    override val time: Int get() = msg.msgHead.msgTime
    override val originalMessage: MessageChain by lazy { msg.toMessageChain(bot, bot.id, 0, false) }
    override val sender: Friend get() = bot.getFriendOrFail(msg.msgHead.fromUin)

    private val jceData by lazy { msg.toJceDataFriendOrTemp(internalIds) }

    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

private fun MsgComm.Msg.toJceDataFriendOrTemp(ids: IntArray): ImMsgBody.SourceMsg {
    val elements = msgBody.richText.elems.toMutableList().also {
        if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
    }
    return ImMsgBody.SourceMsg(
        origSeqs = intArrayOf(this.msgHead.msgSeq),
        senderUin = this.msgHead.fromUin,
        toUin = this.msgHead.toUin,
        flag = 1,
        elems = this.msgBody.richText.elems,
        type = 0,
        time = this.msgHead.msgTime,
        pbReserve = SourceMsg.ResvAttr(
            origUids = ids.map { it.toLong() and 0xFFFF_FFFF }
        ).toByteArray(SourceMsg.ResvAttr.serializer()),
        srcMsg = MsgComm.Msg(
            msgHead = MsgComm.MsgHead(
                fromUin = this.msgHead.fromUin, // qq
                toUin = this.msgHead.toUin, // group
                msgType = this.msgHead.msgType, // 82?
                c2cCmd = this.msgHead.c2cCmd,
                msgSeq = this.msgHead.msgSeq,
                msgTime = this.msgHead.msgTime,
                msgUid = ids.single().toLong() and 0xFFFF_FFFF, // ok
                // groupInfo = MsgComm.GroupInfo(groupCode = this.msgHead.groupInfo.groupCode),
                isSrcMsg = true
            ),
            msgBody = ImMsgBody.MsgBody(
                richText = ImMsgBody.RichText(
                    elems = elements
                )
            )
        ).toByteArray(MsgComm.Msg.serializer())
    )
}

internal class MessageSourceFromTempImpl(
    override val bot: Bot,
    private val msg: MsgComm.Msg
) : OnlineMessageSource.Incoming.FromTemp(), MessageSourceInternal {
    override val sequenceIds: IntArray get() = intArrayOf(msg.msgHead.msgSeq)
    override val internalIds: IntArray get() = intArrayOf(msg.msgBody.richText.attr!!.random)
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val ids: IntArray get() = sequenceIds//
    override val time: Int get() = msg.msgHead.msgTime
    override val originalMessage: MessageChain by lazy { msg.toMessageChain(bot, 0, false) }
    override val sender: Member
        get() = with(msg.msgHead) {
            bot.getGroupOrFail(c2cTmpMsgHead!!.groupUin).getOrFail(fromUin)
        }

    private val jceData by lazy { msg.toJceDataFriendOrTemp(internalIds) }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

internal data class MessageSourceFromGroupImpl(
    override val bot: Bot,
    private val msg: MsgComm.Msg
) : OnlineMessageSource.Incoming.FromGroup(), MessageSourceInternal {
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val sequenceIds: IntArray get() = intArrayOf(msg.msgHead.msgSeq)
    override val internalIds: IntArray get() = intArrayOf(msg.msgBody.richText.attr!!.random)
    override val ids: IntArray get() = sequenceIds
    override val time: Int get() = msg.msgHead.msgTime
    override val originalMessage: MessageChain by lazy {
        msg.toMessageChain(bot, groupIdOrZero = group.id, onlineSource = false)
    }

    override val sender: Member by lazy {
        (bot.getGroup(
            msg.msgHead.groupInfo?.groupCode
                ?: error("cannot find groupCode for MessageSourceFromGroupImpl. msg=${msg._miraiContentToString()}")
        ) as GroupImpl).run {
            get(msg.msgHead.fromUin)
                ?: msg.msgBody.richText.elems.firstOrNull { it.anonGroupMsg != null }?.run {
                    newAnonymous(anonGroupMsg!!.anonNick.encodeToString(), anonGroupMsg.anonId.encodeToBase64())
                }
                ?: error("cannot find member for MessageSourceFromGroupImpl. msg=${msg._miraiContentToString()}")
        }
    }

    override fun toJceData(): ImMsgBody.SourceMsg {
        return ImMsgBody.SourceMsg(
            origSeqs = intArrayOf(msg.msgHead.msgSeq),
            senderUin = msg.msgHead.fromUin,
            toUin = 0,
            flag = 1,
            elems = msg.msgBody.richText.elems,
            type = 0,
            time = msg.msgHead.msgTime,
            pbReserve = EMPTY_BYTE_ARRAY,
            srcMsg = EMPTY_BYTE_ARRAY
        )
    }
}
