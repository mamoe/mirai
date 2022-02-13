/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessagePreSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockAnonymousMember
import net.mamoe.mirai.mock.contact.MockGroup
import net.mamoe.mirai.mock.contact.MockMember
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcFromGroup
import net.mamoe.mirai.mock.internal.msgsrc.newMsgSrc
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.lateinitMutableProperty
import kotlin.coroutines.CoroutineContext

internal class MockAnonymousMemberImpl(
    parentCoroutineContext: CoroutineContext,
    bot: MockBot, id: Long,

    override val anonymousId: String,
    override val group: MockGroup,
    nameCard: String
) : AbstractMockContact(parentCoroutineContext, bot, id), MockAnonymousMember {
    override fun newMessagePreSend(message: Message): MessagePreSendEvent {
        throw AssertionError()
    }

    override var avatarUrl: String by lateinitMutableProperty { runBlocking { MockImage.random(bot).getUrl(bot) } }
    override suspend fun postMessagePreSend(message: MessageChain, receipt: MessageReceipt<*>) {
        throw AssertionError()
    }

    override fun newMessageSource(message: MessageChain): OnlineMessageSource.Outgoing {
        throw AssertionError()
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    override suspend fun sendMessage(message: Message): Nothing = super<MockAnonymousMember>.sendMessage(message)
    override suspend fun uploadImage(resource: ExternalResource): Image =
        super<AbstractMockContact>.uploadImage(resource)

    override var permission: MemberPermission
        get() = MemberPermission.MEMBER
        set(value) {
            error("Modifying permission of AnonymousMember")
        }
    override val specialTitle: String
        get() = "匿名"

    override suspend fun mute(durationSeconds: Int) {
    }

    override var remark: String
        get() = ""
        set(value) {}
    override var nick: String
        get() = nameCard
        set(value) {}

    override val nameCard: String
        get() = mockApi.nick

    override val mockApi: MockMember.MockApi = object : MockMember.MockApi {
        override val member: MockMember
            get() = this@MockAnonymousMemberImpl

        override var nick: String = nameCard

        override var remark: String
            get() = ""
            set(value) {}

        override var permission: MemberPermission
            get() = MemberPermission.MEMBER
            set(value) {}
    }


    override suspend fun says(message: MessageChain): MessageChain {
        val src = newMsgSrc(true, message) { ids, internalIds, time ->
            OnlineMsgSrcFromGroup(ids, internalIds, time, message, bot, this)
        }
        val msg = src withMessage message
        GroupMessageEvent(nameCardOrNick, permission, this, msg, src.time).broadcast()
        return msg
    }

    override fun toString(): String {
        return "$nick[匿名]"
    }
}