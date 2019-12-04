@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "MemberVisibilityCanBePrivate", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.contact.internal

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.data.Profile
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.chain
import net.mamoe.mirai.message.singleChain
import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.network.protocol.tim.packet.event.MemberJoinEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.MemberQuitEvent
import net.mamoe.mirai.network.qqAccount
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.sendPacket
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.logStacktrace
import net.mamoe.mirai.withSession
import kotlin.coroutines.CoroutineContext

internal sealed class ContactImpl : Contact {
    abstract override suspend fun sendMessage(message: MessageChain)

    //这两个方法应写为扩展函数, 但为方便 import 还是写在这里
    override suspend fun sendMessage(plain: String) = sendMessage(plain.singleChain())

    override suspend fun sendMessage(message: Message) = sendMessage(message.chain())

    /**
     * 开始监听事件, 以同步更新资料
     */
    internal abstract suspend fun CoroutineContext.startUpdater()
}

/**
 * 构造 [Group]
 */
@Suppress("FunctionName")
@PublishedApi
internal suspend fun Group(bot: Bot, groupId: GroupId, context: CoroutineContext): Group {
    val info: RawGroupInfo = try {
        bot.withSession { GroupPacket.QueryGroupInfo(qqAccount, groupId.toInternalId(), sessionKey).sendAndExpect() }
    } catch (e: Exception) {
        e.logStacktrace()
        error("Cannot obtain group info for id ${groupId.value}")
    }
    return GroupImpl(bot, groupId, context).apply { this.info = info.parseBy(this) }
}

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
internal data class GroupImpl internal constructor(override val bot: Bot, val groupId: GroupId, override val coroutineContext: CoroutineContext) :
    ContactImpl(), Group, CoroutineScope {
    override val id: UInt get() = groupId.value
    override val internalId = GroupId(id).toInternalId()

    internal lateinit var info: GroupInfo
    override val owner: Member get() = info.owner
    override val name: String get() = info.name
    override val announcement: String get() = info.announcement
    override val members: ContactList<Member> get() = info.members

    override fun getMember(id: UInt): Member =
        if (members.containsKey(id)) members[id]!!
        else throw NoSuchElementException("No such member whose id is ${id.toLong()} in group ${groupId.value.toLong()}")

    override suspend fun sendMessage(message: MessageChain) {
        bot.sendPacket(GroupPacket.Message(bot.qqAccount, internalId, bot.sessionKey, message))
    }

    override suspend fun updateGroupInfo(): GroupInfo = bot.withSession {
        GroupPacket.QueryGroupInfo(qqAccount, internalId, sessionKey).sendAndExpect<RawGroupInfo>().parseBy(this@GroupImpl)
    }

    override suspend fun quit(): QuitGroupResponse = bot.withSession {
        GroupPacket.QuitGroup(qqAccount, sessionKey, internalId).sendAndExpect()
    }

    @UseExperimental(MiraiInternalAPI::class)
    override suspend fun CoroutineContext.startUpdater() {
        subscribeAlways<MemberJoinEventPacket> {
            // FIXME: 2019/11/29 非线程安全!!
            members.delegate[it.member.id] = it.member
        }
        subscribeAlways<MemberQuitEvent> {
            // FIXME: 2019/11/29 非线程安全!!
            members.delegate.remove(it.member.id)
        }
    }

    override fun toString(): String = "Group(${this.id})"

    override fun iterator(): Iterator<Member> = members.delegate.values.iterator()
}

internal data class QQImpl internal constructor(override val bot: Bot, override val id: UInt, override val coroutineContext: CoroutineContext) : ContactImpl(),
    QQ, CoroutineScope {
    override suspend fun sendMessage(message: MessageChain) =
        bot.sendPacket(SendFriendMessagePacket(bot.qqAccount, id, bot.sessionKey, message))

    override suspend fun queryProfile(): Profile = bot.withSession {
        RequestProfileDetailsPacket(bot.qqAccount, id, sessionKey).sendAndExpect<RequestProfileDetailsResponse>().profile
    }

    override suspend fun queryPreviousNameList(): PreviousNameList = bot.withSession {
        QueryPreviousNamePacket(bot.qqAccount, sessionKey, id).sendAndExpect()
    }

    override suspend fun queryRemark(): FriendNameRemark = bot.withSession {
        QueryFriendRemarkPacket(bot.qqAccount, sessionKey, id).sendAndExpect()
    }

    override suspend fun CoroutineContext.startUpdater() {
        // TODO: 2019/11/28 被删除好友事件
    }

    override fun toString(): String = "QQ(${this.id})"
}

/**
 * 群成员
 */
@PublishedApi
internal data class MemberImpl(private val delegate: QQ, override val group: Group, override val permission: MemberPermission) : QQ by delegate, Member {
    override fun toString(): String = "Member(id=${this.id}, group=${group.id}, permission=$permission)"

    override suspend fun mute(durationSeconds: Int): Boolean = bot.withSession {
        require(durationSeconds > 0) { "duration must be greater than 0 second" }

        if (permission == MemberPermission.OWNER) return false

        when (group.getMember(bot.qqAccount).permission) {
            MemberPermission.MEMBER -> return false
            MemberPermission.ADMINISTRATOR -> if (permission == MemberPermission.ADMINISTRATOR) return false
            MemberPermission.OWNER -> {
            }
        }

        GroupPacket.Mute(qqAccount, group.internalId, sessionKey, id, durationSeconds.toUInt()).sendAndExpect<GroupPacket.MuteResponse>()
        return true
    }

    override suspend fun unmute(): Unit = bot.withSession {
        GroupPacket.Mute(qqAccount, group.internalId, sessionKey, id, 0u).sendAndExpect<GroupPacket.MuteResponse>()
    }
}