/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.qqandroid

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.int
import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.internal.MiraiAtomicBoolean
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.qqandroid.contact.FriendImpl
import net.mamoe.mirai.qqandroid.contact.GroupImpl
import net.mamoe.mirai.qqandroid.contact.MemberInfoImpl
import net.mamoe.mirai.qqandroid.contact.checkIsGroupImpl
import net.mamoe.mirai.qqandroid.message.*
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.highway.HighwayHelper
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.LongMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.qqandroid.utils.encodeToString
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.*
import kotlin.collections.asSequence
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmField
import kotlin.jvm.JvmSynthetic
import kotlin.math.absoluteValue
import kotlin.random.Random
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.FriendInfo as JceFriendInfo

internal fun Bot.asQQAndroidBot(): QQAndroidBot {
    contract {
        returns() implies (this@asQQAndroidBot is QQAndroidBot)
    }

    return this as QQAndroidBot
}

@Suppress("INVISIBLE_MEMBER", "BooleanLiteralArgument", "OverridingDeprecatedMember")
internal class QQAndroidBot constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase(context, account, configuration) {

    @OptIn(LowLevelAPI::class)
    override suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent) {
        check(event.bot === this) {
            "the request $event is from Bot ${event.bot.id} but you are responding with bot ${this.id}"
        }

        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        check(!friends.contains(event.fromId)) {
            "the request $event is outdated: You had already responded it on another device."
        }

        _lowLevelSolveNewFriendRequestEvent(
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            accept = true,
            blackList = false
        )
    }

    override suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean) {
        check(event.bot === this) {
            "the request $event is from Bot ${event.bot.id} but you are responding with bot ${this.id}"
        }

        check(event.responded.compareAndSet(false, true)) {
            "the request $event has already been responded"
        }

        check(!friends.contains(event.fromId)) {
            "the request $event is outdated: You had already responded it on another device."
        }

        _lowLevelSolveNewFriendRequestEvent(
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            accept = false,
            blackList = blackList
        )
    }

    @OptIn(LowLevelAPI::class)
    override suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent) {
        @Suppress("DuplicatedCode")
        checkGroupPermission(event.bot, event.group) { event::class.simpleName ?: "<anonymous class>" }
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        check(!event.group.members.contains(event.fromId)) {
            "the request $this is outdated: Another operator has already responded it."
        }

        _lowLevelSolveMemberJoinRequestEvent(
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            groupId = event.groupId,
            accept = true,
            blackList = false
        )
    }

    @Suppress("DuplicatedCode")
    @OptIn(LowLevelAPI::class)
    override suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean) {
        rejectMemberJoinRequest(event, blackList, "")
    }

    @Suppress("DuplicatedCode")
    @OptIn(LowLevelAPI::class)
    override suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean, message: String) {
        checkGroupPermission(event.bot, event.group) { event::class.simpleName ?: "<anonymous class>" }
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        check(!event.group.members.contains(event.fromId)) {
            "the request $this is outdated: Another operator has already responded it."
        }

        _lowLevelSolveMemberJoinRequestEvent(
            eventId = event.eventId,
            fromId = event.fromId,
            fromNick = event.fromNick,
            groupId = event.groupId,
            accept = false,
            blackList = blackList,
            message = message
        )
    }

    private inline fun checkGroupPermission(eventBot: Bot, eventGroup: Group, eventName: () -> String) {
        val group = this.getGroupOrNull(eventGroup.id)
            ?: kotlin.run {
                if (this == eventBot) {
                    error(
                        "A ${eventName()} is outdated. Group ${eventGroup.id} not found for bot ${this.id}. " +
                                "This is because bot isn't in the group anymore"
                    )
                } else {
                    error("A ${eventName()} is from bot ${eventBot.id}, but you are trying to respond it using bot ${this.id} who isn't a member of the group ${eventGroup.id}")
                }
            }

        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
    }

    override suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean) {
        checkGroupPermission(event.bot, event.group) { event::class.simpleName ?: "<anonymous class>" }
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        _lowLevelSolveMemberJoinRequestEvent(
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


    private suspend fun solveInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent, accept: Boolean) {
        check(event.responded.compareAndSet(false, true)) {
            "the request $this has already been responded"
        }

        check(!groups.contains(event.groupId)) {
            "the request $this is outdated: Bot has been already in the group."
        }

        _lowLevelSolveBotInvitedJoinGroupRequestEvent(
            eventId = event.eventId,
            invitorId = event.invitorId,
            groupId = event.groupId,
            accept = accept
        )
    }
}


