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

internal abstract class ContactImpl : Contact

internal class QQImpl(bot: QQAndroidBot, override val coroutineContext: CoroutineContext, override val id: Long) : ContactImpl(), QQ {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        bot.network.run {
            MessageSvc.PbSendMsg.ToFriend(bot.client, id, message).sendAndExpect<MessageSvc.PbSendMsg.Response>()
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
internal class GroupImpl(bot: QQAndroidBot, override val coroutineContext: CoroutineContext, override val id: Long) : ContactImpl(), Group {
    override val internalId: GroupInternalId = GroupId(id).toInternalId()
    override val owner: Member
        get() = TODO("not implemented")
    override val name: String
        get() = TODO("not implemented")
    override val announcement: String
        get() = TODO("not implemented")
    override val members: ContactList<Member> = ContactList(LockFreeLinkedList())

    override fun getMember(id: Long): Member =
        members.delegate.filteringGetOrAdd({ it.id == id }, { MemberImpl(bot.getQQ(id) as QQImpl, this, coroutineContext) })

    override suspend fun updateGroupInfo(): GroupInfo {
        TODO("not implemented")
    }

    override suspend fun quit(): Boolean {
        TODO("not implemented")
    }

    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        TODO("not implemented")
    }

    override suspend fun uploadImage(image: ExternalImage): ImageId {
        TODO("not implemented")
    }
}