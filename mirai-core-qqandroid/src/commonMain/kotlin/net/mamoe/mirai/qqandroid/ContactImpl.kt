package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.message.data.ImageId
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext

internal abstract class ContactImpl : Contact

internal class QQImpl(bot: Bot, override val coroutineContext: CoroutineContext, override val id: Long) : ContactImpl(), QQ {
    override val bot: Bot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        TODO("not implemented")
    }

    override suspend fun uploadImage(image: ExternalImage): ImageId {
        TODO("not implemented")
    }

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

internal class MemberImpl(bot: Bot, group: Group, override val coroutineContext: CoroutineContext, override val id: Long) : ContactImpl(), Member {
    override val group: Group by group.unsafeWeakRef()
    override val permission: MemberPermission
        get() = TODO("not implemented")
    override val bot: Bot by bot.unsafeWeakRef()

    override suspend fun mute(durationSeconds: Int): Boolean {
        TODO("not implemented")
    }

    override suspend fun unmute() {
        TODO("not implemented")
    }

    override suspend fun queryProfile(): Profile {
        TODO("not implemented")
    }

    override suspend fun queryPreviousNameList(): PreviousNameList {
        TODO("not implemented")
    }

    override suspend fun queryRemark(): FriendNameRemark {
        TODO("not implemented")
    }

    override suspend fun sendMessage(message: MessageChain) {
        TODO("not implemented")
    }

    override suspend fun uploadImage(image: ExternalImage): ImageId {
        TODO("not implemented")
    }

}


@UseExperimental(MiraiInternalAPI::class)
internal class GroupImpl(bot: Bot, override val coroutineContext: CoroutineContext, override val id: Long) : ContactImpl(), Group {
    override val internalId: GroupInternalId = GroupId(id).toInternalId()
    override val owner: Member
        get() = TODO("not implemented")
    override val name: String
        get() = TODO("not implemented")
    override val announcement: String
        get() = TODO("not implemented")
    override val members: ContactList<Member> = ContactList(LockFreeLinkedList())

    override fun getMember(id: Long): Member = members.delegate.filteringGetOrAdd({ it.id == id }, { MemberImpl(bot, this, coroutineContext, id) })

    override suspend fun updateGroupInfo(): GroupInfo {
        TODO("not implemented")
    }

    override suspend fun quit(): Boolean {
        TODO("not implemented")
    }

    override val bot: Bot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        TODO("not implemented")
    }

    override suspend fun uploadImage(image: ExternalImage): ImageId {
        TODO("not implemented")
    }
}