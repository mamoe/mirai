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
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.GroupInfoImpl
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList
import net.mamoe.mirai.utils.*
import kotlin.collections.asSequence
import kotlin.coroutines.CoroutineContext

@UseExperimental(MiraiInternalAPI::class)
internal expect class QQAndroidBot constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase

@UseExperimental(MiraiInternalAPI::class, MiraiExperimentalAPI::class)
internal abstract class QQAndroidBotBase constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : BotImpl<QQAndroidBotNetworkHandler>(context, account, configuration) {
    val client: QQAndroidClient =
        QQAndroidClient(
            context,
            account,
            bot = @Suppress("LeakingThis") this as QQAndroidBot,
            device = configuration.deviceInfo?.invoke(context) ?: SystemDeviceInfo(context)
        )
    internal var firstLoginSucceed: Boolean = false
    override val uin: Long get() = client.uin
    override val qqs: ContactList<QQ> = ContactList(LockFreeLinkedList())

    override val selfQQ: QQ by lazy {
        QQ(object : FriendInfo {
            override val uin: Long get() = this@QQAndroidBotBase.uin
            override val nick: String get() = this@QQAndroidBotBase.nick
        })
    }

    override fun QQ(friendInfo: FriendInfo): QQ {
        return QQImpl(this as QQAndroidBot, coroutineContext, friendInfo.uin, friendInfo)
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(this as QQAndroidBot)
    }

    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    // internally visible only
    fun getGroupByUin(uin: Long): Group {
        return groups.delegate.filteringGetOrNull { (it as GroupImpl).uin == uin } ?: throw NoSuchElementException("Can not found group with ID=${uin}")
    }

    fun getGroupByUinOrNull(uin: Long): Group? {
        return groups.delegate.filteringGetOrNull { (it as GroupImpl).uin == uin }
    }

    override suspend fun queryGroupList(): Sequence<Long> {
        return network.run {
            FriendList.GetTroopListSimplify(bot.client)
                .sendAndExpect<FriendList.GetTroopListSimplify.Response>(retry = 2)
        }.groups.asSequence().map { it.groupUin.shl(32) and it.groupCode }
    }

    override suspend fun queryGroupInfo(groupCode: Long): GroupInfo = network.run {
        TroopManagement.GetGroupInfo(
            client = bot.client,
            groupCode = groupCode
        ).sendAndExpect<GroupInfoImpl>(retry = 2)
    }

    override suspend fun queryGroupMemberList(groupUin: Long, groupCode: Long, ownerId: Long): Sequence<MemberInfo> = network.run {
        var nextUin = 0L
        var sequence = sequenceOf<MemberInfoImpl>()
        while (true) {
            val data = FriendList.GetTroopMemberList(
                client = bot.client,
                targetGroupUin = groupUin,
                targetGroupCode = groupCode,
                nextUin = nextUin
            ).sendAndExpect<FriendList.GetTroopMemberList.Response>(timeoutMillis = 3000)
            sequence += data.members.asSequence().map { troopMemberInfo ->
                MemberInfoImpl(troopMemberInfo, ownerId)
            }
            nextUin = data.nextUin
            if (nextUin == 0L) {
                break
            }
        }
        return sequence
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