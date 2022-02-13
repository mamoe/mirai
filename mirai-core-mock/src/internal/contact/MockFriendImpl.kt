/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockFriend
import net.mamoe.mirai.mock.internal.contact.roaming.MockRoamingMessages
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcFromFriend
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcToFriend
import net.mamoe.mirai.mock.internal.msgsrc.newMsgSrc
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.lateinitMutableProperty
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext

internal class MockFriendImpl(
    parentCoroutineContext: CoroutineContext,
    bot: MockBot,
    id: Long,
    nick: String,
    remark: String
) : AbstractMockContact(
    parentCoroutineContext,
    bot, id
), MockFriend {
    override val mockApi: MockFriend.MockApi = object : MockFriend.MockApi {
        override val contact: MockFriend get() = this@MockFriendImpl

        override var nick: String = nick
        override var remark: String = remark
        override var avatarUrl: String by lateinitMutableProperty { runBlocking { MockImage.random(bot).getUrl(bot) } }
    }
    override var avatarUrl: String
        get() = mockApi.avatarUrl
        set(value) {
            mockApi.avatarUrl = value
            FriendAvatarChangedEvent(this).broadcastBlocking()
        }

    override var nick: String
        get() = mockApi.nick
        set(value) {
            val ov = mockApi.nick
            if (ov == value) return
            mockApi.nick = value
            FriendNickChangedEvent(this, ov, value).broadcastBlocking()
        }

    override var remark: String
        get() = mockApi.remark
        set(value) {
            val ov = mockApi.remark
            if (ov == value) return
            mockApi.remark = value
            FriendRemarkChangeEvent(this, ov, value).broadcastBlocking()
        }

    override fun newMessagePreSend(message: Message): MessagePreSendEvent {
        return FriendMessagePreSendEvent(this, message)
    }

    override suspend fun postMessagePreSend(message: MessageChain, receipt: MessageReceipt<*>) {
        FriendMessagePostSendEvent(this, message, null, receipt.cast()).broadcast()
    }

    override fun newMessageSource(message: MessageChain): OnlineMessageSource.Outgoing {
        return newMsgSrc(false, message) { ids, internalIds, time ->
            OnlineMsgSrcToFriend(ids, internalIds, time, message, bot, bot, this)
        }
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        return super<AbstractMockContact>.sendMessage(message).cast()
    }

    override suspend fun delete() {
        if (bot.friends.delegate.remove(this)) {
            FriendDeleteEvent(this).broadcast()
            cancel(CancellationException("Friend deleted"))
        }
    }

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio =
        resource.mockUploadAudio(bot)

    override val roamingMessages: RoamingMessages = MockRoamingMessages(this)

    override suspend fun says(message: MessageChain): MessageChain {
        val src = newMsgSrc(true, message) { ids, internalIds, time ->
            OnlineMsgSrcFromFriend(ids, internalIds, time, message, bot, this)
        }
        val msg = src withMessage message
        FriendMessageEvent(this, msg, src.time).broadcast()
        return msg
    }

    override suspend fun broadcastMsgSyncEvent(message: MessageChain, time: Int) {
        val src = newMsgSrc(true, message, time.toLong()) { ids, internalIds, time0 ->
            OnlineMsgSrcToFriend(ids, internalIds, time0, message, bot, bot, this)
        }
        val msg = src withMessage message
        FriendMessageSyncEvent(this, msg, time).broadcast()
    }

    override fun toString(): String {
        return "$nick($id)"
    }
}