/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockFriend
import net.mamoe.mirai.mock.contact.MockGroup
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcFromGroup
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcToTemp
import net.mamoe.mirai.mock.internal.msgsrc.newMsgSrc
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.lateinitMutableProperty
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max

internal class MockNormalMemberImpl(
    parentCoroutineContext: CoroutineContext,
    bot: MockBot,
    id: Long,
    override val group: MockGroup,
    permission: MemberPermission,
    remark: String,
    nick: String,
    muteTimeRemaining: Int,
    joinTimestamp: Int,
    lastSpeakTimestamp: Int,
    specialTitle: String,
    nameCard: String,
) : AbstractMockContact(
    parentCoroutineContext, bot,
    id
), MockNormalMember {
    override var avatarUrl: String by lateinitMutableProperty { runBlocking { MockImage.random(bot).getUrl(bot) } }
    private inline fun <T> crossFriendAccess(
        ifExists: (MockFriend) -> T,
        ifNotExists: () -> T,
    ): T {
        val f = bot.getFriend(id) ?: return ifNotExists()
        return ifExists(f)
    }

    override val mockApi: MockNormalMember.MockApi = object : MockNormalMember.MockApi {
        override val member: MockNormalMember get() = this@MockNormalMemberImpl
        override var lastSpeakTimestamp: Int = lastSpeakTimestamp
        override var joinTimestamp: Int = joinTimestamp
        override var muteTimeEndTimestamp: Long = currentTimeSeconds() + muteTimeRemaining

        override var nick: String = nick
            get() = crossFriendAccess(ifExists = { it.nick }, ifNotExists = { field })
            set(value) {
                crossFriendAccess(ifExists = { it.mockApi.nick = value }, ifNotExists = { field = value })
            }

        override var remark: String = remark
            get() = crossFriendAccess(ifExists = { it.remark }, ifNotExists = { field })
            set(value) {
                crossFriendAccess(ifExists = { it.mockApi.remark = value }, ifNotExists = { field = value })
            }

        override var permission: MemberPermission = permission
        override var nameCard: String = nameCard
        override var specialTitle: String = specialTitle
    }

    override val permission: MemberPermission
        get() = mockApi.permission

    override val joinTimestamp: Int
        get() = mockApi.joinTimestamp

    override val lastSpeakTimestamp: Int
        get() = mockApi.lastSpeakTimestamp

    override val muteTimeRemaining: Int
        get() = max((currentTimeSeconds() - mockApi.muteTimeEndTimestamp).toInt(), 0)

    override val remark: String
        get() = mockApi.remark

    override var nameCard: String
        get() = mockApi.nameCard
        set(value) {
            if (!group.botPermission.isOperator()) {
                throw PermissionDeniedException("Bot don't have permission to change the namecard of $this")
            }
            MemberCardChangeEvent(mockApi.nameCard, value, this).broadcastBlocking()
            mockApi.nameCard = value
        }

    override var specialTitle: String
        get() = mockApi.specialTitle
        set(value) {
            if (group.botPermission != MemberPermission.OWNER) {
                throw PermissionDeniedException("Bot is not the owner of $group so bot cannot change the specialTitle of $this")
            }
            MemberSpecialTitleChangeEvent(mockApi.specialTitle, value, this, group.botAsMember).broadcastBlocking()
            mockApi.specialTitle = value
        }

    override val nick: String
        get() = mockApi.nick

    override suspend fun unmute() {
        requireBotPermissionHigherThanThis("unmute")
        mockApi.muteTimeEndTimestamp = 0
        MemberUnmuteEvent(this, null)
    }

    override suspend fun kick(message: String, block: Boolean) {
        kick(message)
    }

    override suspend fun kick(message: String) {
        requireBotPermissionHigherThanThis("kick")
        if (group.members.delegate.remove(this)) {
            MemberLeaveEvent.Kick(this, group.botAsMember).broadcastBlocking()
            cancel(CancellationException("Member kicked: $message"))
        }
    }

    override suspend fun modifyAdmin(operation: Boolean) {
        if (group.botPermission != MemberPermission.OWNER) {
            throw PermissionDeniedException("Bot is not the owner of group ${group.id}, can't modify the permission of $id($permission")
        }
        if (operation && permission > MemberPermission.MEMBER) return

        if (permission == MemberPermission.OWNER) {
            throw IllegalArgumentException("Not allowed modify permission of owner ($id, $permission)")
        }
        val newPerm = if (operation) MemberPermission.ADMINISTRATOR else MemberPermission.MEMBER
        if (newPerm != permission) {
            val oldPerm = permission
            mockApi.permission = oldPerm
            MemberPermissionChangeEvent(this, oldPerm, newPerm).broadcast()
        }
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<NormalMember> {
        return super<AbstractMockContact>.sendMessage(message).cast()
    }

    override suspend fun mute(durationSeconds: Int) {
        requireBotPermissionHigherThanThis("mute")
        require(durationSeconds > 0) {
            "$durationSeconds < 0"
        }
        mockApi.muteTimeEndTimestamp = currentTimeSeconds() + durationSeconds
        MemberMuteEvent(this, durationSeconds, null)
    }

    override suspend fun broadcastMute(target: MockNormalMember, durationSeconds: Int) {
        target.mockApi.muteTimeEndTimestamp = currentTimeSeconds() + durationSeconds
        if (target.id == bot.id) {
            if (durationSeconds == 0) {
                BotUnmuteEvent(this)
            } else {
                BotMuteEvent(durationSeconds, this)
            }
        } else {
            if (durationSeconds == 0) {
                MemberUnmuteEvent(target, this)
            } else {
                MemberMuteEvent(target, durationSeconds, this)
            }
        }.broadcast()
    }

    override suspend fun says(message: MessageChain): MessageChain {
        val src = newMsgSrc(true, message) { ids, internalIds, time ->
            mockApi.lastSpeakTimestamp = time
            OnlineMsgSrcFromGroup(ids, internalIds, time, message, bot, this)
        }
        val msg = src withMessage message
        GroupMessageEvent(nameCardOrNick, permission, this, msg, src.time).broadcast()
        return msg
    }

    override fun newMessagePreSend(message: Message): MessagePreSendEvent {
        return GroupTempMessagePreSendEvent(this, message)
    }

    override suspend fun postMessagePreSend(message: MessageChain, receipt: MessageReceipt<*>) {
        GroupTempMessagePostSendEvent(this, message, null, receipt.cast()).broadcast()
    }

    override fun newMessageSource(message: MessageChain): OnlineMessageSource.Outgoing {
        return newMsgSrc(false, message) { ids, internalIds, time ->
            OnlineMsgSrcToTemp(ids, internalIds, time, message, bot, bot, this)
        }
    }

    override fun toString(): String {
        return "$nameCardOrNick[$id]"
    }
}