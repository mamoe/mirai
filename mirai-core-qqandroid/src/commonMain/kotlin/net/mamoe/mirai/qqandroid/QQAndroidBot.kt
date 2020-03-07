/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.io.ByteReadChannel
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.message.OnlineFriendImageImpl
import net.mamoe.mirai.qqandroid.message.OnlineGroupImageImpl
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.GroupInfoImpl
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.PbMessageSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList
import net.mamoe.mirai.utils.*
import kotlin.collections.asSequence
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalAPI::class)
internal expect class QQAndroidBot constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase

@OptIn(MiraiInternalAPI::class, MiraiExperimentalAPI::class)
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

    @Deprecated(
        "use friends instead",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("this.friends")
    )
    override val qqs: ContactList<QQ>
        get() = friends
    override val friends: ContactList<QQ> = ContactList(LockFreeLinkedList())

    override val selfQQ: QQ by lazy {
        @OptIn(LowLevelAPI::class)
        _lowLevelNewQQ(object : FriendInfo {
            override val uin: Long get() = this@QQAndroidBotBase.uin
            override val nick: String get() = this@QQAndroidBotBase.nick
        })
    }

    @LowLevelAPI
    override fun _lowLevelNewQQ(friendInfo: FriendInfo): QQ {
        return QQImpl(
            this as QQAndroidBot,
            coroutineContext + CoroutineName("QQ(${friendInfo.uin}"),
            friendInfo.uin,
            friendInfo
        )
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(this as QQAndroidBot)
    }

    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    // internally visible only
    fun getGroupByUin(uin: Long): Group {
        return groups.delegate.getOrNull(uin) ?: throw NoSuchElementException("Can not found group with ID=${uin}")
    }

    fun getGroupByUinOrNull(uin: Long): Group? {
        return groups.delegate.getOrNull(uin)
    }

    @OptIn(LowLevelAPI::class)
    override suspend fun _lowLevelQueryGroupList(): Sequence<Long> {
        return network.run {
            FriendList.GetTroopListSimplify(bot.client)
                .sendAndExpect<FriendList.GetTroopListSimplify.Response>(retry = 2)
        }.groups.asSequence().map { it.groupUin.shl(32) and it.groupCode }
    }

    @OptIn(LowLevelAPI::class)
    override suspend fun _lowLevelQueryGroupInfo(groupCode: Long): GroupInfo = network.run {
        TroopManagement.GetGroupInfo(
            client = bot.client,
            groupCode = groupCode
        ).sendAndExpect<GroupInfoImpl>(retry = 2)
    }

    @OptIn(LowLevelAPI::class)
    override suspend fun _lowLevelQueryGroupMemberList(
        groupUin: Long,
        groupCode: Long,
        ownerId: Long
    ): Sequence<MemberInfo> =
        network.run {
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

    override suspend fun recall(source: MessageSource) {
        if (source.senderId != uin && source.groupId != 0L) {
            getGroup(source.groupId).checkBotPermissionOperator()
        }

        // println(source._miraiContentToString())
        source.ensureSequenceIdAvailable()

        network.run {
            val response: PbMessageSvc.PbMsgWithDraw.Response =
                if (source.groupId == 0L) {
                    PbMessageSvc.PbMsgWithDraw.Friend(
                        bot.client,
                        source.senderId,
                        source.sequenceId,
                        source.messageRandom,
                        source.time
                    ).sendAndExpect()
                } else {
                    MessageRecallEvent.GroupRecall(
                        bot,
                        source.senderId,
                        source.id,
                        source.time.toInt(),
                        null,
                        getGroup(source.groupId)
                    ).broadcast()
                    PbMessageSvc.PbMsgWithDraw.Group(
                        bot.client,
                        source.groupId,
                        source.sequenceId,
                        source.messageRandom
                    ).sendAndExpect()
                }

            check(response is PbMessageSvc.PbMsgWithDraw.Response.Success) { "Failed to recall message #${source.id}: $response" }
        }
    }

    @OptIn(LowLevelAPI::class)
    override suspend fun _lowLevelRecallFriendMessage(friendId: Long, messageId: Long, time: Long) {
        network.run {
            val response: PbMessageSvc.PbMsgWithDraw.Response =
                PbMessageSvc.PbMsgWithDraw.Friend(client, friendId, (messageId shr 32).toInt(), messageId.toInt(), time)
                    .sendAndExpect()

            check(response is PbMessageSvc.PbMsgWithDraw.Response.Success) { "Failed to recall message #${messageId}: $response" }
        }
    }

    @OptIn(LowLevelAPI::class)
    override suspend fun _lowLevelRecallGroupMessage(groupId: Long, messageId: Long) {
        network.run {
            val response: PbMessageSvc.PbMsgWithDraw.Response =
                PbMessageSvc.PbMsgWithDraw.Group(client, groupId, (messageId shr 32).toInt(), messageId.toInt())
                    .sendAndExpect()

            check(response is PbMessageSvc.PbMsgWithDraw.Response.Success) { "Failed to recall message #${messageId}: $response" }
        }
    }

    override suspend fun queryImageUrl(image: Image): String = when (image) {
        is OnlineFriendImageImpl -> image.originUrl
        is OnlineGroupImageImpl -> image.originUrl
        is OfflineGroupImage -> {
            TODO("暂不支持获取离线图片链接")
        }
        is OfflineFriendImage -> {
            TODO("暂不支持获取离线图片链接")
        }
        else -> error("unsupported image class: ${image::class.simpleName}")
    }

    override suspend fun openChannel(image: Image): ByteReadChannel {
        return Http.get<HttpResponse>(queryImageUrl(image)).content.toKotlinByteReadChannel()
    }
}

@Suppress("DEPRECATION")
@UseExperimental(MiraiInternalAPI::class)
internal expect fun io.ktor.utils.io.ByteReadChannel.toKotlinByteReadChannel(): ByteReadChannel