@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.contact.internal

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.data.Profile
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.chain
import net.mamoe.mirai.message.singleChain
import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.network.qqAccount
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.sendPacket
import net.mamoe.mirai.utils.SuspendLazy
import net.mamoe.mirai.withSession

internal sealed class ContactImpl : Contact {
    abstract override suspend fun sendMessage(message: MessageChain)

    //这两个方法应写为扩展函数, 但为方便 import 还是写在这里
    override suspend fun sendMessage(plain: String) = sendMessage(plain.singleChain())

    override suspend fun sendMessage(message: Message) = sendMessage(message.chain())
}

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
internal data class GroupImpl internal constructor(override val bot: Bot, val groupId: GroupId) : ContactImpl(), Group {
    override val id: UInt get() = groupId.value
    override val internalId = GroupId(id).toInternalId()

    private val _members: MutableContactList<Member> = MutableContactList()
    override val member: ContactList<Member> = ContactList(_members)
    private val membersLock: Mutex = Mutex()

    override suspend fun getMember(id: UInt): Member =
        if (_members.containsKey(id)) _members[id]!!
        else throw NoSuchElementException("No such member whose id is $id in group $id") /*membersLock.withLock {
            _members.getOrPut(id) { MemberImpl(bot.getQQ(id), this) }
        }*/

    override suspend fun sendMessage(message: MessageChain) {
        bot.sendPacket(GroupPacket.Message(bot.qqAccount, internalId, bot.sessionKey, message))
    }

    override suspend fun queryGroupInfo(): GroupInfo = bot.withSession {
        GroupPacket.QueryGroupInfo(qqAccount, internalId, sessionKey).sendAndExpect()
    }

    override fun toString(): String = "Group(${this.id})"
}

internal data class QQImpl internal constructor(override val bot: Bot, override val id: UInt) : ContactImpl(), QQ {
    private var _profile: Profile? = null
    private val _initialProfile by bot.network.SuspendLazy { updateProfile() }

    override val profile: Deferred<Profile> get() = if (_profile == null) _initialProfile else CompletableDeferred(_profile!!)

    override suspend fun sendMessage(message: MessageChain) =
        bot.sendPacket(SendFriendMessagePacket(bot.qqAccount, id, bot.sessionKey, message))

    override suspend fun updateProfile(): Profile = bot.withSession {
        _profile = RequestProfileDetailsPacket(bot.qqAccount, id, sessionKey)
            .sendAndExpect<RequestProfileDetailsResponse, Profile> { it.profile }

        return _profile!!
    }

    override suspend fun queryPreviousNameList(): PreviousNameList = bot.withSession {
        QueryPreviousNamePacket(bot.qqAccount, sessionKey, id).sendAndExpect()
    }

    override fun toString(): String = "QQ(${this.id})"
}

/**
 * 群成员
 */
internal data class MemberImpl(private val delegate: QQ, override val group: Group, override val permission: MemberPermission) : QQ by delegate, Member {
    override fun toString(): String = "Member(id=${this.id}, permission=$permission)"
}