internal abstract class QQAndroidBotBase constructor(
    context: Context,
    private val account: BotAccount,
    configuration: BotConfiguration
) : BotImpl<QQAndroidBotNetworkHandler>(context, configuration) {
    val client: QQAndroidClient =
        QQAndroidClient(
            context,
            account,
            bot = @Suppress("LeakingThis") this as QQAndroidBot,
            device = configuration.deviceInfo?.invoke(context) ?: SystemDeviceInfo(context)
        )
    internal var firstLoginSucceed: Boolean = false

    override val id: Long
        get() = account.id

    private inline val json get() = configuration.json

    override val friends: ContactList<Friend> = ContactList(LockFreeLinkedList())

    override val nick: String get() = selfInfo.nick

    internal lateinit var selfInfo: JceFriendInfo

    override val selfQQ: Friend by lazy {
        @OptIn(LowLevelAPI::class)
        _lowLevelNewFriend(object : FriendInfo {
            override val uin: Long get() = this@QQAndroidBotBase.id
            override val nick: String get() = this@QQAndroidBotBase.nick
        })
    }

    /**
     * Final process for 'login'
     */
    @ThisApiMustBeUsedInWithConnectionLockBlock
    @Throws(LoginFailedException::class) // only
    override suspend fun relogin(cause: Throwable?) {
        client.useNextServers { host, port ->
            network.closeEverythingAndRelogin(host, port, cause)
        }
    }

    @LowLevelAPI
    override fun _lowLevelNewFriend(friendInfo: FriendInfo): Friend {
        return FriendImpl(
            this as QQAndroidBot,
            coroutineContext + CoroutineName("Friend(${friendInfo.uin}"),
            friendInfo.uin,
            friendInfo
        )
    }

    override fun createNetworkHandler(coroutineContext: CoroutineContext): QQAndroidBotNetworkHandler {
        return QQAndroidBotNetworkHandler(coroutineContext, this as QQAndroidBot)
    }

    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    @JvmField
    val groupListModifyLock = Mutex()

    // internally visible only
    fun getGroupByUin(uin: Long): Group {
        return getGroupByUinOrNull(uin)
            ?: throw NoSuchElementException("Group ${Group.calculateGroupCodeByGroupUin(uin)} not found")
    }

    fun getGroupByUinOrNull(uin: Long): Group? {
        return groups.asSequence().firstOrNull { it.checkIsGroupImpl(); it.uin == uin }
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
        ).sendAndExpect<GroupInfoImpl>(retry = 3)
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
                ).sendAndExpect<FriendList.GetTroopMemberList.Response>(retry = 3)
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

    @Suppress("RemoveExplicitTypeArguments") // false positive
    override suspend fun recall(source: MessageSource) {
        check(source is MessageSourceInternal)
        source.ensureSequenceIdAvailable()

        @Suppress("BooleanLiteralArgument") // false positive
        check(!source.isRecalledOrPlanned.value && source.isRecalledOrPlanned.compareAndSet(false, true)) {
            "$source had already been recalled."
        }

        val response: PbMessageSvc.PbMsgWithDraw.Response = when (source) {
            is MessageSourceToGroupImpl,
            is MessageSourceFromGroupImpl
            -> {
                val group = when (source) {
                    is MessageSourceToGroupImpl -> source.target
                    is MessageSourceFromGroupImpl -> source.group
                    else -> error("stub")
                }
                if (this.id != source.fromId) {
                    group.checkBotPermission(MemberPermission.ADMINISTRATOR)
                }
                MessageRecallEvent.GroupRecall(
                    this,
                    source.fromId,
                    source.id,
                    source.internalId,
                    source.time,
                    null,
                    group
                ).broadcast()

                network.run {
                    PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                        bot.asQQAndroidBot().client,
                        group.id,
                        source.sequenceId,
                        source.internalId
                    ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
                }
            }
            is MessageSourceFromFriendImpl,
            is MessageSourceToFriendImpl
            -> network.run {
                check(source.fromId == this@QQAndroidBotBase.id) {
                    "can only recall a message sent by bot"
                }
                PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                    bot.client,
                    source.targetId,
                    source.sequenceId,
                    source.internalId,
                    source.time
                ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
            }
            is MessageSourceFromTempImpl,
            is MessageSourceToTempImpl
            -> network.run {
                check(source.fromId == this@QQAndroidBotBase.id) {
                    "can only recall a message sent by bot"
                }
                source as MessageSourceToTempImpl
                PbMessageSvc.PbMsgWithDraw.createForTempMessage(
                    bot.client,
                    (source.target.group as GroupImpl).uin,
                    source.targetId,
                    source.sequenceId,
                    source.internalId,
                    source.time
                ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
            }
            is OfflineMessageSource -> network.run {
                when (source.kind) {
                    OfflineMessageSource.Kind.FRIEND -> {
                        check(source.fromId == this@QQAndroidBotBase.id) {
                            "can only recall a message sent by bot"
                        }
                        PbMessageSvc.PbMsgWithDraw.createForFriendMessage(
                            bot.client,
                            source.targetId,
                            source.sequenceId,
                            source.internalId,
                            source.time
                        ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
                    }
                    OfflineMessageSource.Kind.TEMP -> {
                        check(source.fromId == this@QQAndroidBotBase.id) {
                            "can only recall a message sent by bot"
                        }
                        PbMessageSvc.PbMsgWithDraw.createForTempMessage(
                            bot.client,
                            source.targetId, // groupUin
                            source.targetId, // memberUin
                            source.sequenceId,
                            source.internalId,
                            source.time
                        ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
                    }
                    OfflineMessageSource.Kind.GROUP -> {
                        PbMessageSvc.PbMsgWithDraw.createForGroupMessage(
                            bot.client,
                            source.targetId,
                            source.sequenceId,
                            source.internalId
                        ).sendAndExpect<PbMessageSvc.PbMsgWithDraw.Response>()
                    }
                }
            }
            else -> error("stub!")
        }


        // 1001: No message meets the requirements (实际上是没权限, 管理员在尝试撤回群主的消息)
        // 154: timeout
        // 3: <no message>
        check(response is PbMessageSvc.PbMsgWithDraw.Response.Success) { "Failed to recall message #${source.id}: $response" }
    }

    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelGetAnnouncements(groupId: Long, page: Int, amount: Int): GroupAnnouncementList {
        val data = network.async {
            MiraiPlatformUtils.Http.post<String> {
                url("https://web.qun.qq.com/cgi-bin/announce/list_announce")
                body = MultiPartFormDataContent(formData {
                    append("qid", groupId)
                    append("bkn", bkn)
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

        val rep = data.await()
//        bot.network.logger.error(rep)
        return json.parse(GroupAnnouncementList.serializer(), rep)
    }

    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelSendAnnouncement(groupId: Long, announcement: GroupAnnouncement): String {
        val rep = withContext(network.coroutineContext) {
            MiraiPlatformUtils.Http.post<String> {
                url("https://web.qun.qq.com/cgi-bin/announce/add_qun_notice")
                body = MultiPartFormDataContent(formData {
                    append("qid", groupId)
                    append("bkn", bkn)
                    append("text", announcement.msg.text)
                    append("pinned", announcement.pinned)
                    append(
                        "settings",
                        json.stringify(
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
        }
        val jsonObj = json.parseJson(rep)
        return jsonObj.jsonObject["new_fid"]?.primitive?.content
            ?: throw throw IllegalStateException("Send Announcement fail group:$groupId msg:${jsonObj.jsonObject["em"]} content:${announcement.msg.text}")
    }

    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelDeleteAnnouncement(groupId: Long, fid: String) {
        val data = withContext(network.coroutineContext) {
            MiraiPlatformUtils.Http.post<String> {
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
        }
        val jsonObj = json.parseJson(data)
        if (jsonObj.jsonObject["ec"]?.int ?: 1 != 0) {
            throw throw IllegalStateException("delete Announcement fail group:$groupId msg:${jsonObj.jsonObject["em"]} fid:$fid")
        }
    }

    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelGetAnnouncement(groupId: Long, fid: String): GroupAnnouncement {
        val data = network.async {
            HttpClient().post<String> {
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

        val rep = data.await()
//        bot.network.logger.error(rep)
        return json.parse(GroupAnnouncement.serializer(), rep)

    }

    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelGetGroupActiveData(groupId: Long, page: Int): GroupActiveData {
        val data = network.async {
            HttpClient().get<String> {
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
        val rep = data.await()
        return json.parse(GroupActiveData.serializer(), rep)
    }

    @JvmSynthetic
    @LowLevelAPI
    @MiraiExperimentalAPI
    internal suspend fun lowLevelSendGroupLongOrForwardMessage(
        groupCode: Long,
        message: Collection<ForwardMessage.INode>,
        isLong: Boolean,
        forwardMessage: ForwardMessage?
    ): MessageReceipt<Group> {
        message.forEach {
            it.message.ensureSequenceIdAvailable()
        }

        val group = getGroup(groupCode)

        val time = currentTimeSeconds
        val sequenceId = client.atomicNextMessageSequenceId()

        network.run {
            val data = message.calculateValidationDataForGroup(
                sequenceId = sequenceId,
                random = Random.nextInt().absoluteValue,
                groupCode = groupCode
            )

            val response =
                MultiMsg.ApplyUp.createForGroupLongMessage(
                    buType = if (isLong) 1 else 2,
                    client = this@QQAndroidBotBase.client,
                    messageData = data,
                    dstUin = Group.calculateGroupUinByGroupCode(groupCode)
                ).sendAndExpect<MultiMsg.ApplyUp.Response>()

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
                                dstUin = Group.calculateGroupUinByGroupCode(groupCode),
                                msgId = 0,
                                msgUkey = response.proto.msgUkey,
                                needCache = 0,
                                storeType = 2,
                                msgContent = data.data
                            )
                        )
                    ).toByteArray(LongMsg.ReqBody.serializer())

                    HighwayHelper.uploadImageToServers(
                        bot,
                        response.proto.uint32UpIp.zip(response.proto.uint32UpPort),
                        response.proto.msgSig,
                        MiraiPlatformUtils.md5(body),
                        @Suppress("INVISIBLE_REFERENCE")
                        net.mamoe.mirai.utils.internal.asReusableInput0(body), // don't use toLongUnsigned: Overload resolution ambiguity
                        "group long message",
                        27
                    )
                }
            }

            return if (isLong) {
                group.sendMessage(
                    RichMessage.longMessage(
                        brief = message.joinToString(limit = 27) { it.message.contentToString() },
                        resId = resId,
                        timeSeconds = time
                    )
                )
            } else {
                checkNotNull(forwardMessage) { "Internal error: forwardMessage is null when sending forward" }
                group.sendMessage(
                    RichMessage.forwardMessage(
                        resId = resId,
                        timeSeconds = time,
                        //  preview = message.take(5).joinToString {
                        //      """
                        //          <title size="26" color="#777777" maxLines="2" lineSpace="12">${it.message.asMessageChain().joinToString(limit = 10)}</title>
                        //      """.trimIndent()
                        //  },
                        preview = forwardMessage.displayStrategy.generatePreview(forwardMessage).take(4)
                            .map {
                                """<title size="26" color="#777777" maxLines="2" lineSpace="12">$it</title>"""
                            }.joinToString(""),
                        title = forwardMessage.displayStrategy.generateTitle(forwardMessage),
                        brief = forwardMessage.displayStrategy.generateBrief(forwardMessage),
                        source = forwardMessage.displayStrategy.generateSource(forwardMessage),
                        summary = forwardMessage.displayStrategy.generateSummary(forwardMessage)
                    )
                )
            }
        }
    }


    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelSolveNewFriendRequestEvent(
        eventId: Long,
        fromId: Long,
        fromNick: String,
        accept: Boolean,
        blackList: Boolean
    ) {
        network.apply {
            NewContact.SystemMsgNewFriend.Action(
                bot.client,
                eventId = eventId,
                fromId = fromId,
                accept = accept,
                blackList = blackList
            ).sendWithoutExpect()
            bot.friends.delegate.addLast(bot._lowLevelNewFriend(object : FriendInfo {
                override val uin: Long get() = fromId
                override val nick: String get() = fromNick
            }))
        }
    }

    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelSolveBotInvitedJoinGroupRequestEvent(
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    ) {
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

    @LowLevelAPI
    @MiraiExperimentalAPI
    override suspend fun _lowLevelSolveMemberJoinRequestEvent(
        eventId: Long,
        fromId: Long,
        fromNick: String,
        groupId: Long,
        accept: Boolean?,
        blackList: Boolean,
        message: String
    ) {
        network.apply {
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
            if (accept ?: return)
                groups[groupId].apply {
                    members.delegate.addLast(newMember(object : MemberInfo {
                        override val nameCard: String get() = ""
                        override val permission: MemberPermission get() = MemberPermission.MEMBER
                        override val specialTitle: String get() = ""
                        override val muteTimestamp: Int get() = 0
                        override val uin: Long get() = fromId
                        override val nick: String get() = fromNick
                    }))
                }
        }
    }

    @Suppress("DEPRECATION", "OverridingDeprecatedMember")
    override suspend fun queryImageUrl(image: Image): String = when (image) {
        is ConstOriginUrlAware -> image.originUrl
        is DeferredOriginUrlAware -> image.getUrl(this)
        is SuspendDeferredOriginUrlAware -> image.getUrl(this)
        else -> error("Internal error: unsupported image class: ${image::class.simpleName}")
    }

    override fun constructMessageSource(
        kind: OfflineMessageSource.Kind,
        fromUin: Long,
        targetUin: Long,
        id: Int,
        time: Int,
        internalId: Int,
        originalMessage: MessageChain
    ): OfflineMessageSource {
        return object : OfflineMessageSource(), MessageSourceInternal {
            override val kind: Kind get() = kind
            override val id: Int get() = id
            override val bot: Bot get() = this@QQAndroidBotBase
            override val time: Int get() = time
            override val fromId: Long get() = fromUin
            override val targetId: Long get() = targetUin
            override val originalMessage: MessageChain get() = originalMessage
            override val sequenceId: Int = id
            override val internalId: Int = internalId
            override var isRecalledOrPlanned: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

            override fun toJceData(): ImMsgBody.SourceMsg {
                return ImMsgBody.SourceMsg(
                    origSeqs = listOf(sequenceId),
                    senderUin = fromUin,
                    toUin = 0,
                    flag = 1,
                    elems = originalMessage.toRichTextElems(forGroup = kind == Kind.GROUP, withGeneralFlags = false),
                    type = 0,
                    time = time,
                    pbReserve = EMPTY_BYTE_ARRAY,
                    srcMsg = EMPTY_BYTE_ARRAY
                )
            }
        }
    }

    /**
     * 获取 获取群公告 所需的 bkn 参数
     * */
    private val bkn: Int
        get() = client.wLoginSigInfo.sKey.data
            .fold(5381) { acc: Int, b: Byte -> acc + acc.shl(5) + b.toInt() }
}

internal val EMPTY_BYTE_ARRAY = ByteArray(0)

private fun RichMessage.Templates.longMessage(brief: String, resId: String, timeSeconds: Long): RichMessage {
    val limited: String = if (brief.length > 30) {
        brief.take(30) + "…"
    } else {
        brief
    }

    val template = """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <msg serviceID="35" templateID="1" action="viewMultiMsg"
                     brief="$limited"
                     m_resid="$resId"
                     m_fileName="$timeSeconds" sourceMsgId="0" url=""
                     flag="3" adverSign="0" multiMsgFlag="1">
                    <item layout="1">
                        <title>$limited</title>
                        <hr hidden="false" style="0"/>
                        <summary>点击查看完整消息</summary>
                    </item>
                    <source name="聊天记录" icon="" action="" appid="-1"/>
                </msg>
            """.trimIndent()

    return LongMessage(template, resId)
}


private fun RichMessage.Templates.forwardMessage(
    resId: String,
    timeSeconds: Long,
    preview: String,
    title: String,
    brief: String,
    source: String,
    summary: String
): ForwardMessageInternal {
    val template = """
        <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
        <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="$brief"
             m_resid="$resId" m_fileName="$timeSeconds"
             tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">
            <item layout="1" advertiser_id="0" aid="0">
                <title size="34" maxLines="2" lineSpace="12">$title</title>
                $preview
                <hr hidden="false" style="0"/>
                <summary size="26" color="#777777">$summary</summary>
            </item>
            <source name="$source" icon="" action="" appid="-1"/>
        </msg>
    """.trimIndent().replace("\n", " ")
    return ForwardMessageInternal(template)
}