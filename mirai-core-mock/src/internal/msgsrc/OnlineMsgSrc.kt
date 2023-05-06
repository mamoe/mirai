/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(MiraiInternalApi::class)

package net.mamoe.mirai.mock.internal.msgsrc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.internal.message.MessageSourceSerializerImpl
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacadeImpl
import net.mamoe.mirai.internal.message.protocol.StubMessageProtocol
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.internal.contact.AbstractMockContact
import net.mamoe.mirai.mock.internal.contact.MockImage
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.currentTimeSeconds

internal fun registerMockMsgSerializers() {
    val serializers = mutableListOf<MessageSerializer<*>>()

    MessageSerializer.superclassesScope(Image::class, MessageContent::class, SingleMessage::class) {
        serializers.add(
            MessageSerializer(
                MockImage::class,
                MockImage.serializer()
            )
        )
    }

    MessageSerializer.superclassesScope(MessageSource::class, MessageMetadata::class, SingleMessage::class) {

        serializers.add(
            MessageSerializer(
                OnlineMsgSrcToGroup::class,
                OnlineMsgSrcToGroup.serializer()
            )
        )
        serializers.add(
            MessageSerializer(
                OnlineMsgSrcToFriend::class,
                OnlineMsgSrcToFriend.serializer()
            )
        )
        serializers.add(
            MessageSerializer(
                OnlineMsgSrcToStranger::class,
                OnlineMsgSrcToStranger.serializer()
            )
        )
        serializers.add(
            MessageSerializer(
                OnlineMsgSrcToTemp::class,
                OnlineMsgSrcToTemp.serializer()
            )
        )


        serializers.add(
            MessageSerializer(
                OnlineMsgSrcFromGroup::class,
                OnlineMsgSrcFromGroup.serializer()
            )
        )
        serializers.add(
            MessageSerializer(
                OnlineMsgSrcFromFriend::class,
                OnlineMsgSrcFromFriend.serializer()
            )
        )
        serializers.add(
            MessageSerializer(
                OnlineMsgSrcFromStranger::class,
                OnlineMsgSrcFromStranger.serializer()
            )
        )
        serializers.add(
            MessageSerializer(
                OnlineMsgSrcFromTemp::class,
                OnlineMsgSrcFromTemp.serializer()
            )
        )
    }

    val module = MessageProtocolFacadeImpl(listOf(StubMessageProtocol), "").also {
        it.serializers.addAll(serializers)
    }.createSerializersModule()

    MessageSerializers.registerSerializers(module)
}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcToGroup.Serializer::class)
internal class OnlineMsgSrcToGroup(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Group
) : OnlineMessageSource.Outgoing.ToGroup() {
    override val isOriginalMessageInitialized: Boolean get() = true

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("Mock_OnlineMessageSourceToGroup")

}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcToFriend.Serializer::class)
internal class OnlineMsgSrcToFriend(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Friend
) : OnlineMessageSource.Outgoing.ToFriend() {
    override val isOriginalMessageInitialized: Boolean get() = true

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("Mock_OnlineMessageSourceToFriend")

}


@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcToStranger.Serializer::class)
internal class OnlineMsgSrcToStranger(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Stranger
) : OnlineMessageSource.Outgoing.ToStranger() {
    override val isOriginalMessageInitialized: Boolean get() = true

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("Mock_OnlineMessageSourceToStranger")
}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcToTemp.Serializer::class)
internal class OnlineMsgSrcToTemp(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Member
) : OnlineMessageSource.Outgoing.ToTemp() {
    override val isOriginalMessageInitialized: Boolean get() = true

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("Mock_OnlineMessageSourceToTemp")
}


@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcFromFriend.Serializer::class)
internal class OnlineMsgSrcFromFriend(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Friend,
    override val target: ContactOrBot,
) : OnlineMessageSource.Incoming.FromFriend() {
    override val isOriginalMessageInitialized: Boolean get() = true

    override val subject: Friend
        get() {
            if (target is Bot) return sender
            return target.cast()
        }

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("Mock_OnlineMessageSourceFromFriend")
}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcFromStranger.Serializer::class)
internal class OnlineMsgSrcFromStranger(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Stranger,
    override val target: ContactOrBot,
) : OnlineMessageSource.Incoming.FromStranger() {
    override val isOriginalMessageInitialized: Boolean get() = true

    override val subject: Stranger
        get() {
            if (target is Bot) return sender
            return target.cast()
        }

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl(
        "Mock_OnlineMessageSourceFromStranger"
    )
}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcFromTemp.Serializer::class)
internal class OnlineMsgSrcFromTemp(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Member,
    override val target: ContactOrBot,
) : OnlineMessageSource.Incoming.FromTemp() {
    override val isOriginalMessageInitialized: Boolean get() = true
    override val subject: Member
        get() {
            if (target is Bot) return sender
            return target.cast()
        }

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("Mock_OnlineMessageSourceFromTemp")

}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(OnlineMsgSrcFromGroup.Serializer::class)
internal class OnlineMsgSrcFromGroup(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Member
) : OnlineMessageSource.Incoming.FromGroup() {
    override val isOriginalMessageInitialized: Boolean get() = true

    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("Mock_OnlineMessageSourceFromGroup")

}

internal typealias MsgSrcConstructor<R> = (
    ids: IntArray,
    internalIds: IntArray,
    time: Int,
) -> R

internal inline fun <R> AbstractMockContact.newMsgSrc(
    isSaying: Boolean,
    messageChain: MessageChain,
    time: Long = currentTimeSeconds(),
    constructor: MsgSrcConstructor<R>,
): R {
    val db = bot.msgDatabase
    val info = if (isSaying) {
        db.newMessageInfo(
            sender = id,
            subject = when (this) {
                is Member -> group.id
                is Stranger,
                is Friend,
                -> this.id

                else -> error("Invalid contact: $this")
            },
            kind = when (this) {
                is Member -> MessageSourceKind.GROUP
                is Stranger -> MessageSourceKind.STRANGER
                is Friend -> MessageSourceKind.FRIEND
                else -> error("Invalid contact: $this")
            },
            message = messageChain,
            time = time,
        )
    } else {
        db.newMessageInfo(
            sender = bot.id,
            subject = this.id,
            kind = when (this) {
                is NormalMember -> MessageSourceKind.TEMP
                is Stranger -> MessageSourceKind.STRANGER
                is Friend -> MessageSourceKind.FRIEND
                is Group -> MessageSourceKind.GROUP
                else -> error("Invalid contact: $this")
            },
            message = messageChain,
            time = time,
        )
    }
    return constructor(
        intArrayOf(info.id),
        intArrayOf(info.internal),
        info.time.toInt(),
    )
}

