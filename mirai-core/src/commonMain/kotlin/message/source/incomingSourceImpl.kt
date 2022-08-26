/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.internal.message.source

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.contact.newAnonymous
import net.mamoe.mirai.internal.getGroupByUinOrCodeOrFail
import net.mamoe.mirai.internal.message.MessageSourceSerializerImpl
import net.mamoe.mirai.internal.message.toMessageChainNoSource
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.SourceMsg
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.debug.structureToString
import net.mamoe.mirai.utils.encodeBase64
import net.mamoe.mirai.utils.mapToIntArray

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMessageSourceFromFriendImpl.Serializer::class)
internal class OnlineMessageSourceFromFriendImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>,
) : OnlineMessageSource.Incoming.FromFriend(), IncomingMessageSourceInternal {
    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromFriend")

    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override var isRecalledOrPlanned: AtomicBoolean = atomic(false)
    override val ids: IntArray get() = sequenceIds // msg.msgBody.richText.attr!!.random
    override val internalIds: IntArray = msg.mapToIntArray {
        it.msgBody.richText.attr?.random ?: 0
    } // other client 消息的这个是0
    override val time: Int = msg.first().msgHead.msgTime
    override var originalMessageLazy = lazy {
        msg.toMessageChainNoSource(bot, 0, MessageSourceKind.FRIEND)
    }
    override val originalMessage: MessageChain get() = originalMessageLazy.value
    override val isOriginalMessageInitialized: Boolean
        get() = originalMessageLazy.isInitialized()

    override val sender: Friend = bot.getFriendOrFail(msg.first().msgHead.fromUin)

    private val jceData: ImMsgBody.SourceMsg by lazy { msg.toJceDataPrivate(internalIds) }

    override fun toJceData(): ImMsgBody.SourceMsg = jceData

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return super<IncomingMessageSourceInternal>.accept(visitor, data)
    }
}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMessageSourceFromStrangerImpl.Serializer::class)
internal class OnlineMessageSourceFromStrangerImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>,
) : OnlineMessageSource.Incoming.FromStranger(), IncomingMessageSourceInternal {
    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromStranger")

    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override var isRecalledOrPlanned: AtomicBoolean = atomic(false)
    override val ids: IntArray get() = sequenceIds // msg.msgBody.richText.attr!!.random
    override val internalIds: IntArray = msg.mapToIntArray {
        it.msgBody.richText.attr?.random ?: 0
    } // other client 消息的这个是0
    override val time: Int = msg.first().msgHead.msgTime
    override var originalMessageLazy = lazy {
        msg.toMessageChainNoSource(bot, 0, MessageSourceKind.STRANGER)
    }
    override val originalMessage: MessageChain get() = originalMessageLazy.value
    override val isOriginalMessageInitialized: Boolean
        get() = originalMessageLazy.isInitialized()

    override val sender: Stranger = bot.getStrangerOrFail(msg.first().msgHead.fromUin)

    private val jceData: ImMsgBody.SourceMsg by lazy { msg.toJceDataPrivate(internalIds) }

    override fun toJceData(): ImMsgBody.SourceMsg = jceData

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return super<IncomingMessageSourceInternal>.accept(visitor, data)
    }
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

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMessageSourceFromTempImpl.Serializer::class)
internal class OnlineMessageSourceFromTempImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>,
) : OnlineMessageSource.Incoming.FromTemp(), IncomingMessageSourceInternal {
    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromTemp")

    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override val internalIds: IntArray = msg.mapToIntArray { it.msgBody.richText.attr!!.random }
    override var isRecalledOrPlanned: AtomicBoolean = atomic(false)
    override val ids: IntArray get() = sequenceIds //
    override val time: Int = msg.first().msgHead.msgTime
    override var originalMessageLazy = lazy {
        msg.toMessageChainNoSource(bot, groupIdOrZero = 0, MessageSourceKind.TEMP)
    }
    override val originalMessage: MessageChain get() = originalMessageLazy.value
    override val isOriginalMessageInitialized: Boolean
        get() = originalMessageLazy.isInitialized()

    override val sender: Member = with(msg.first().msgHead) {
        // it must be uin, see #1410
        // corresponding test: net.mamoe.mirai.internal.notice.processors.MessageTest.group temp message test 2

        // search for group code also is for tests. code may be passed as uin in tests.
        // clashing is unlikely possible in real time, so it would not be a problem.
        bot.asQQAndroidBot().getGroupByUinOrCodeOrFail(c2cTmpMsgHead!!.groupUin).getOrFail(fromUin)
    }

    private val jceData: ImMsgBody.SourceMsg by lazy { msg.toJceDataPrivate(internalIds) }
    override fun toJceData(): ImMsgBody.SourceMsg = jceData

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return super<IncomingMessageSourceInternal>.accept(visitor, data)
    }
}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMessageSourceFromGroupImpl.Serializer::class)
internal class OnlineMessageSourceFromGroupImpl(
    override val bot: Bot,
    msg: List<MsgComm.Msg>,
) : OnlineMessageSource.Incoming.FromGroup(), IncomingMessageSourceInternal {
    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromGroupImpl")

    @Transient
    override var isRecalledOrPlanned: AtomicBoolean = atomic(false)
    override val sequenceIds: IntArray = msg.mapToIntArray { it.msgHead.msgSeq }
    override val internalIds: IntArray = msg.mapToIntArray { it.msgBody.richText.attr!!.random }
    override val ids: IntArray get() = sequenceIds
    override val time: Int = msg.first().msgHead.msgTime
    override var originalMessageLazy = lazy {
        msg.toMessageChainNoSource(bot, groupIdOrZero = group.id, MessageSourceKind.GROUP)
    }
    override val originalMessage: MessageChain get() = originalMessageLazy.value
    override val isOriginalMessageInitialized: Boolean
        get() = originalMessageLazy.isInitialized()


    override val subject: GroupImpl by lazy {
        val groupCode = msg.first().msgHead.groupInfo?.groupCode
            ?: error("cannot find groupCode for OnlineMessageSourceFromGroupImpl. msg=${msg.structureToString()}")

        val group = bot.getGroup(groupCode)?.checkIsGroupImpl()
            ?: error("cannot find group for OnlineMessageSourceFromGroupImpl. msg=${msg.structureToString()}")

        group
    }

    override val sender: Member by lazy {
        val group = subject

        val member = group[msg.first().msgHead.fromUin]
        if (member != null) return@lazy member

        val anonymousInfo = msg.first().msgBody.richText.elems.firstOrNull { it.anonGroupMsg != null }
            ?: error("cannot find member for OnlineMessageSourceFromGroupImpl. msg=${msg.structureToString()}")

        anonymousInfo.run {
            group.newAnonymous(anonGroupMsg!!.anonNick.decodeToString(), anonGroupMsg.anonId.encodeBase64())
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

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return super<IncomingMessageSourceInternal>.accept(visitor, data)
    }
}