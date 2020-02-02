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
import net.mamoe.mirai.utils.io.getRandomByteArray
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

    override fun getQQ(id: Long): QQ {
        return qqs.delegate.filteringGetOrAdd({ it.id == id }, { QQImpl(this as QQAndroidBot, coroutineContext, id) })
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(this as QQAndroidBot)
    }

    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    override fun getGroupByID(id: Long): Group {
        return groups.delegate.getOrNull(id) ?: throw NoSuchElementException("Can not found group with ID=${id}")
    }

    override fun getGroupByGroupCode(groupCode: Long): Group {
        return groups.delegate.filterGetOrNull { it.groupCode == groupCode }
            ?: throw NoSuchElementException("Can not found group with GroupCode=${groupCode}")
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