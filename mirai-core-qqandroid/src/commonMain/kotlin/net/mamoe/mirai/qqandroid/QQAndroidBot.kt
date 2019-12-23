package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

internal expect class QQAndroidBot(
    account: BotAccount,
    logger: MiraiLogger?,
    context: CoroutineContext
) : QQAndroidBotBase

@UseExperimental(MiraiInternalAPI::class)
internal abstract class QQAndroidBotBase constructor(
    account: BotAccount,
    logger: MiraiLogger?,
    context: CoroutineContext
) : BotImpl<QQAndroidBotNetworkHandler>(account, logger, context) {
    override val qqs: ContactList<QQ>
        get() = TODO("not implemented")

    override fun getQQ(id: Long): QQ {
        TODO("not implemented")
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(this as QQAndroidBot)
    }

    override val groups: ContactList<Group>
        get() = TODO("not implemented")

    override suspend fun getGroup(id: GroupId): Group {
        TODO("not implemented")
    }

    override suspend fun getGroup(internalId: GroupInternalId): Group {
        TODO("not implemented")
    }

    override suspend fun getGroup(id: Long): Group {
        TODO("not implemented")
    }

    override suspend fun login(configuration: BotConfiguration) {
        TODO("not implemented")
    }

    override suspend fun Image.getLink(): ImageLink {
        TODO("not implemented")
    }

    override suspend fun addFriend(id: Long, message: String?, remark: String?): AddFriendResult {
        TODO("not implemented")
    }

    override suspend fun approveFriendAddRequest(id: Long, remark: String?) {
        TODO("not implemented")
    }
}