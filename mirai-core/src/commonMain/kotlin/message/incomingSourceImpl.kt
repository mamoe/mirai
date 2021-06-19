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
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.contact.newAnonymous
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.encodeBase64
import net.mamoe.mirai.utils.encodeToString
import net.mamoe.mirai.utils.mapToIntArray
import java.util.concurrent.atomic.AtomicBoolean

@Serializable(OnlineMessageSourceFromFriendImpl.Serializer::class)
internal class OnlineMessageSourceFromFriendImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>
) : OnlineMessageSource.Incoming.FromFriend(), MessageSourceInternal {
    object Serializer : MessageSourceSerializerImpl("OnlineMessageSourceFromFriend")

    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val ids: IntArray get() = sequenceIds// msg.msgBody.richText.attr!!.random
    override val internalIds: IntArray = msg.mapToIntArray {
        it.msgBody.richText.attr?.random ?: 0
    } // other client 消息的这个是0
    override val time: Int = msg.first().msgHead.msgTime
    override val originalMessage: MessageChain by lazy {
        msg.toMessageChainNoSource(bot, 0, MessageSourceKind.FRIEND)
    }
    override val sender: Friend = bot.getFriendOrFail(msg.first().msgHead.fromUin)

    private val jceData: ImMsgBody.SourceMsg by lazy { msg.toJceDataPrivate(internalIds) }

    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

@Serializable(OnlineMessageSourceFromStrangerImpl.Serializer::class)
internal class OnlineMessageSourceFromStrangerImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>
) : OnlineMessageSource.Incoming.FromStranger(), MessageSourceInternal {
    object Serializer : MessageSourceSerializerImpl("OnlineMessageSourceFromStranger")

    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val ids: IntArray get() = sequenceIds// msg.msgBody.richText.attr!!.random
    override val internalIds: IntArray = msg.mapToIntArray {
        it.msgBody.richText.attr?.random ?: 0
    } // other client 消息的这个是0
    override val time: Int = msg.first().msgHead.msgTime
    override val originalMessage: MessageChain by lazy {
        msg.toMessageChainNoSource(bot, 0, MessageSourceKind.STRANGER)
    }
    override val sender: Stranger = bot.getStrangerOrFail(msg.first().msgHead.fromUin)

    private val jceData: ImMsgBody.SourceMsg by lazy { msg.toJceDataPrivate(internalIds) }

    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

private fun List<MsgComm.Msg>.toJceDataPrivate(ids: IntArray): ImMsgBody.SourceMsg {
    val elements = flatMap { it.msgBody.richText.elems }.toMutableList().also {
        if (it.last().elemFlags2 == null) it.add(ImMsgBody.Elem(elemFlags2 = ImMsgBody.ElemFlags2()))
    }

    first().msgHead.run {
        return ImMsgBody.SourceMsg(
            origSeqs = mapToIntArray { it.msgHead.msgSeq },
            senderUin = fromUin,
            toUin = toUin,
            flag = 1,
            elems = flatMap { it.msgBody.richText.elems },
            type = 0,
            time = msgTime,
            pbReserve = SourceMsg.ResvAttr(
                origUids = ids.map { it.toLong() and 0xFFFF_FFFF }
            ).toByteArray(SourceMsg.ResvAttr.serializer()),
            srcMsg = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = fromUin, // qq
                    toUin = toUin, // group
                    msgType = msgType, // 82?
                    c2cCmd = c2cCmd,
                    msgSeq = msgSeq,
                    msgTime = msgTime,
                    msgUid = ids.single().toLong() and 0xFFFF_FFFF, // ok
                    // groupInfo = MsgComm.GroupInfo(groupCode = msgHead.groupInfo.groupCode),
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
}

@Serializable(OnlineMessageSourceFromTempImpl.Serializer::class)
internal class OnlineMessageSourceFromTempImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>
) : OnlineMessageSource.Incoming.FromTemp(), MessageSourceInternal {
    object Serializer : MessageSourceSerializerImpl("OnlineMessageSourceFromTemp")

    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override val internalIds: IntArray = msg.mapToIntArray { it.msgBody.richText.attr!!.random }
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val ids: IntArray get() = sequenceIds//
    override val time: Int = msg.first().msgHead.msgTime
    override val originalMessage: MessageChain by lazy {
        msg.toMessageChainNoSource(bot, groupIdOrZero = 0, MessageSourceKind.TEMP)
    }
    override val sender: Member = with(msg.first().msgHead) {
        bot.getGroupOrFail(c2cTmpMsgHead!!.groupUin).getOrFail(fromUin)
    }

    private val jceData: ImMsgBody.SourceMsg by lazy { msg.toJceDataPrivate(internalIds) }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData
}

@Serializable(OnlineMessageSourceFromGroupImpl.Serializer::class)
internal class OnlineMessageSourceFromGroupImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>
) : OnlineMessageSource.Incoming.FromGroup(), MessageSourceInternal {
    object Serializer : MessageSourceSerializerImpl("OnlineMessageSourceFromGroupImpl")

    @Transient
    override var isRecalledOrPlanned: AtomicBoolean = AtomicBoolean(false)
    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override val internalIds: IntArray = msg.mapToIntArray { it.msgBody.richText.attr!!.random }
    override val ids: IntArray get() = sequenceIds
    override val time: Int = msg.first().msgHead.msgTime
    override val originalMessage: MessageChain by lazy {
        msg.toMessageChainNoSource(bot, groupIdOrZero = group.id, MessageSourceKind.GROUP)
    }

    override val sender: Member by lazy {
        val groupCode = msg.first().msgHead.groupInfo?.groupCode
            ?: error("cannot find groupCode for OnlineMessageSourceFromGroupImpl. msg=${msg._miraiContentToString()}")

        val group = bot.getGroup(groupCode)?.checkIsGroupImpl()
            ?: error("cannot find group for OnlineMessageSourceFromGroupImpl. msg=${msg._miraiContentToString()}")

        val member = group[msg.first().msgHead.fromUin]
        if (member != null) return@lazy member

        val anonymousInfo = msg.first().msgBody.richText.elems.firstOrNull { it.anonGroupMsg != null }
            ?: error("cannot find member for OnlineMessageSourceFromGroupImpl. msg=${msg._miraiContentToString()}")

        anonymousInfo.run {
            group.newAnonymous(anonGroupMsg!!.anonNick.encodeToString(), anonGroupMsg.anonId.encodeBase64())
        }
    }

    private val jceData: ImMsgBody.SourceMsg by lazy {
        ImMsgBody.SourceMsg(
            origSeqs = intArrayOf(msg.first().msgHead.msgSeq),
            senderUin = msg.first().msgHead.fromUin,
            toUin = 0,
            flag = 1,
            elems = msg.flatMap { it.msgBody.richText.elems },
            type = 0,
            time = msg.first().msgHead.msgTime,
            pbReserve = EMPTY_BYTE_ARRAY,
            srcMsg = EMPTY_BYTE_ARRAY
        )
    }

    override fun toJceData(): ImMsgBody.SourceMsg {
        return jceData
    }
}