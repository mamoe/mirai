@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "MemberVisibilityCanBePrivate", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.data.ImageId
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.timpc.TIMPCBot
import net.mamoe.mirai.timpc.internal.RawGroupInfo
import net.mamoe.mirai.timpc.network.packet.action.*
import net.mamoe.mirai.timpc.network.packet.event.MemberJoinEventPacket
import net.mamoe.mirai.timpc.network.packet.event.MemberQuitEvent
import net.mamoe.mirai.timpc.sendPacket
import net.mamoe.mirai.timpc.utils.assertUnreachable
import net.mamoe.mirai.timpc.withTIMPCBot
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.coroutines.CoroutineContext

internal sealed class ContactImpl : Contact {
    abstract override suspend fun sendMessage(message: MessageChain)

    /**
     * 开始监听事件, 以同步更新资料
     */
    internal abstract fun startUpdater()
}

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
internal class GroupImpl internal constructor(bot: TIMPCBot, val groupId: GroupId, override val coroutineContext: CoroutineContext) :
    ContactImpl(), Group, CoroutineScope {
    override val bot: TIMPCBot by bot.unsafeWeakRef()

    override val id: Long get() = groupId.value
    override val internalId = GroupId(id).toInternalId()

    internal lateinit var info: GroupInfo
    internal lateinit var initialInfoJob: Job

    override val owner: Member get() = info.owner
    override val name: String get() = info.name
    override val announcement: String get() = info.announcement
    override val members: ContactList<Member> get() = info.members

    override fun getMember(id: Long): Member =
        members.getOrNull(id) ?: throw NoSuchElementException("No such member whose id is $id in group ${groupId.value}")

    override suspend fun sendMessage(message: MessageChain) {
        bot.sendPacket(GroupPacket.Message(bot.qqAccount, internalId, bot.sessionKey, message))
    }

    override suspend fun uploadImage(image: ExternalImage): ImageId = withTIMPCBot {
        val userContext = coroutineContext
        val response = GroupImagePacket.RequestImageId(bot.qqAccount, internalId, image, sessionKey).sendAndExpect<GroupImageResponse>()

        withContext(userContext) {
            when (response) {
                is ImageUploadInfo -> response.uKey?.let { uKey ->
                    check(Http.postImage(
                        htcmd = "0x6ff0071",
                        uin = bot.qqAccount,
                        groupId = GroupId(id),
                        imageInput = image.input,
                        inputSize = image.inputSize,
                        uKeyHex = uKey.toUHexString("").also { require(it.length == 128 * 2) { "Illegal uKey. expected size=256, actual size=${it.length}" } }
                    )) { "Group image upload failed: cannot access api" }
                    logger.verbose("group image uploaded")
                } ?: logger.verbose("Group image upload: already exists")

                // TODO: 2019/11/17 超过大小的情况
                //is Overfile -> throw OverFileSizeMaxException()
                else -> assertUnreachable()
            }
        }

        return image.groupImageId
    }

    override suspend fun updateGroupInfo(): GroupInfo = withTIMPCBot {
        GroupPacket.QueryGroupInfo(qqAccount, internalId, sessionKey).sendAndExpect<RawGroupInfo>().parseBy(this@GroupImpl).also { info = it }
    }

    override suspend fun quit(): Boolean = withTIMPCBot {
        GroupPacket.QuitGroup(qqAccount, sessionKey, internalId).sendAndExpect<GroupPacket.QuitGroupResponse>().isSuccess
    }

    @UseExperimental(MiraiInternalAPI::class)
    override fun startUpdater() {
        subscribeAlways<MemberJoinEventPacket> {
            members.delegate.addLast(it.member)
        }
        subscribeAlways<MemberQuitEvent> {
            members.delegate.remove(it.member)
        }
    }

    override fun toString(): String = "Group(${this.id})"
}

@PublishedApi
internal class QQImpl @PublishedApi internal constructor(bot: TIMPCBot, override val id: Long, override val coroutineContext: CoroutineContext) :
    ContactImpl(),
    QQ, CoroutineScope {
    override val bot: TIMPCBot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) =
        bot.sendPacket(SendFriendMessagePacket(bot.qqAccount, id, bot.sessionKey, message))

    override suspend fun uploadImage(image: ExternalImage): ImageId = withTIMPCBot {
        FriendImagePacket.RequestImageId(qqAccount, sessionKey, id, image).sendAndExpect<FriendImageResponse>().let {
            when (it) {
                is FriendImageUKey -> {
                    check(
                        Http.postImage(
                            htcmd = "0x6ff0070",
                            uin = bot.qqAccount,
                            groupId = null,
                            uKeyHex = it.uKey.toUHexString(""),
                            imageInput = image.input,
                            inputSize = image.inputSize
                        )
                    ) { "Friend image upload failed: cannot access api" }
                    logger.verbose("friend image uploaded")
                    it.imageId
                }
                is FriendImageAlreadyExists -> it.imageId
                is FriendImageOverFileSizeMax -> throw OverFileSizeMaxException()
                else -> error("This shouldn't happen")
            }
        }
    }

    override suspend fun queryProfile(): Profile = withTIMPCBot {
        RequestProfileDetailsPacket(bot.qqAccount, id, sessionKey).sendAndExpect<RequestProfileDetailsResponse>().profile
    }

    override suspend fun queryPreviousNameList(): PreviousNameList = withTIMPCBot {
        QueryPreviousNamePacket(bot.qqAccount, sessionKey, id).sendAndExpect()
    }

    override suspend fun queryRemark(): FriendNameRemark = withTIMPCBot {
        QueryFriendRemarkPacket(bot.qqAccount, sessionKey, id).sendAndExpect()
    }

    @PublishedApi
    override fun startUpdater() {
        // TODO: 2019/11/28 被删除好友事件
    }

    override fun toString(): String = "QQ(${this.id})"
}

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

    override suspend fun mute(durationSeconds: Int): Boolean = withTIMPCBot {
        require(durationSeconds > 0) { "duration must be greater than 0 second" }
        require(durationSeconds <= 30 * 24 * 3600) { "duration must be no more than 30 days" }

        if (permission == MemberPermission.OWNER) return false
        val operator = group.getMember(bot.qqAccount)
        check(operator.id != id) { "The bot is the owner of group ${group.id}, it cannot mute itself!" }
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
    override fun startUpdater() {
        // TODO: 2019/12/6 更新群成员信息
    }

    override suspend fun unmute(): Unit = withTIMPCBot {
        GroupPacket.Mute(qqAccount, group.internalId, sessionKey, id, 0u).sendAndExpect<GroupPacket.MuteResponse>()
    }
}