/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.japt.internal

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.japt.BlockingBot
import net.mamoe.mirai.japt.BlockingGroup
import net.mamoe.mirai.japt.BlockingMember
import net.mamoe.mirai.japt.BlockingQQ
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.toChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.toList

internal class BlockingQQImpl(private val delegate: QQ) : BlockingQQ {
    override fun getBot(): BlockingBot = delegate.bot.blocking()
    override fun getId(): Long = delegate.id
    override fun sendMessage(messages: MessageChain) = runBlocking { delegate.sendMessage(messages) }
    override fun sendMessage(message: String) = runBlocking { delegate.sendMessage(message.toMessage().toChain()) }
    override fun sendMessage(message: Message) = runBlocking { delegate.sendMessage(message.toChain()) }

    override fun queryProfile(): Profile = runBlocking { delegate.queryProfile() }
    override fun queryPreviousNameList(): PreviousNameList = runBlocking { delegate.queryPreviousNameList() }
    override fun queryRemark(): FriendNameRemark = runBlocking { delegate.queryRemark() }
}

internal class BlockingGroupImpl(private val delegate: Group) : BlockingGroup {
    override fun sendMessage(messages: MessageChain) = runBlocking { delegate.sendMessage(messages) }
    override fun sendMessage(message: String) = runBlocking { delegate.sendMessage(message.toMessage().toChain()) }
    override fun sendMessage(message: Message) = runBlocking { delegate.sendMessage(message.toChain()) }
    override fun getOwner(): BlockingMember = delegate.owner.blocking()
    override fun getName(): String = delegate.name
    override fun getId(): Long = delegate.id
    override fun toFullString(): String = delegate.toFullString()
    override fun getMember(id: Long): BlockingMember = delegate[id].blocking()
    override fun getBot(): BlockingBot = delegate.bot.blocking()
    override fun getAnnouncement(): String = delegate.entranceAnnouncement
    @UseExperimental(MiraiInternalAPI::class)
    override fun getMembers(): Map<Long, BlockingMember> =
        delegate.members.delegate.toList().associateBy { it.id }.mapValues { it.value.blocking() }

    override fun quit(): Boolean = runBlocking { delegate.quit() }
}

internal class BlockingMemberImpl(private val delegate: Member) : BlockingMember {
    override fun getGroup(): BlockingGroup = delegate.group.blocking()
    override fun getPermission(): MemberPermission = delegate.permission
    override fun mute(durationSeconds: Int): Boolean = runBlocking { delegate.mute(durationSeconds) }
    override fun unmute() = runBlocking { delegate.unmute() }
}