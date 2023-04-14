/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.cancel
import net.mamoe.mirai.contact.AvatarSpec
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.contact.friendgroup.FriendGroup
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
import net.mamoe.mirai.mock.internal.contact.friendfroup.MockFriendGroups
import net.mamoe.mirai.mock.internal.contact.roaming.MockRoamingMessages
import net.mamoe.mirai.mock.internal.impl
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcFromFriend
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcToFriend
import net.mamoe.mirai.mock.internal.msgsrc.newMsgSrc
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.cast
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
    private val ccinfo = bot.impl().contactDatabase.acquireCI(id, nick)

    override val mockApi: MockFriend.MockApi = object : MockFriend.MockApi {
        override val contact: MockFriend get() = this@MockFriendImpl

        override var remark: String = remark

        override var nick: String by ccinfo::nick
        override var avatarUrl: String by ccinfo::avatarUrl

        override var friendGroupId: Int = 0
    }

    override val friendGroup: FriendGroup
        get() = bot.friendGroups.cast<MockFriendGroups>().findOrDefault(mockApi.friendGroupId)

    override val avatarUrl: String get() = ccinfo.avatarUrl
    internal fun initAvatarUrl(v: String) {
        ccinfo.avatarUrl = v
    }

    override fun changeAvatarUrl(newAvatar: String) {
        ccinfo.changeAvatarUrl(newAvatar)
    }

    override fun avatarUrl(spec: AvatarSpec): String {
        return avatarUrl
    }

    override var nick: String
        get() = ccinfo.nick
        set(value) {
            ccinfo.changeNick(value)
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
            OnlineMsgSrcFromFriend(ids, internalIds, time, message, bot, this, bot)
        }
        val msg = src.withMessage(message)
        FriendMessageEvent(this, msg, src.time).broadcast()
        return msg
    }

    override suspend fun broadcastMsgSyncEvent(client: OtherClient, message: MessageChain, time: Int) {
        val src = newMsgSrc(true, message, time.toLong()) { ids, internalIds, time0 ->
            OnlineMsgSrcToFriend(ids, internalIds, time0, message, bot, bot, this)
        }
        val msg = src.withMessage(message)
        FriendMessageSyncEvent(client, this, msg, time).broadcast()
    }

    override fun toString(): String {
        return "Friend($id)"
    }
}