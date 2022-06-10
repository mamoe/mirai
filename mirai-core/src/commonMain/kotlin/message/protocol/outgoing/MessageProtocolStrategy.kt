/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.outgoing

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToFriendImpl
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.components.ClockHolder.Companion.clock
import net.mamoe.mirai.internal.network.notice.group.GroupMessageProcessor
import net.mamoe.mirai.internal.network.notice.priv.PrivateMessageProcessor
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource

internal interface MessageProtocolStrategy<in C : AbstractContact> {
    suspend fun sendPacket(bot: AbstractBot, packet: OutgoingPacket): Packet? {
        return bot.network.sendAndExpect(packet)
    }

    suspend fun <R : Packet?> sendPacket(bot: AbstractBot, packet: OutgoingPacketWithRespType<R>): R {
        return bot.network.sendAndExpect(packet)
    }

    /**
     * If [fragmented] is false, returned list must contain at most one element.
     */
    suspend fun createPacketsForGeneralMessage(
        client: QQAndroidClient,
        contact: C,
        message: MessageChain, // to send
        originalMessage: MessageChain, // to create Receipt
        fragmented: Boolean,
        sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit,
    ): List<OutgoingPacket>

    suspend fun constructSourceForSpecialMessage(
        originalMessage: MessageChain,
        fromAppId: Int,
    ): OnlineMessageSource.Outgoing

    companion object : ComponentKey<MessageProtocolStrategy<*>>
}

internal sealed class UserMessageProtocolStrategy<C : AbstractUser> : MessageProtocolStrategy<C> {
    override suspend fun constructSourceForSpecialMessage(
        originalMessage: MessageChain,
        fromAppId: Int
    ): OnlineMessageSource.Outgoing {
        throw UnsupportedOperationException("Sending MusicShare or FileMessage to User is not yet supported")
    }
}

internal class FriendMessageProtocolStrategy(
    private val contact: FriendImpl,
) : UserMessageProtocolStrategy<FriendImpl>() {
    override suspend fun createPacketsForGeneralMessage(
        client: QQAndroidClient,
        contact: FriendImpl,
        message: MessageChain,
        originalMessage: MessageChain,
        fragmented: Boolean,
        sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit
    ): List<OutgoingPacket> {
        return MessageSvcPbSendMsg.createToFriend(client, contact, message, originalMessage, fragmented, sourceCallback)
    }

    override suspend fun constructSourceForSpecialMessage(
        originalMessage: MessageChain,
        fromAppId: Int
    ): OnlineMessageSource.Outgoing {
        val receipt: PrivateMessageProcessor.SendPrivateMessageReceipt = withTimeoutOrNull(3000) {
            GlobalEventChannel.parentScope(this).nextEvent(EventPriority.MONITOR) {
                it.bot === contact.bot && it.fromAppId == fromAppId
            }
        } ?: PrivateMessageProcessor.SendPrivateMessageReceipt.EMPTY

        return OnlineMessageSourceToFriendImpl(
            internalIds = intArrayOf(receipt.messageRandom),
            sequenceIds = intArrayOf(receipt.sequenceId),
            sender = contact.bot,
            target = contact,
            time = contact.bot.clock.server.currentTimeSeconds().toInt(),
            originalMessage = originalMessage
        )
    }
}

internal object StrangerMessageProtocolStrategy : UserMessageProtocolStrategy<StrangerImpl>() {
    override suspend fun createPacketsForGeneralMessage(
        client: QQAndroidClient,
        contact: StrangerImpl,
        message: MessageChain,
        originalMessage: MessageChain,
        fragmented: Boolean,
        sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit
    ): List<OutgoingPacket> {
        return MessageSvcPbSendMsg.createToStranger(
            client,
            contact,
            message,
            originalMessage,
            fragmented,
            sourceCallback
        )
    }
}

internal object GroupTempMessageProtocolStrategy : UserMessageProtocolStrategy<NormalMemberImpl>() {
    override suspend fun createPacketsForGeneralMessage(
        client: QQAndroidClient,
        contact: NormalMemberImpl,
        message: MessageChain,
        originalMessage: MessageChain,
        fragmented: Boolean,
        sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit
    ): List<OutgoingPacket> {
        return MessageSvcPbSendMsg.createToTemp(client, contact, message, originalMessage, fragmented, sourceCallback)
    }
}

internal open class GroupMessageProtocolStrategy(
    private val contact: GroupImpl,
) : MessageProtocolStrategy<GroupImpl> {
    override suspend fun createPacketsForGeneralMessage(
        client: QQAndroidClient,
        contact: GroupImpl,
        message: MessageChain,
        originalMessage: MessageChain,
        fragmented: Boolean,
        sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit
    ): List<OutgoingPacket> {
        return MessageSvcPbSendMsg.createToGroup(client, contact, message, originalMessage, fragmented, sourceCallback)
    }

    override suspend fun constructSourceForSpecialMessage(
        originalMessage: MessageChain,
        fromAppId: Int
    ): OnlineMessageSource.Outgoing {
        val receipt: GroupMessageProcessor.SendGroupMessageReceipt = withTimeoutOrNull(3000) {
            GlobalEventChannel.parentScope(this).nextEvent(EventPriority.MONITOR) {
                it.bot === contact.bot && it.fromAppId == fromAppId
            }
        } ?: GroupMessageProcessor.SendGroupMessageReceipt.EMPTY

        return OnlineMessageSourceToGroupImpl(
            contact,
            internalIds = intArrayOf(receipt.messageRandom),
            providedSequenceIds = intArrayOf(receipt.sequenceId),
            sender = contact.bot,
            target = contact,
            time = contact.bot.clock.server.currentTimeSeconds().toInt(),
            originalMessage = originalMessage
        )
    }

}