/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.filteringGetOrNull
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.utils.*
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
    val client: QQAndroidClient =
        QQAndroidClient(context, account, bot = @Suppress("LeakingThis") this as QQAndroidBot, device = configuration.deviceInfo?.invoke(context) ?: SystemDeviceInfo(context))
    internal var firstLoginSucceed: Boolean = false
    override val uin: Long get() = client.uin
    override val qqs: ContactList<QQ> = ContactList(LockFreeLinkedList())

    override val selfQQ: QQ by lazy { QQ(uin) }

    override fun QQ(id: Long): QQ {
        return QQImpl(this as QQAndroidBot, coroutineContext, id)
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(this as QQAndroidBot)
    }

    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    // internally visible only
    fun getGroupByUin(uin: Long): Group {
        return groups.delegate.filteringGetOrNull { (it as GroupImpl).uin == uin } ?: throw NoSuchElementException("Can not found group with ID=${uin}")
    }

    override fun onEvent(event: BotEvent): Boolean {
        return firstLoginSucceed
    }

    override suspend fun addFriend(id: Long, message: String?, remark: String?): AddFriendResult {
        TODO("not implemented")
    }

    override suspend fun Image.download(): ByteReadPacket {
        TODO("not implemented")
    }

    @Suppress("OverridingDeprecatedMember")
    override suspend fun Image.downloadAsByteArray(): ByteArray {
        TODO("not implemented")
    }

    override suspend fun approveFriendAddRequest(id: Long, remark: String?) {
        TODO("not implemented")
    }
}