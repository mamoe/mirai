package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.coroutines.CoroutineContext

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

    override suspend fun uploadImage(image: ExternalImage): Image {
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
    override val coroutineContext: CoroutineContext,
    override val permission: MemberPermission
) : ContactImpl(), Member, QQ by qq {
    override val group: GroupImpl by group.unsafeWeakRef()
    val qq: QQImpl by qq.unsafeWeakRef()


    override suspend fun mute(durationSeconds: Int): Boolean {
        TODO("not implemented")
    }

    override suspend fun unmute() {
        TODO("not implemented")
    }

}


@UseExperimental(MiraiInternalAPI::class)
internal class GroupImpl(
    bot: QQAndroidBot, override val coroutineContext: CoroutineContext,
    override val id: Long,
    val uin: Long,
    override var name: String,
    override var announcement: String,
    override var members: ContactList<Member>
) : ContactImpl(), Group {
    override lateinit var owner: Member

    override suspend fun quit(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override operator fun get(id: Long): Member {
        return members.delegate.filteringGetOrNull { it.id == id } ?: throw NoSuchElementException("for group id $id")
    }

    override fun contains(id: Long): Boolean {
        return members.delegate.filteringGetOrNull { it.id == id } != null
    }

    override fun getOrNull(id: Long): Member? {
        return members.delegate.filteringGetOrNull { it.id == id }
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

    override suspend fun uploadImage(image: ExternalImage): Image {
        TODO("not implemented")
    }

}