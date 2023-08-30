/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("MiraiImplKt_common")

package net.mamoe.mirai.internal

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl.Companion.impl
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.contact.info.StrangerInfoImpl.Companion.impl
import net.mamoe.mirai.internal.event.EventChannelToEventDispatcherAdapter
import net.mamoe.mirai.internal.event.InternalEventMechanism
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.EmptyRefineContext
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.message.SimpleRefineContext
import net.mamoe.mirai.internal.message.data.*
import net.mamoe.mirai.internal.message.image.*
import net.mamoe.mirai.internal.message.source.*
import net.mamoe.mirai.internal.message.toMessageChainNoSource
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.highway.tryDownload
import net.mamoe.mirai.internal.network.highway.tryServersDownload
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcDevLoginInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.LongMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.internal.network.protocol.packet.chat.MultiMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.NewContact
import net.mamoe.mirai.internal.network.protocol.packet.chat.NudgePacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.PbMessageSvc
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.summarycard.SummaryCard
import net.mamoe.mirai.internal.network.psKey
import net.mamoe.mirai.internal.network.sKey
import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmName

internal fun getMiraiImpl() = Mirai as MiraiImpl

internal expect fun createDefaultHttpClient(): HttpClient

// used by `net.mamoe.mirai.deps.test.CoreDependencyResolutionTest` in mirai-deps-test module. Do not change signature.
@Suppress("unused")
@TestOnly
internal fun testHttpClient() {
    createDefaultHttpClient().close()
}

@Suppress("FunctionName")
internal expect fun _MiraiImpl_static_init()

@OptIn(LowLevelApi::class)
// not object for ServiceLoader.
internal open class MiraiImpl : IMirai, LowLevelApiAccessor {
    init {
        _MiraiImpl_static_init() // companion object is lazily initialized on native
    }

    companion object {
        init {
            _MiraiImpl_static_init()
        }
    }

    override val BotFactory: BotFactory
        get() = BotFactoryImpl

    override var FileCacheStrategy: FileCacheStrategy = net.mamoe.mirai.utils.FileCacheStrategy.PlatformDefault

    private val httpClient: HttpClient = createDefaultHttpClient()

