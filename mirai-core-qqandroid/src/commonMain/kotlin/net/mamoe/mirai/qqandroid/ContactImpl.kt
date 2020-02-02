package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.message.data.ImageId
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal abstract class ContactImpl : Contact

internal class QQImpl(bot: QQAndroidBot, override val coroutineContext: CoroutineContext, override val id: Long) : ContactImpl(), QQ {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        bot.network.run {
            check(
                MessageSvc.PbSendMsg.ToFriend(
                    bot.client,
                    id,
                    message
                ).sendAndExpect<MessageSvc.PbSendMsg.Response>() is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed" }
        }
    }

    override suspend fun uploadImage(image: ExternalImage): ImageId {
        TODO("not implemented")
    }

    override val isOnline: Boolean
        get() = true

    override suspend fun queryProfile(): Profile {
        TODO("not implemented")
    }

    override suspend fun queryPreviousNameList(): PreviousNameList {
        TODO("not implemented")
    }

    override suspend fun queryRemark(): FriendNameRemark {
        TODO("not implemented")
    }

}


internal class MemberImpl(
    qq: QQImpl,
    group: GroupImpl,
    override val coroutineContext: CoroutineContext
) : ContactImpl(), Member, QQ by qq {
    override val group: GroupImpl by group.unsafeWeakRef()
    val qq: QQImpl by qq.unsafeWeakRef()

    override val permission: MemberPermission
        get() = TODO("not implemented")

    override suspend fun mute(durationSeconds: Int): Boolean {
        TODO("not implemented")
    }

    override suspend fun unmute() {
        TODO("not implemented")
    }

}


@UseExperimental(MiraiInternalAPI::class)
internal class GroupImpl(
    bot: QQAndroidBot, override val coroutineContext: CoroutineContext, override val id: Long,
    override var name: String,
    override var announcement: String,
    override var members: ContactList<Member>
) : ContactImpl(), Group {
    override lateinit var owner: Member
    override val internalId: GroupInternalId = GroupId(id).toInternalId()

    override fun getMember(id: Long): Member =
        members.delegate.filteringGetOrAdd(
            { it.id == id },
            { MemberImpl(bot.getQQ(id) as QQImpl, this, coroutineContext) })

    override suspend fun updateGroupInfo(): net.mamoe.mirai.data.GroupInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun quit(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    operator fun get(key: Long): Member? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        bot.network.run {
            val response = MessageSvc.PbSendMsg.ToGroup(
                bot.client,
                id,
                message
            ).sendAndExpect<MessageSvc.PbSendMsg.Response>()
            check(
                response is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed: $response" }
        }
    }

    override suspend fun uploadImage(image: ExternalImage): ImageId {
        TODO("not implemented")
    }

}