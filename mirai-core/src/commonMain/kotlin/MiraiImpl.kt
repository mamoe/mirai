/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.serialization.json.*
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.message.*
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.EventDispatcherScopeFlag
import net.mamoe.mirai.internal.network.highway.*
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcDevLoginInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.LongMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.internal.network.protocol.packet.chat.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.internal.network.protocol.packet.summarycard.SummaryCard
import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.IMAGE_ID_REGEX
import net.mamoe.mirai.message.data.Image.Key.IMAGE_RESOURCE_ID_REGEX_1
import net.mamoe.mirai.message.data.Image.Key.IMAGE_RESOURCE_ID_REGEX_2
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(LowLevelApi::class)
// not object for ServiceLoader.
internal open class MiraiImpl : IMirai, LowLevelApiAccessor {
    companion object INSTANCE : MiraiImpl() {
        @Suppress("ObjectPropertyName", "unused", "DEPRECATION_ERROR")
        private val _init = Mirai.let {
            MessageSerializers.registerSerializer(OfflineGroupImage::class, OfflineGroupImage.serializer())
            MessageSerializers.registerSerializer(OfflineFriendImage::class, OfflineFriendImage.serializer())
            MessageSerializers.registerSerializer(OnlineFriendImageImpl::class, OnlineFriendImageImpl.serializer())
            MessageSerializers.registerSerializer(OnlineGroupImageImpl::class, OnlineGroupImageImpl.serializer())

            MessageSerializers.registerSerializer(MarketFaceImpl::class, MarketFaceImpl.serializer())
            MessageSerializers.registerSerializer(FileMessageImpl::class, FileMessageImpl.serializer())

            // MessageSource

            MessageSerializers.registerSerializer(
                OnlineMessageSourceFromGroupImpl::class,
                OnlineMessageSourceFromGroupImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OnlineMessageSourceFromFriendImpl::class,
                OnlineMessageSourceFromFriendImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OnlineMessageSourceFromTempImpl::class,
                OnlineMessageSourceFromTempImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OnlineMessageSourceFromStrangerImpl::class,
                OnlineMessageSourceFromStrangerImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OnlineMessageSourceToGroupImpl::class,
                OnlineMessageSourceToGroupImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OnlineMessageSourceToFriendImpl::class,
                OnlineMessageSourceToFriendImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OnlineMessageSourceToTempImpl::class,
                OnlineMessageSourceToTempImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OnlineMessageSourceToStrangerImpl::class,
                OnlineMessageSourceToStrangerImpl.serializer()
            )
            MessageSerializers.registerSerializer(
                OfflineMessageSourceImplData::class,
                OfflineMessageSourceImplData.serializer()
            )
            MessageSerializers.registerSerializer(
                OfflineMessageSourceImplData::class,
                OfflineMessageSourceImplData.serializer()
            )
            MessageSerializers.registerSerializer(
                UnsupportedMessageImpl::class,
                UnsupportedMessageImpl.serializer()
            )
        }
    }

    @Suppress("DEPRECATION")
    override val BotFactory: BotFactory
        get() = BotFactoryImpl

    override var FileCacheStrategy: FileCacheStrategy = net.mamoe.mirai.utils.FileCacheStrategy.PlatformDefault

    override var Http: HttpClient = HttpClient(OkHttp) {
        install(HttpTimeout) {
            this.requestTimeoutMillis = 30_0000
            this.connectTimeoutMillis = 30_0000
            this.socketTimeoutMillis = 30_0000
        }
    }

