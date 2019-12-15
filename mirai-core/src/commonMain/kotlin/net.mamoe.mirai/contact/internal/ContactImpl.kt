@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "MemberVisibilityCanBePrivate", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.contact.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.data.Profile
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.network.protocol.tim.packet.event.MemberJoinEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.MemberQuitEvent
import net.mamoe.mirai.network.qqAccount
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.sendPacket
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.withSession
import kotlin.coroutines.CoroutineContext

internal sealed class ContactImpl : Contact {
    abstract override suspend fun sendMessage(message: MessageChain)

    /**
     * 开始监听事件, 以同步更新资料
     */
    internal abstract suspend fun startUpdater()
}

/**
 * 构造 [Group]
 */
@Suppress("FunctionName")
@PublishedApi
internal fun CoroutineScope.Group(bot: Bot, groupId: GroupId, info: RawGroupInfo, context: CoroutineContext): Group =
    GroupImpl(bot, groupId, context).apply {
        this@apply.info = info.parseBy(this@apply)
        launch { startUpdater() }
    }


@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
internal data class GroupImpl internal constructor(override val bot: Bot, val groupId: GroupId, override val coroutineContext: CoroutineContext) :
    ContactImpl(), Group, CoroutineScope {
    override val id: UInt get() = groupId.value
    override val internalId = GroupId(id).toInternalId()

    internal lateinit var info: GroupInfo
    internal lateinit var initialInfoJob: Job

    override val owner: Member get() = info.owner
    override val name: String get() = info.name
    override val announcement: String get() = info.announcement
    override val members: ContactList<Member> get() = info.members

    override fun getMember(id: UInt): Member =
        members.getOrNull(id) ?: throw NoSuchElementException("No such member whose id is ${id.toLong()} in group ${groupId.value.toLong()}")

    override suspend fun sendMessage(message: MessageChain) {
        bot.sendPacket(GroupPacket.Message(bot.qqAccount, internalId, bot.sessionKey, message))
    }

    override suspend fun updateGroupInfo(): GroupInfo = bot.withSession {
        GroupPacket.QueryGroupInfo(qqAccount, internalId, sessionKey).sendAndExpect<RawGroupInfo>().parseBy(this@GroupImpl).also { info = it }
    }

    override suspend fun quit(): QuitGroupResponse = bot.withSession {
        GroupPacket.QuitGroup(qqAccount, sessionKey, internalId).sendAndExpect()
    }

    @UseExperimental(MiraiInternalAPI::class)
    override suspend fun startUpdater() {
        subscribeAlways<MemberJoinEventPacket> {
            members.delegate.addLast(it.member)
        }
        subscribeAlways<MemberQuitEvent> {
            members.delegate.remove(it.member)
        }
    }

    override fun toString(): String = "Group(${this.id})"
}

@Suppress("FunctionName", "NOTHING_TO_INLINE")
internal inline fun CoroutineScope.QQ(bot: Bot, id: UInt, coroutineContext: CoroutineContext): QQ = QQImpl(bot, id, coroutineContext).apply { launch { startUpdater() } }

@PublishedApi
internal data class QQImpl @PublishedApi internal constructor(override val bot: Bot, override val id: UInt, override val coroutineContext: CoroutineContext) :
    ContactImpl(),
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

    @PublishedApi
    override suspend fun startUpdater() {
        // TODO: 2019/11/28 被删除好友事件
    }

    override fun toString(): String = "QQ(${this.id})"
}

@Suppress("FunctionName", "NOTHING_TO_INLINE")
internal inline fun Group.Member(delegate: QQ, permission: MemberPermission, coroutineContext: CoroutineContext): Member =
    MemberImpl(delegate, this, permission, coroutineContext).apply { launch { startUpdater() } }

/**
 * 群成员
 */
@PublishedApi
internal data class MemberImpl(
    private val delegate: QQ,
    override val group: Group,
    override val permission: MemberPermission,
    override val coroutineContext: CoroutineContext
) : QQ by delegate, CoroutineScope, Member, ContactImpl() {
    override fun toString(): String = "Member(id=${this.id}, group=${group.id}, permission=$permission)"

    override suspend fun mute(durationSeconds: Int): Boolean = bot.withSession {
        require(durationSeconds > 0) { "duration must be greater than 0 second" }
        require(durationSeconds <= 30 * 24 * 3600) { "duration must be no more than 30 days" }

        if (permission == MemberPermission.OWNER) return false
        val operator = group.getMember(bot.qqAccount)
        check(operator.id != id) { "The bot is the owner of group ${group.id.toLong()}, it cannot mute itself!" }
        when (operator.permission) {
            MemberPermission.MEMBER -> return false
            MemberPermission.ADMINISTRATOR -> if (permission == MemberPermission.ADMINISTRATOR) return false
            MemberPermission.OWNER -> {
            }
        }

        GroupPacket.Mute(qqAccount, group.internalId, sessionKey, id, durationSeconds.toUInt()).sendAndExpect<GroupPacket.MuteResponse>()
        return true
    }

    @PublishedApi
    override suspend fun startUpdater() {
        // TODO: 2019/12/6 更新群成员信息
    }

    override suspend fun unmute(): Unit = bot.withSession {
        GroupPacket.Mute(qqAccount, group.internalId, sessionKey, id, 0u).sendAndExpect<GroupPacket.MuteResponse>()
    }
}