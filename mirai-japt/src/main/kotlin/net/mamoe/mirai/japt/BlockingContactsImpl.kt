@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.japt

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.data.Profile
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.protocol.tim.packet.action.FriendNameRemark
import net.mamoe.mirai.network.protocol.tim.packet.action.GroupInfo
import net.mamoe.mirai.network.protocol.tim.packet.action.PreviousNameList

internal class BlockingQQImpl(private val delegate: QQ) : BlockingQQ {
    override fun getBot(): Bot = delegate.bot
    override fun getId(): Long = delegate.id.toLong()
    override fun sendMessage(messages: MessageChain) = runBlocking { delegate.sendMessage(messages) }
    override fun queryProfile(): Profile = runBlocking { delegate.queryProfile() }
    override fun queryPreviousNameList(): PreviousNameList = runBlocking { delegate.queryPreviousNameList() }
    override fun queryRemark(): FriendNameRemark = runBlocking { delegate.queryRemark() }
}

internal class BlockingGroupImpl(private val delegate: Group) : BlockingGroup {
    override fun sendMessage(messages: MessageChain) = runBlocking { delegate.sendMessage(messages) }
    override fun getOwner(): BlockingMember = delegate.owner.blocking()
    override fun getName(): String = delegate.name
    override fun getId(): Long = delegate.id.toLong()
    override fun updateGroupInfo(): GroupInfo = runBlocking { delegate.updateGroupInfo() }
    override fun toFullString(): String = delegate.toFullString()
    override fun getMember(id: Long): BlockingMember = delegate.getMember(id.toUInt()).blocking()
    override fun getBot(): Bot = delegate.bot
    override fun getAnnouncement(): String = delegate.announcement
    override fun getMembers(): MutableMap<Long, BlockingMember> = delegate.members.mapKeys { it.key.toLong() }.mapValues { it.value.blocking() }.toMutableMap()
    override fun getInternalId(): Long = delegate.internalId.value.toLong()
    override fun quit(): Boolean = runBlocking { delegate.quit().isSuccess }
}

internal class BlockingMemberImpl(private val delegate: Member) : BlockingMember {
    override fun getGroup(): BlockingGroup = delegate.group.blocking()
    override fun getPermission(): MemberPermission = delegate.permission
    override fun mute(durationSeconds: Int): Boolean = runBlocking { delegate.mute(durationSeconds) }
    override fun unmute() = runBlocking { delegate.unmute() }
}