    @OptIn(LowLevelApi::class)
    override suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent) {
        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        check(!event.bot.friends.contains(event.fromId)) {
            "the request $event is outdated: You had already responded it on another device."
        }

        solveNewFriendRequestEvent(
            event.bot,
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            accept = true,
            blackList = false
        )

        event.bot.getFriend(event.fromId)?.let { friend ->
            FriendAddEvent(friend).broadcast()
        }
    }

    override suspend fun refreshKeys(bot: Bot) {
        // TODO: 2021/4/14 MiraiImpl.refreshKeysNow
    }

    override suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean) {
        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        check(event.responded.compareAndSet(false, true)) {
            "the request $event has already been responded"
        }

        check(!event.bot.friends.contains(event.fromId)) {
            "the request $event is outdated: You had already responded it on another device."
        }

        solveNewFriendRequestEvent(
            event.bot,
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            accept = false,
            blackList = blackList
        )
    }

    override suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent) {
        @Suppress("DuplicatedCode")
        checkGroupPermission(event.bot, event.groupId) { event::class.simpleName ?: "<anonymous class>" }
        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        if (event.group?.contains(event.fromId) == true) return

        solveMemberJoinRequestEvent(
            bot = event.bot,
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            groupId = event.groupId,
            accept = true,
            blackList = false
        )
    }

    @Suppress("DuplicatedCode")
    override suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean, message: String) {
        checkGroupPermission(event.bot, event.groupId) { event::class.simpleName ?: "<anonymous class>" }
        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        if (event.group?.contains(event.fromId) == true) return

        solveMemberJoinRequestEvent(
            bot = event.bot,
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            groupId = event.groupId,
            accept = false,
            blackList = blackList,
            message = message
        )
    }

    private inline fun checkGroupPermission(eventBot: Bot, groupId: Long, eventName: () -> String) {
        val group = eventBot.getGroup(groupId)
            ?: kotlin.run {
                error(
                    "A ${eventName()} is outdated. Group $groupId not found for bot ${eventBot.id}. " +
                            "This is because bot isn't in the group anymore"
                )

            }

        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
    }

    override suspend fun getOnlineOtherClientsList(bot: Bot, mayIncludeSelf: Boolean): List<OtherClientInfo> {
        bot.asQQAndroidBot()
        val response = bot.network.run {
            StatSvc.GetDevLoginInfo(bot.client).sendAndExpect()
        }

        fun SvcDevLoginInfo.toOtherClientInfo() = OtherClientInfo(
            iAppId.toInt(),
            Platform.getByTerminalId(iTerType?.toInt() ?: 0),
            deviceName.orEmpty(),
            deviceTypeInfo.orEmpty()
        )

        return response.deviceList.map { it.toOtherClientInfo() }.let { result ->
            if (mayIncludeSelf) result else result.filterNot {
                it.appId == MiraiProtocolInternal[bot.configuration.protocol].id.toInt()
            }
        }
    }

    override suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean) {
        checkGroupPermission(event.bot, event.groupId) { event::class.simpleName ?: "<anonymous class>" }
        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        solveMemberJoinRequestEvent(
            bot = event.bot,
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            groupId = event.groupId,
            accept = null,
            blackList = blackList
        )
    }

    override suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) =
        solveInvitedJoinGroupRequest(event, accept = true)

    override suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) =
        solveInvitedJoinGroupRequest(event, accept = false)

    override suspend fun broadcastEvent(event: Event) {
        if (currentCoroutineContext()[EventDispatcherScopeFlag] != null) {
            // called by [EventDispatcher]
            return super.broadcastEvent(event)
        }
        if (event is BotEvent) {
            val bot = event.bot
            if (bot is QQAndroidBot) {
                bot.components[EventDispatcher].broadcast(event)
            }
        } else {
            super.broadcastEvent(event)
        }
    }

    private suspend fun solveInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent, accept: Boolean) {
        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        check(!event.bot.groups.contains(event.groupId)) {
            "the request $this is outdated: Bot has been already in the group."
        }

        solveBotInvitedJoinGroupRequestEvent(
            bot = event.bot,
            eventId = event.eventId,
            invitorId = event.invitorId,
            groupId = event.groupId,
            accept = accept
        )
    }

    @LowLevelApi
    override fun newFriend(bot: Bot, friendInfo: FriendInfo): Friend {
        return FriendImpl(
            bot.asQQAndroidBot(),
            bot.coroutineContext + SupervisorJob(bot.supervisorJob),
            friendInfo
        )
    }

    @LowLevelApi
    override fun newStranger(bot: Bot, strangerInfo: StrangerInfo): Stranger {
        return StrangerImpl(
            bot.asQQAndroidBot(),
            bot.coroutineContext + SupervisorJob(bot.supervisorJob),
            strangerInfo
        )
    }


    @OptIn(LowLevelApi::class)
    override suspend fun getRawGroupList(bot: Bot): Sequence<Long> {
        bot.asQQAndroidBot()
        return bot.network.run {
            FriendList.GetTroopListSimplify(bot.client).sendAndExpect(retry = 2)
        }.groups.asSequence().map { it.groupUin.shl(32) and it.groupCode }
    }

    @OptIn(LowLevelApi::class)
    override suspend fun getRawGroupMemberList(
        bot: Bot,
        groupUin: Long,
        groupCode: Long,
        ownerId: Long
    ): Sequence<MemberInfo> =
        bot.asQQAndroidBot().network.run {
            var nextUin = 0L
            var sequence = sequenceOf<MemberInfoImpl>()
            while (true) {
                val data = FriendList.GetTroopMemberList(
                    client = bot.client,
                    targetGroupUin = groupUin,
                    targetGroupCode = groupCode,
                    nextUin = nextUin
                ).sendAndExpect(retry = 3)
                sequence += data.members.asSequence().map { troopMemberInfo ->
                    MemberInfoImpl(bot.client, troopMemberInfo, ownerId)
                }
                nextUin = data.nextUin
                if (nextUin == 0L) {
                    break
                }
            }
            return sequence
        }

    override suspend fun recallGroupMessageRaw(
        bot: Bot,
        groupCode: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
    ): Boolean = bot.asQQAndroidBot().run {
        val response = network.run {
            PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                client,
                groupCode,
                messageIds,
                messageInternalIds
            ).sendAndExpect()
        }

        response is PbMessageSvc.PbMsgWithDraw.Response.Success
    }

    override suspend fun recallFriendMessageRaw(
        bot: Bot,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int,
    ): Boolean = bot.asQQAndroidBot().run {
        val response = network.run {
            PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                client,
                targetId,
                messageIds,
                messageInternalIds,
                time,
            ).sendAndExpect()
        }

        response is PbMessageSvc.PbMsgWithDraw.Response.Success
    }

    override suspend fun recallGroupTempMessageRaw(
        bot: Bot,
        groupUin: Long,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int
    ): Boolean = bot.asQQAndroidBot().run {
        val response = network.run {
            PbMessageSvc.PbMsgWithDraw.createForGroupTempMessage(
                client,
                groupUin,
                targetId,
                messageIds,
                messageInternalIds,
                time,
            ).sendAndExpect()
        }

        response is PbMessageSvc.PbMsgWithDraw.Response.Success
    }

    @Suppress("RemoveExplicitTypeArguments") // false positive
    override suspend fun recallMessage(bot: Bot, source: MessageSource) = bot.asQQAndroidBot().run {
        check(source is MessageSourceInternal)

        source.ensureSequenceIdAvailable()

        @Suppress("BooleanLiteralArgument", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER") // false positive
        check(!source.isRecalledOrPlanned.get() && source.isRecalledOrPlanned.compareAndSet(false, true)) {
            "$source had already been recalled."
        }

        val response: PbMessageSvc.PbMsgWithDraw.Response = when (source) {
            is OnlineMessageSourceToGroupImpl,
            is OnlineMessageSourceFromGroupImpl
            -> {
                val group = when (source) {
                    is OnlineMessageSourceToGroupImpl -> source.target
                    is OnlineMessageSourceFromGroupImpl -> source.group
                    else -> error("stub")
                }
                if (bot.id != source.fromId) {
                    group.checkBotPermission(MemberPermission.ADMINISTRATOR)
                }

                network.run {
                    PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                        bot.asQQAndroidBot().client,
                        group.id,
                        source.sequenceIds,
                        source.internalIds
                    ).sendAndExpect()
                }
            }
            is OnlineMessageSourceFromFriendImpl,
            is OnlineMessageSourceToFriendImpl,
            is OnlineMessageSourceFromStrangerImpl,
            is OnlineMessageSourceToStrangerImpl,
            -> network.run {
                check(source.fromId == bot.id) {
                    "can only recall a message sent by bot"
                }
                PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                    bot.client,
                    source.targetId,
                    source.sequenceIds,
                    source.internalIds,
                    source.time
                ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
            }
            is OnlineMessageSourceFromTempImpl,
            is OnlineMessageSourceToTempImpl
            -> network.run {
                check(source.fromId == bot.id) {
                    "can only recall a message sent by bot"
                }
                source as OnlineMessageSourceToTempImpl
                PbMessageSvc.PbMsgWithDraw.createForGroupTempMessage(
                    bot.client,
                    (source.target.group as GroupImpl).uin,
                    source.targetId,
                    source.sequenceIds,
                    source.internalIds,
                    source.time
                ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
            }
            is OfflineMessageSource -> network.run {
                when (source.kind) {
                    MessageSourceKind.FRIEND, MessageSourceKind.STRANGER -> {
                        check(source.fromId == bot.id) {
                            "can only recall a message sent by bot"
                        }
                        PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                            bot.client,
                            source.targetId,
                            source.sequenceIds,
                            source.internalIds,
                            source.time
                        ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
                    }
                    MessageSourceKind.TEMP -> {
                        check(source.fromId == bot.id) {
                            "can only recall a message sent by bot"
                        }
                        PbMessageSvc.PbMsgWithDraw.createForGroupTempMessage(
                            bot.client,
                            source.targetId, // groupUin
                            source.targetId, // memberUin
                            source.sequenceIds,
                            source.internalIds,
                            source.time
                        ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
                    }
                    MessageSourceKind.GROUP -> {
                        PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                            bot.client,
                            source.targetId,
                            source.sequenceIds,
                            source.internalIds
                        ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
                    }
                }
            }
            else -> error("stub!")
        }


        // 1001: No message meets the requirements (实际上是没权限, 管理员在尝试撤回群主的消息)
        // 154: timeout
        // 3: <no message>
        check(response is PbMessageSvc.PbMsgWithDraw.Response.Success) { "Failed to recall message #${source.ids.contentToString()}: $response" }
    }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun getRawGroupAnnouncements(
        bot: Bot,
        groupId: Long,
        page: Int,
        amount: Int
    ): GroupAnnouncementList = bot.asQQAndroidBot().run {
        val rep = bot.asQQAndroidBot().network.run {
            Mirai.Http.post<String> {
                url("https://web.qun.qq.com/cgi-bin/announce/list_announce")
                body = MultiPartFormDataContent(formData {
                    append("qid", groupId)
                    append("bkn", bot.bkn)
                    append("ft", 23)  //好像是一个用来识别应用的参数
                    append("s", if (page == 1) 0 else -(page * amount + 1))  // 第一页这里的参数应该是-1
                    append("n", amount)
                    append("ni", if (page == 1) 1 else 0)
                    append("format", "json")
                })
                headers {
                    append(
                        "cookie",
                        "uin=o${id}; skey=${client.wLoginSigInfo.sKey.data.encodeToString()}; p_uin=o${id};"
                    )
                }
            }
        }
//        bot.network.logger.error(rep)
        return json.decodeFromString(GroupAnnouncementList.serializer(), rep)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun sendGroupAnnouncement(bot: Bot, groupId: Long, announcement: GroupAnnouncement): String =
        bot.asQQAndroidBot().run {
            val rep = Mirai.Http.post<String> {
                url("https://web.qun.qq.com/cgi-bin/announce/add_qun_notice")
                body = MultiPartFormDataContent(formData {
                    append("qid", groupId)
                    append("bkn", bkn)
                    append("text", announcement.msg.text)
                    append("pinned", announcement.pinned)
                    append(
                        "settings",
                        json.encodeToString(
                            GroupAnnouncementSettings.serializer(),
                            announcement.settings ?: GroupAnnouncementSettings()
                        )
                    )
                    append("format", "json")
                })
                headers {
                    append(
                        "cookie",
                        "uin=o${id};" +
                                " skey=${client.wLoginSigInfo.sKey.data.encodeToString()};" +
                                " p_uin=o${id};" +
                                " p_skey=${client.wLoginSigInfo.psKeyMap["qun.qq.com"]?.data?.encodeToString()}; "
                    )
                }
            }
            val jsonObj = json.parseToJsonElement(rep)
            return jsonObj.jsonObject["new_fid"]?.jsonPrimitive?.content
                ?: throw throw IllegalStateException("Send Announcement fail group:$groupId msg:${jsonObj.jsonObject["em"]} content:${announcement.msg.text}")
        }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun deleteGroupAnnouncement(bot: Bot, groupId: Long, fid: String) = bot.asQQAndroidBot().run {
        val data = Mirai.Http.post<String> {
            url("https://web.qun.qq.com/cgi-bin/announce/del_feed")
            body = MultiPartFormDataContent(formData {
                append("qid", groupId)
                append("bkn", bkn)
                append("fid", fid)
                append("format", "json")
            })
            headers {
                append(
                    "cookie",
                    "uin=o${id};" +
                            " skey=${client.wLoginSigInfo.sKey.data.encodeToString()};" +
                            " p_uin=o${id};" +
                            " p_skey=${client.wLoginSigInfo.psKeyMap["qun.qq.com"]?.data?.encodeToString()}; "
                )
            }
        }
        val jsonObj = json.parseToJsonElement(data)
        if (jsonObj.jsonObject["ec"]?.jsonPrimitive?.int ?: 1 != 0) {
            throw throw IllegalStateException("delete Announcement fail group:$groupId msg:${jsonObj.jsonObject["em"]} fid:$fid")
        }
    }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun getGroupAnnouncement(bot: Bot, groupId: Long, fid: String): GroupAnnouncement =
        bot.asQQAndroidBot().run {
            val rep = network.run {
                Mirai.Http.post<String> {
                    url("https://web.qun.qq.com/cgi-bin/announce/get_feed")
                    body = MultiPartFormDataContent(formData {
                        append("qid", groupId)
                        append("bkn", bkn)
                        append("fid", fid)
                        append("format", "json")
                    })
                    headers {
                        append(
                            "cookie",
                            "uin=o${id}; skey=${client.wLoginSigInfo.sKey.data.encodeToString()}; p_uin=o${id};"
                        )
                    }
                }
            }

//        bot.network.logger.error(rep)
            return json.decodeFromString(GroupAnnouncement.serializer(), rep)

        }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun getRawGroupActiveData(bot: Bot, groupId: Long, page: Int): GroupActiveData =
        bot.asQQAndroidBot().run {
            val rep = network.run {
                Mirai.Http.get<String> {
                    url("https://qqweb.qq.com/c/activedata/get_mygroup_data")
                    parameter("bkn", bkn)
                    parameter("gc", groupId)
                    if (page != -1) {
                        parameter("page", page)
                    }
                    headers {
                        append(
                            "cookie",
                            "uin=o${id}; skey=${client.wLoginSigInfo.sKey.data.encodeToString()}; p_uin=o${id};"
                        )
                    }
                }
            }
            return json.decodeFromString(GroupActiveData.serializer(), rep)
        }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun getRawGroupHonorListData(
        bot: Bot,
        groupId: Long,
        type: GroupHonorType
    ): GroupHonorListData? = bot.asQQAndroidBot().run {
        val rep = network.run {
            Mirai.Http.get<String> {
                url("https://qun.qq.com/interactive/honorlist")
                parameter("gc", groupId)
                parameter("type", type.value)
                headers {
                    append(
                        "cookie",
                        "uin=o${id};" +
                                " skey=${client.wLoginSigInfo.sKey.data.encodeToString()};" +
                                " p_uin=o${id};" +
                                " p_skey=${client.wLoginSigInfo.psKeyMap["qun.qq.com"]?.data?.encodeToString()}; "
                    )
                }
            }
        }
        val jsonText = Regex("""window.__INITIAL_STATE__=(.+?)</script>""").find(rep)?.groupValues?.get(1)
        return jsonText?.let { json.decodeFromString(GroupHonorListData.serializer(), it) }
    }

    internal suspend fun uploadMessageHighway(
        bot: Bot,
        sendMessageHandler: SendMessageHandler<*>,
        message: Collection<ForwardMessage.INode>,
        isLong: Boolean,
    ): String = with(bot.asQQAndroidBot()) {
        message.forEach {
            it.messageChain.ensureSequenceIdAvailable()
        }


        val data = message.calculateValidationData(
            client = client,
            random = Random.nextInt().absoluteValue,
            sendMessageHandler,
            isLong,
        )

        val response = network.run {
            MultiMsg.ApplyUp.createForGroup(
                buType = if (isLong) 1 else 2,
                client = bot.client,
                messageData = data,
                dstUin = sendMessageHandler.targetUin
            ).sendAndExpect()
        }

        val resId: String
        when (response) {
            is MultiMsg.ApplyUp.Response.MessageTooLarge ->
                error(
                    "Internal error: message is too large, but this should be handled before sending. "
                )
            is MultiMsg.ApplyUp.Response.RequireUpload -> {
                resId = response.proto.msgResid

                val body = LongMsg.ReqBody(
                    subcmd = 1,
                    platformType = 9,
                    termType = 5,
                    msgUpReq = listOf(
                        LongMsg.MsgUpReq(
                            msgType = 3, // group
                            dstUin = sendMessageHandler.targetUin,
                            msgId = 0,
                            msgUkey = response.proto.msgUkey,
                            needCache = 0,
                            storeType = 2,
                            msgContent = data.data
                        )
                    )
                ).toByteArray(LongMsg.ReqBody.serializer())

                body.toExternalResource().use { resource ->
                    Highway.uploadResourceBdh(
                        bot = bot,
                        resource = resource,
                        kind = when (isLong) {
                            true -> ResourceKind.LONG_MESSAGE
                            false -> ResourceKind.FORWARD_MESSAGE
                        },
                        commandId = 27,
                        initialTicket = response.proto.msgSig
                    )
                }
            }
        }

        return resId
    }


    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun solveNewFriendRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        accept: Boolean,
        blackList: Boolean
    ): Unit = bot.asQQAndroidBot().run {
        network.apply {
            NewContact.SystemMsgNewFriend.Action(
                bot.client,
                eventId = eventId,
                fromId = fromId,
                accept = accept,
                blackList = blackList
            ).sendWithoutExpect()

            if (!accept) return@apply

            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
            bot.friends.delegate.add(newFriend(bot, FriendInfoImpl(fromId, fromNick, "")))
        }
    }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun solveBotInvitedJoinGroupRequestEvent(
        bot: Bot,
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    ) = bot.asQQAndroidBot().run {
        network.run {
            NewContact.SystemMsgNewGroup.Action(
                bot.client,
                eventId = eventId,
                fromId = invitorId,
                groupId = groupId,
                isInvited = true,
                accept = accept
            ).sendWithoutExpect()
        }
    }

    @LowLevelApi
    @MiraiExperimentalApi
    override suspend fun solveMemberJoinRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        groupId: Long,
        accept: Boolean?,
        blackList: Boolean,
        message: String
    ) = bot.asQQAndroidBot().run {
        network.run {
            NewContact.SystemMsgNewGroup.Action(
                bot.client,
                eventId = eventId,
                fromId = fromId,
                groupId = groupId,
                isInvited = false,
                accept = accept,
                blackList = blackList,
                message = message
            ).sendWithoutExpect()
        }
        // Add member in MsgOnlinePush.PbPushMsg
    }

    @OptIn(ExperimentalStdlibApi::class)
    @LowLevelApi
    override suspend fun getGroupVoiceDownloadUrl(
        bot: Bot,
        md5: ByteArray,
        groupId: Long,
        dstUin: Long
    ): String {
        bot.asQQAndroidBot().network.run {
            val response = PttStore.GroupPttDown(bot.client, groupId, dstUin, md5).sendAndExpect()
            return "http://${response.strDomain}${response.downPara.encodeToString()}"
        }
    }

    override suspend fun muteAnonymousMember(
        bot: Bot,
        anonymousId: String,
        anonymousNick: String,
        groupId: Long,
        seconds: Int
    ) {
        bot as QQAndroidBot
        val response = Mirai.Http.post<String> {
            url("https://qqweb.qq.com/c/anonymoustalk/blacklist")
            body = MultiPartFormDataContent(formData {
                append("anony_id", anonymousId)
                append("group_code", groupId)
                append("seconds", seconds)
                append("anony_nick", anonymousNick)
                append("bkn", bot.bkn)
            })
            headers {
                append(
                    "cookie",
                    "uin=o${bot.id}; skey=${bot.client.wLoginSigInfo.sKey.data.encodeToString()};"
                )
            }
        }
        val jsonObj = Json.decodeFromString(JsonObject.serializer(), response)
        if ((jsonObj["retcode"] ?: jsonObj["cgicode"] ?: error("missing response code")).jsonPrimitive.long != 0L) {
            throw IllegalStateException(response)
        }
    }

    override fun createImage(imageId: String): Image {
        return when {
            imageId matches IMAGE_ID_REGEX -> OfflineGroupImage(imageId)
            imageId matches IMAGE_RESOURCE_ID_REGEX_1 -> OfflineFriendImage(imageId)
            imageId matches IMAGE_RESOURCE_ID_REGEX_2 -> OfflineFriendImage(imageId)
            else ->
                @Suppress("INVISIBLE_MEMBER")
                throw IllegalArgumentException("Illegal imageId: $imageId. $ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE")
        }
    }

    override fun createFileMessage(id: String, internalId: Int, name: String, size: Long): FileMessage {
        return FileMessageImpl(id, internalId, name, size)
    }

    override fun createUnsupportedMessage(struct: ByteArray): UnsupportedMessage =
        UnsupportedMessageImpl(struct.loadAs(ImMsgBody.Elem.serializer()))

    @Suppress("DEPRECATION", "OverridingDeprecatedMember")
    override suspend fun queryImageUrl(bot: Bot, image: Image): String = when (image) {
        is ConstOriginUrlAware -> image.originUrl
        is DeferredOriginUrlAware -> image.getUrl(bot)
        is SuspendDeferredOriginUrlAware -> image.getUrl(bot)
        else -> error("Internal error: unsupported image class: ${image::class.simpleName}")
    }

    override suspend fun queryProfile(bot: Bot, targetId: Long): UserProfile {
        bot.asQQAndroidBot().network.apply {
            return SummaryCard.ReqSummaryCard(bot.client, targetId)
                .sendAndExpect()
        }
    }

    override suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean {
        if (bot.configuration.protocol != BotConfiguration.MiraiProtocol.ANDROID_PHONE) {
            throw UnsupportedOperationException("nudge is supported only with protocol ANDROID_PHONE")
        }
        bot.asQQAndroidBot()

        bot.network.run {
            return if (receiver is Group) {
                receiver.checkIsGroupImpl()
                NudgePacket.troopInvoke(
                    client = bot.client,
                    messageReceiverGroupCode = receiver.id,
                    nudgeTargetId = nudge.target.id,
                ).sendAndExpect<NudgePacket.Response>().success
            } else {
                NudgePacket.friendInvoke(
                    client = bot.client,
                    messageReceiverUin = receiver.id,
                    nudgeTargetId = nudge.target.id,
                ).sendAndExpect<NudgePacket.Response>().success
            }
        }
    }

    override fun constructMessageSource(
        botId: Long,
        kind: MessageSourceKind,
        fromId: Long,
        targetId: Long,
        ids: IntArray,
        time: Int,
        internalIds: IntArray,
        originalMessage: MessageChain
    ): OfflineMessageSource = OfflineMessageSourceImplData(
        kind, ids, botId, time, fromId, targetId, originalMessage, internalIds
    )

    override suspend fun downloadLongMessage(bot: Bot, resourceId: String): MessageChain {
        return downloadMultiMsgTransmit(bot, resourceId, ResourceKind.LONG_MESSAGE).msg
            .toMessageChainNoSource(bot, 0, MessageSourceKind.GROUP)
            .refineDeep(bot)
    }

    override suspend fun downloadForwardMessage(bot: Bot, resourceId: String): List<ForwardMessage.Node> {
        return downloadMultiMsgTransmit(bot, resourceId, ResourceKind.FORWARD_MESSAGE).toForwardMessageNodes(bot)
    }

    internal open suspend fun MsgTransmit.PbMultiMsgNew.toForwardMessageNodes(
        bot: Bot,
        context: RefineContext
    ): List<ForwardMessage.Node> {
        return msg.map { it.toNode(bot, context) }
    }

    internal open suspend fun MsgTransmit.PbMultiMsgTransmit.toForwardMessageNodes(bot: Bot): List<ForwardMessage.Node> {
        val pbs = this.pbItemList.associate {
            it.fileName to it.buffer.loadAs(MsgTransmit.PbMultiMsgNew.serializer())
        }
        val main = pbs["MultiMsg"] ?: return this.msg.map { it.toNode(bot, EmptyRefineContext) }
        val context = SimpleRefineContext(mutableMapOf())
        context[ForwardMessageInternal.MsgTransmits] = pbs
        return main.toForwardMessageNodes(bot, context)
    }

    protected open suspend fun MsgComm.Msg.toNode(bot: Bot, refineContext: RefineContext): ForwardMessage.Node {
        val msg = this
        return ForwardMessage.Node(
            senderId = msg.msgHead.fromUin,
            time = msg.msgHead.msgTime,
            senderName = msg.msgHead.groupInfo?.groupCard
                ?: msg.msgHead.fromNick.takeIf { it.isNotEmpty() }
                ?: msg.msgHead.fromUin.toString(),
            messageChain = listOf(msg)
                .toMessageChainNoSource(bot, 0, MessageSourceKind.GROUP)
                .refineDeep(bot, refineContext)
        )
    }

    private suspend fun downloadMultiMsgTransmit(
        bot: Bot,
        resourceId: String,
        resourceKind: ResourceKind,
    ): MsgTransmit.PbMultiMsgTransmit {
        bot.asQQAndroidBot()
        when (val resp = MultiMsg.ApplyDown(bot.client, 2, resourceId, 1).sendAndExpect(bot)) {
            is MultiMsg.ApplyDown.Response.RequireDownload -> {
                val http = Mirai.Http
                val origin = resp.origin

                val data: ByteArray = if (origin.msgExternInfo?.channelType == 2) {
                    tryDownload(
                        bot = bot,
                        host = "https://ssl.htdata.qq.com",
                        port = 443,
                        times = 3,
                        resourceKind = resourceKind,
                        channelKind = ChannelKind.HTTP
                    ) { host, _ ->
                        http.get("$host${origin.thumbDownPara}")
                    }
                } else tryServersDownload(
                    bot = bot,
                    servers = origin.uint32DownIp.zip(origin.uint32DownPort),
                    resourceKind = resourceKind,
                    channelKind = ChannelKind.HTTP
                ) { ip, port ->
                    http.get("http://$ip:$port${origin.thumbDownPara}")
                }

                val body = data.read {
                    check(readByte() == 40.toByte()) {
                        "bad data while MultiMsg.ApplyDown: ${data.toUHexString()}"
                    }
                    val headLength = readInt()
                    val bodyLength = readInt()
                    discardExact(headLength)
                    readBytes(bodyLength)
                }

                val decrypted = TEA.decrypt(body, origin.msgKey)
                val longResp =
                    decrypted.loadAs(LongMsg.RspBody.serializer())

                val down = longResp.msgDownRsp.single()
                check(down.result == 0) {
                    "Message download failed, result=${down.result}, resId=${down.msgResid.encodeToString()}, msgContent=${down.msgContent.toUHexString()}"
                }

                val content = down.msgContent.ungzip()
                return content.loadAs(MsgTransmit.PbMultiMsgTransmit.serializer())
            }
            MultiMsg.ApplyDown.Response.MessageTooLarge -> {
                error("Message is too large and cannot download")
            }
        }
    }
}