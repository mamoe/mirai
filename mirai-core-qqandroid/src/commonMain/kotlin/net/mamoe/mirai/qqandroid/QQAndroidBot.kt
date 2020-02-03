package net.mamoe.mirai.qqandroid

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.coroutines.CoroutineContext

@UseExperimental(MiraiInternalAPI::class)
internal expect class QQAndroidBot constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase

@UseExperimental(MiraiInternalAPI::class)
internal abstract class QQAndroidBotBase constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : BotImpl<QQAndroidBotNetworkHandler>(account, configuration) {
    val client: QQAndroidClient = QQAndroidClient(context, account, bot = @Suppress("LeakingThis") this as QQAndroidBot)
    override val uin: Long get() = client.uin
    override val qqs: ContactList<QQ> = ContactList(LockFreeLinkedList())

    val selfQQ: QQ by lazy { QQ(uin) }

    override fun getFriend(id: Long): QQ {
        return qqs.delegate.filteringGetOrAdd({ it.id == id }, { QQImpl(this as QQAndroidBot, coroutineContext, id) })
    }

    fun getQQOrAdd(id: Long): QQ {
        return qqs.delegate.filteringGetOrAdd({ it.id == id }, { QQImpl(this as QQAndroidBot, coroutineContext, id) })
    }

    override fun QQ(id: Long): QQ {
        return QQImpl(this as QQAndroidBot, coroutineContext, id)
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(this as QQAndroidBot)
    }

    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    fun getGroupByUin(uin: Long): Group {
        return groups.delegate.filteringGetOrNull { (it as GroupImpl).uin == uin } ?: throw NoSuchElementException("Can not found group with ID=${uin}")
    }

    override fun getGroup(id: Long): Group {
        return groups.delegate.getOrNull(id)
            ?: throw NoSuchElementException("Can not found group with GroupCode=${id}")
    }

    override suspend fun addFriend(id: Long, message: String?, remark: String?): AddFriendResult {
        TODO("not implemented")
    }

    override suspend fun Image.download(): ByteReadPacket {
        TODO("not implemented")
    }

    override suspend fun Image.downloadAsByteArray(): ByteArray {
        TODO("not implemented")
    }

    override suspend fun approveFriendAddRequest(id: Long, remark: String?) {
        TODO("not implemented")
    }
}