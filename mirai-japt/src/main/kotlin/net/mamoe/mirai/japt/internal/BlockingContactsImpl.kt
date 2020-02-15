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
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.japt.BlockingBot
import net.mamoe.mirai.japt.BlockingGroup
import net.mamoe.mirai.japt.BlockingMember
import net.mamoe.mirai.japt.BlockingQQ
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.toList

internal class BlockingQQImpl(private val delegate: QQ) : BlockingQQ {
    override fun getBot(): BlockingBot = delegate.bot.blocking()
    override fun getId(): Long = delegate.id
    override fun getNick(): String = delegate.nick

    override fun sendMessage(messages: MessageChain) = runBlocking { delegate.sendMessage(messages) }
    override fun sendMessage(message: String) = runBlocking { delegate.sendMessage(message.toMessage().toChain()) }
    override fun sendMessage(message: Message) = runBlocking { delegate.sendMessage(message.toChain()) }
    override fun uploadImage(image: ExternalImage): Image = runBlocking { delegate.uploadImage(image) }

    @MiraiExperimentalAPI
    override fun queryProfile(): Profile = runBlocking { delegate.queryProfile() }

    @MiraiExperimentalAPI
    override fun queryPreviousNameList(): PreviousNameList = runBlocking { delegate.queryPreviousNameList() }

    @MiraiExperimentalAPI
    override fun queryRemark(): FriendNameRemark = runBlocking { delegate.queryRemark() }
}

internal class BlockingGroupImpl(private val delegate: Group) : BlockingGroup {
    override fun sendMessage(messages: MessageChain) = runBlocking { delegate.sendMessage(messages) }
    override fun sendMessage(message: String) = runBlocking { delegate.sendMessage(message.toMessage().toChain()) }
    override fun sendMessage(message: Message) = runBlocking { delegate.sendMessage(message.toChain()) }
    override fun getOwner(): BlockingMember = delegate.owner.blocking()
    @MiraiExperimentalAPI
    override fun newMember(memberInfo: MemberInfo): Member = delegate.Member(memberInfo)

    override fun uploadImage(image: ExternalImage): Image = runBlocking { delegate.uploadImage(image) }
    override fun setEntranceAnnouncement(announcement: String) {
        delegate.entranceAnnouncement = announcement
    }

    override fun getName(): String = delegate.name
    override fun getId(): Long = delegate.id
    @MiraiExperimentalAPI
    override fun getBotPermission(): MemberPermission = delegate.botPermission

    override fun setConfessTalk(enabled: Boolean) {
        delegate.isConfessTalkEnabled = enabled
    }

    override fun isAnonymousChatEnabled(): Boolean = delegate.isAnonymousChatEnabled

    override fun isAutoApproveEnabled(): Boolean = delegate.isAutoApproveEnabled

    override fun isConfessTalkEnabled(): Boolean = delegate.isConfessTalkEnabled

    override fun toFullString(): String = delegate.toFullString()
    override fun containsMember(id: Long): Boolean = delegate.contains(id)

    override fun isAllowMemberInvite(): Boolean = delegate.isAllowMemberInvite

    override fun getMember(id: Long): BlockingMember = delegate[id].blocking()
    override fun getBot(): BlockingBot = delegate.bot.blocking()
    override fun getBotMuteRemaining(): Int = delegate.botMuteRemaining

    override fun isMuteAll(): Boolean = delegate.isMuteAll

    override fun setName(name: String) {
        delegate.name = name
    }

    override fun setMuteAll(enabled: Boolean) {
        delegate.isMuteAll = enabled
    }

    override fun getEntranceAnnouncement(): String = delegate.entranceAnnouncement
    @UseExperimental(MiraiInternalAPI::class)
    override fun getMembers(): List<BlockingMember> =
        delegate.members.delegate.toList().map { it.blocking() }

    override fun setAllowMemberInvite(allow: Boolean) {
        delegate.isAllowMemberInvite = allow
    }

    override fun getMemberOrNull(id: Long): BlockingMember? {
        return delegate.getOrNull(id)?.blocking()
    }

    override fun quit(): Boolean = runBlocking { delegate.quit() }
}

internal class BlockingMemberImpl(private val delegate: Member) : BlockingMember, BlockingQQ by (delegate as QQ).blocking() {
    override fun getGroup(): BlockingGroup = delegate.group.blocking()
    override fun getNameCard(): String = delegate.nameCard

    override fun getPermission(): MemberPermission = delegate.permission
    override fun setNameCard(nameCard: String) {
        delegate.nameCard = nameCard
    }

    override fun mute(durationSeconds: Int) = runBlocking { delegate.mute(durationSeconds) }
    override fun unmute() = runBlocking { delegate.unmute() }
    override fun kick(message: String) {
        runBlocking { delegate.kick(message) }
    }
}