    override suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent) {
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
            bot.network.sendAndExpect(StatSvc.GetDevLoginInfo(bot.client))
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

    @OptIn(InternalEventMechanism::class)
    override suspend fun broadcastEvent(event: Event) {
        if (event is BotEvent) {
            val bot = event.bot
            if (bot is AbstractBot) {
                bot.components[EventDispatcher].broadcast(event)
            }
        } else {
            EventChannelToEventDispatcherAdapter.instance.broadcastEventImpl(event)
        }
    }

    private suspend fun solveInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent, accept: Boolean) {
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
    override fun newFriend(bot: Bot, friendInfo: FriendInfo): FriendImpl {
        return FriendImpl(
            bot.asQQAndroidBot(),
            bot.coroutineContext,
            friendInfo.impl(),
        )
    }

    @LowLevelApi
    override fun newStranger(bot: Bot, strangerInfo: StrangerInfo): StrangerImpl {
        return StrangerImpl(
            bot.asQQAndroidBot(),
            bot.coroutineContext,
            strangerInfo.impl(),
        )
    }


    @OptIn(LowLevelApi::class)
    override suspend fun getRawGroupList(bot: Bot): Sequence<Long> {
        bot.asQQAndroidBot()
        return bot.network.run {
            bot.network.sendAndExpect(FriendList.GetTroopListSimplify(bot.client))
        }.groups.asSequence().map { it.groupUin.shl(32) and it.groupCode }
    }

    @OptIn(LowLevelApi::class)
    override suspend fun getRawGroupMemberList(
        bot: Bot,
        groupUin: Long,
        groupCode: Long,
        ownerId: Long
    ): Sequence<MemberInfo> {
        var nextUin = 0L
        var sequence = sequenceOf<MemberInfoImpl>()
        while (true) {
            val data = bot.asQQAndroidBot().network.sendAndExpect(
                FriendList.GetTroopMemberList(
                    client = bot.client,
                    targetGroupUin = groupUin,
                    targetGroupCode = groupCode,
                    nextUin = nextUin
                ), 5000, 3
            )
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
    ): Boolean {
        val response = bot.asQQAndroidBot().network.sendAndExpect(
            PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                bot.client,
                groupCode,
                messageIds,
                messageInternalIds
            ), 5000, 2
        )

        return response is PbMessageSvc.PbMsgWithDraw.Response.Success
    }

    override suspend fun recallFriendMessageRaw(
        bot: Bot,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int,
    ): Boolean {
        val response = bot.asQQAndroidBot().network.sendAndExpect(
            PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                bot.client,
                targetId,
                messageIds,
                messageInternalIds,
                time,
            ), 5000, 2
        )

        return response is PbMessageSvc.PbMsgWithDraw.Response.Success
    }

    override suspend fun recallGroupTempMessageRaw(
        bot: Bot,
        groupUin: Long,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int
    ): Boolean {
        val response = bot.asQQAndroidBot().network.sendAndExpect(
            PbMessageSvc.PbMsgWithDraw.createForGroupTempMessage(
                bot.client,
                groupUin,
                targetId,
                messageIds,
                messageInternalIds,
                time,
            ), 5000, 2
        )

        return response is PbMessageSvc.PbMsgWithDraw.Response.Success
    }

    override suspend fun recallMessage(bot: Bot, source: MessageSource) = bot.asQQAndroidBot().run {
        check(source is MessageSourceInternal)

        source.ensureSequenceIdAvailable()

        check(!source.isRecalledOrPlanned && source.setRecalled()) {
            "$source had already been recalled."
        }

        val response: PbMessageSvc.PbMsgWithDraw.Response = when (source) {
            is OnlineMessageSourceToGroupImpl,
            is OnlineMessageSourceFromGroupImpl
            -> {
                val group: Group = when (source) {
                    is OnlineMessageSourceToGroupImpl -> source.subject
                    is OnlineMessageSourceFromGroupImpl -> source.subject
                    else -> error("stub")
                }
                if (bot.id != source.fromId) {
                    // if member leave, messageSource will throw exception(#1661)
                    when (group[source.fromId]?.permission ?: MemberPermission.MEMBER) {
                        MemberPermission.MEMBER -> group.checkBotPermission(MemberPermission.ADMINISTRATOR)
                        MemberPermission.ADMINISTRATOR -> group.checkBotPermission(MemberPermission.OWNER)
                        // bot cannot be owner
                        MemberPermission.OWNER -> throw PermissionDeniedException("Permission denied: cannot recall message from owner")
                    }
                }


                bot.asQQAndroidBot().network.sendAndExpect(
                    PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                        bot.asQQAndroidBot().client,
                        group.id,
                        source.sequenceIds,
                        source.internalIds
                    ), 5000, 2
                )
            }
            is OnlineMessageSourceFromFriendImpl,
            is OnlineMessageSourceToFriendImpl,
            is OnlineMessageSourceFromStrangerImpl,
            is OnlineMessageSourceToStrangerImpl,
            -> {
                check(source.fromId == bot.id) {
                    "can only recall a message sent by bot"
                }
                bot.asQQAndroidBot().network.sendAndExpect(
                    PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                        bot.client,
                        source.targetId,
                        source.sequenceIds,
                        source.internalIds,
                        source.time
                    ), 5000, 2
                )
            }
            is OnlineMessageSourceFromTempImpl,
            is OnlineMessageSourceToTempImpl
            -> {
                check(source.fromId == bot.id) {
                    "can only recall a message sent by bot"
                }
                source as OnlineMessageSourceToTempImpl
                bot.asQQAndroidBot().network.sendAndExpect(
                    PbMessageSvc.PbMsgWithDraw.createForGroupTempMessage(
                        bot.client,
                        (source.target.group as GroupImpl).uin,
                        source.targetId,
                        source.sequenceIds,
                        source.internalIds,
                        source.time
                    ), 5000, 2
                )
            }
            is OfflineMessageSource -> {
                when (source.kind) {
                    MessageSourceKind.FRIEND, MessageSourceKind.STRANGER -> {
                        check(source.fromId == bot.id) {
                            "can only recall a message sent by bot"
                        }
                        bot.asQQAndroidBot().network.sendAndExpect(
                            PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                                bot.client,
                                source.targetId,
                                source.sequenceIds,
                                source.internalIds,
                                source.time
                            ), 5000, 2
                        )
                    }
                    MessageSourceKind.TEMP -> {
                        check(source.fromId == bot.id) {
                            "can only recall a message sent by bot"
                        }
                        bot.asQQAndroidBot().network.sendAndExpect(
                            PbMessageSvc.PbMsgWithDraw.createForGroupTempMessage(
                                bot.client,
                                source.targetId, // groupUin
                                source.targetId, // memberUin
                                source.sequenceIds,
                                source.internalIds,
                                source.time
                            ), 5000, 2
                        )
                    }
                    MessageSourceKind.GROUP -> {
                        bot.asQQAndroidBot().network.sendAndExpect(
                            PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                                bot.client,
                                source.targetId,
                                source.sequenceIds,
                                source.internalIds
                            ), 5000, 2
                        )
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

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }


    override suspend fun solveNewFriendRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        accept: Boolean,
        blackList: Boolean
    ): Unit = bot.asQQAndroidBot().run {
        network.sendWithoutExpect(
            NewContact.SystemMsgNewFriend.Action(
                bot.client,
                eventId = eventId,
                fromId = fromId,
                accept = accept,
                blackList = blackList
            )
        )

        if (!accept) return

        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        bot.friends.delegate.add(newFriend(bot, FriendInfoImpl(fromId, fromNick, "", 0)))
    }

    override suspend fun solveBotInvitedJoinGroupRequestEvent(
        bot: Bot,
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    ) {
        bot.asQQAndroidBot().network.sendWithoutExpect(
            NewContact.SystemMsgNewGroup.Action(
                bot.client,
                eventId = eventId,
                fromId = invitorId,
                groupId = groupId,
                isInvited = true,
                accept = accept
            )
        )
    }

    override suspend fun solveMemberJoinRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        groupId: Long,
        accept: Boolean?,
        blackList: Boolean,
        message: String
    ) {
        bot.asQQAndroidBot().network.sendWithoutExpect(
            NewContact.SystemMsgNewGroup.Action(
                bot.client,
                eventId = eventId,
                fromId = fromId,
                groupId = groupId,
                isInvited = false,
                accept = accept,
                blackList = blackList,
                message = message
            )
        )
        // Add member in MsgOnlinePush.PbPushMsg
    }

    @LowLevelApi
    override suspend fun getGroupVoiceDownloadUrl(
        bot: Bot,
        md5: ByteArray,
        groupId: Long,
        dstUin: Long
    ): String {
        val response = bot.asQQAndroidBot().network.sendAndExpect(
            PttStore.GroupPttDown(bot.client, groupId, dstUin, md5),
            5000,
            2
        )
        return "http://${response.strDomain}${response.downPara.decodeToString()}"
    }

    override suspend fun muteAnonymousMember(
        bot: Bot,
        anonymousId: String,
        anonymousNick: String,
        groupId: Long,
        seconds: Int
    ) {
        bot as QQAndroidBot
        val response = httpClient.post {
            url("https://qqweb.qq.com/c/anonymoustalk/blacklist")
            setBody(
                MultiPartFormDataContent(formData {
                    append("anony_id", anonymousId)
                    append("group_code", groupId)
                    append("seconds", seconds)
                    append("anony_nick", anonymousNick)
                    append("bkn", bot.client.wLoginSigInfo.bkn)
                })
            )
            headers {
                // ktor bug
                append(
                    "cookie",
                    "uin=o${bot.id}; skey=${bot.sKey}; p_uin=o${bot.id}; p_skey=${bot.psKey(host)};"
                )
            }
        }.bodyAsText()
        val jsonObj = Json.decodeFromString(JsonObject.serializer(), response)
        if ((jsonObj["retcode"] ?: jsonObj["cgicode"] ?: error("missing response code")).jsonPrimitive.long != 0L) {
            throw IllegalStateException(response)
        }
    }

    override fun createFileMessage(id: String, internalId: Int, name: String, size: Long): FileMessage {
        return FileMessageImpl(id, internalId, name, size)
    }

    override fun createUnsupportedMessage(struct: ByteArray): UnsupportedMessage =
        UnsupportedMessageImpl(struct.loadAs(ImMsgBody.Elem.serializer()))

    @Suppress("OverridingDeprecatedMember")
    override suspend fun queryImageUrl(bot: Bot, image: Image): String = when (image) {
        is ConstOriginUrlAware -> image.originUrl
        is DeferredOriginUrlAware -> image.getUrl(bot)
        is SuspendDeferredOriginUrlAware -> image.getUrl(bot)
        else -> error("Internal error: unsupported image class: ${image::class.simpleName}")
    }

    override suspend fun queryProfile(bot: Bot, targetId: Long): UserProfile {

        return bot.asQQAndroidBot().network.sendAndExpect(
            SummaryCard.ReqSummaryCard(bot.client, targetId),
            5000, 2
        )
    }

    override suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean {
        if (!bot.configuration.protocol.isNudgeSupported) {
            throw UnsupportedOperationException("nudge is supported only with protocol ${
                MiraiProtocolInternal.protocols.filter { it.value.supportsNudge }.map { it.key }
            }")
        }
        bot.asQQAndroidBot()

        bot.network.run {
            return if (receiver is Group) {
                receiver.checkIsGroupImpl()
                bot.network.sendAndExpect(
                    NudgePacket.troopInvoke(
                        client = bot.client,
                        messageReceiverGroupCode = receiver.id,
                        nudgeTargetId = nudge.target.id,
                    )
                ).success
            } else {
                bot.network.sendAndExpect(
                    NudgePacket.friendInvoke(
                        client = bot.client,
                        messageReceiverUin = receiver.id,
                        nudgeTargetId = nudge.target.id,
                    )
                ).success
            }
        }
    }

    override fun getUin(contactOrBot: ContactOrBot): Long {
        return when (contactOrBot) {
            is Group -> contactOrBot.uin
            is User -> contactOrBot.uin
            is Bot -> contactOrBot.uin
            else -> contactOrBot.id
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
        try {
            return downloadMultiMsgTransmit(bot, resourceId, ResourceKind.LONG_MESSAGE).msg
                .toMessageChainNoSource(bot, 0, MessageSourceKind.GROUP)
                .refineDeep(bot)
        } catch (error: Throwable) {
            throw IllegalStateException("Failed to download long message `$resourceId`", error)
        }
    }

    override suspend fun downloadForwardMessage(bot: Bot, resourceId: String): List<ForwardMessage.Node> {
        try {
            return downloadMultiMsgTransmit(bot, resourceId, ResourceKind.FORWARD_MESSAGE).toForwardMessageNodes(bot)
        } catch (error: Throwable) {
            throw IllegalStateException("Failed to download forward message `$resourceId`", error)
        }
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
        return main.toForwardMessageNodes(bot, SimpleRefineContext(ForwardMessageInternal.MsgTransmits to pbs))
    }

    private suspend fun MsgComm.Msg.toNode(bot: Bot, refineContext: RefineContext): ForwardMessage.Node {
        val msg = this

        @Suppress("USELESS_CAST") // compiler bug, do not remove
        val senderName = (msg.msgHead.groupInfo?.groupCard
            ?: msg.msgHead.fromNick.takeIf { it.isNotEmpty() }
            ?: msg.msgHead.fromUin.toString()) as String
        val chain = listOf(msg)
            .toMessageChainNoSource(bot, 0, MessageSourceKind.GROUP)
            .refineDeep(bot, refineContext)
        return ForwardMessage.Node(
            senderId = msg.msgHead.fromUin,
            time = msg.msgHead.msgTime,
            senderName = senderName,
            messageChain = chain
        )
    }

    private suspend fun downloadMultiMsgTransmit(
        bot: Bot,
        resourceId: String,
        resourceKind: ResourceKind,
    ): MsgTransmit.PbMultiMsgTransmit {
        bot.asQQAndroidBot()
        when (val resp = bot.network.sendAndExpect(MultiMsg.ApplyDown(bot.client, 2, resourceId, 1))) {
            is MultiMsg.ApplyDown.Response.RequireDownload -> {
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
                        httpClient.get("$host${origin.thumbDownPara}")
                    }.readBytes()
                } else tryServersDownload(
                    bot = bot,
                    servers = origin.uint32DownIp.zip(origin.uint32DownPort),
                    resourceKind = resourceKind,
                    channelKind = ChannelKind.HTTP
                ) { ip, port ->
                    httpClient.get("http://$ip:$port${origin.thumbDownPara}")
                }.readBytes()

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
                    "Message download failed, result=${down.result}, resId=${down.msgResid.decodeToString()}, msgContent=${down.msgContent.toUHexString()}"
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
