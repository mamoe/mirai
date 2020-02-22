/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid

import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.highway.HighwayHelper
import net.mamoe.mirai.qqandroid.network.highway.postImage
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.StTroopMemberInfo
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.qqandroid.utils.toIpV4AddressString
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.coroutines.CoroutineContext
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.FriendInfo as JceFriendInfo

internal abstract class ContactImpl : Contact {
    override fun hashCode(): Int {
        var result = bot.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false
        if (this::class != other::class) return false
        return this.id == other.id && this.bot == other.bot
    }
}

internal inline class FriendInfoImpl(
    private val jceFriendInfo: JceFriendInfo
) : FriendInfo {
    override val nick: String get() = jceFriendInfo.nick ?: ""
    override val uin: Long get() = jceFriendInfo.friendUin
}

internal class QQImpl(
    bot: QQAndroidBot,
    override val coroutineContext: CoroutineContext,
    override val id: Long,
    private val friendInfo: FriendInfo
) : ContactImpl(), QQ {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    override val nick: String
        get() = friendInfo.nick

    override suspend fun sendMessage(message: MessageChain): MessageReceipt<QQ> {
        val event = FriendMessageSendEvent(this, message).broadcast()
        if (event.isCancelled) {
            throw EventCancelledException("cancelled by FriendMessageSendEvent")
        }
        lateinit var source: MessageSource
        bot.network.run {
            check(
                MessageSvc.PbSendMsg.ToFriend(
                    bot.client,
                    id,
                    event.message
                ) { source = it }.sendAndExpect<MessageSvc.PbSendMsg.Response>() is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed" }
        }
        return MessageReceipt(message, source, this)
    }

    override suspend fun uploadImage(image: ExternalImage): Image = try {
        if (BeforeImageUploadEvent(this, image).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        bot.network.run {
            val response = LongConn.OffPicUp(
                bot.client, Cmd0x352.TryUpImgReq(
                    srcUin = bot.uin.toInt(),
                    dstUin = id.toInt(),
                    fileId = 0,
                    fileMd5 = image.md5,
                    fileSize = image.inputSize.toInt(),
                    fileName = image.md5.toUHexString("") + "." + image.format,
                    imgOriginal = 1,
                    imgWidth = image.width,
                    imgHeight = image.height,
                    imgType = image.imageType
                )
            ).sendAndExpect<LongConn.OffPicUp.Response>()

            return when (response) {
                is LongConn.OffPicUp.Response.FileExists -> NotOnlineImageFromFile(
                    filepath = response.resourceId,
                    md5 = response.imageInfo.fileMd5,
                    fileLength = response.imageInfo.fileSize.toInt(),
                    height = response.imageInfo.fileHeight,
                    width = response.imageInfo.fileWidth,
                    resourceId = response.resourceId
                ).also {
                    ImageUploadEvent.Succeed(this@QQImpl, image, it).broadcast()
                }
                is LongConn.OffPicUp.Response.RequireUpload -> {
                    Http.postImage("0x6ff0070", bot.uin, null, imageInput = image.input, inputSize = image.inputSize, uKeyHex = response.uKey.toUHexString(""))
//                    HighwayHelper.uploadImage(
//                        client = bot.client,
//                        serverIp = response.serverIp[0].toIpV4AddressString(),
//                        serverPort = response.serverPort[0],
//                        imageInput = image.input,
//                        inputSize = image.inputSize.toInt(),
//                        md5 = image.md5,
//                        uKey = response.uKey,
//                        commandId = 1
//                    )

                    return NotOnlineImageFromFile(
                        filepath = response.resourceId,
                        md5 = image.md5,
                        fileLength = image.inputSize.toInt(),
                        height = image.height,
                        width = image.width,
                        resourceId = response.resourceId
                    ).also {
                        ImageUploadEvent.Succeed(this@QQImpl, image, it).broadcast()
                    }
                }
                is LongConn.OffPicUp.Response.Failed -> {
                    ImageUploadEvent.Failed(this@QQImpl, image, -1, response.message).broadcast()
                    error(response.message)
                }
            }
        }
    } finally {
        image.input.close()
    }

    @MiraiExperimentalAPI
    override suspend fun queryProfile(): Profile {
        TODO("not implemented")
    }

    @MiraiExperimentalAPI
    override suspend fun queryPreviousNameList(): PreviousNameList {
        TODO("not implemented")
    }

    @MiraiExperimentalAPI
    override suspend fun queryRemark(): FriendNameRemark {
        TODO("not implemented")
    }

    override fun toString(): String = "QQ($id)"
}


@Suppress("MemberVisibilityCanBePrivate")
internal class MemberImpl(
    qq: QQImpl,
    group: GroupImpl,
    override val coroutineContext: CoroutineContext,
    memberInfo: MemberInfo
) : ContactImpl(), Member, QQ by qq {
    override val group: GroupImpl by group.unsafeWeakRef()
    val qq: QQImpl by qq.unsafeWeakRef()

    override var permission: MemberPermission = memberInfo.permission
    @Suppress("PropertyName")
    internal var _nameCard: String = memberInfo.nameCard
    @Suppress("PropertyName")
    internal var _specialTitle: String = memberInfo.specialTitle

    @Suppress("PropertyName")
    var _muteTimestamp: Int = memberInfo.muteTimestamp

    override val muteTimeRemaining: Int =
        if (_muteTimestamp == 0 || _muteTimestamp == 0xFFFFFFFF.toInt()) {
            0
        } else {
            _muteTimestamp - currentTimeSeconds.toInt() - bot.client.timeDifference.toInt()
        }

    override var nameCard: String
        get() = _nameCard
        set(newValue) {
            group.checkBotPermissionOperator()
            if (_nameCard != newValue) {
                val oldValue = _nameCard
                _nameCard = newValue
                launch {
                    bot.network.run {
                        TroopManagement.EditGroupNametag(
                            bot.client,
                            this@MemberImpl,
                            newValue
                        ).sendWithoutExpect()
                    }
                    MemberCardChangeEvent(oldValue, newValue, this@MemberImpl, null).broadcast()
                }
            }
        }

    override var specialTitle: String
        get() = _specialTitle
        set(newValue) {
            group.checkBotPermission(MemberPermission.OWNER)
            if (_specialTitle != newValue) {
                val oldValue = _specialTitle
                _specialTitle = newValue
                launch {
                    bot.network.run {
                        TroopManagement.EditSpecialTitle(
                            bot.client,
                            this@MemberImpl,
                            newValue
                        ).sendWithoutExpect()
                    }
                    MemberSpecialTitleChangeEvent(oldValue, newValue, this@MemberImpl, null).broadcast()
                }
            }
        }

    override val bot: QQAndroidBot get() = qq.bot

    override suspend fun mute(durationSeconds: Int) {
        if (group.botPermission != MemberPermission.OWNER && (!group.botPermission.isOperator() || this.isOperator())) {
            throw PermissionDeniedException()
        }

        bot.network.run {
            TroopManagement.Mute(
                client = bot.client,
                groupCode = group.id,
                memberUin = this@MemberImpl.id,
                timeInSecond = durationSeconds
            ).sendAndExpect<TroopManagement.Mute.Response>()
        }

        @Suppress("RemoveRedundantQualifierName") // or unresolved reference
        net.mamoe.mirai.event.events.MemberMuteEvent(this@MemberImpl, durationSeconds, null).broadcast()
    }

    override suspend fun unmute() {
        if (group.botPermission != MemberPermission.OWNER && (!group.botPermission.isOperator() || this.isOperator())) {
            throw PermissionDeniedException()
        }

        bot.network.run {
            TroopManagement.Mute(
                client = bot.client,
                groupCode = group.id,
                memberUin = this@MemberImpl.id,
                timeInSecond = 0
            ).sendAndExpect<TroopManagement.Mute.Response>()
        }

        @Suppress("RemoveRedundantQualifierName") // or unresolved reference
        net.mamoe.mirai.event.events.MemberUnmuteEvent(this@MemberImpl, null).broadcast()
    }

    override suspend fun kick(message: String) {
        if (group.botPermission != MemberPermission.OWNER && (!group.botPermission.isOperator() || this.isOperator())) {
            throw PermissionDeniedException()
        }

        bot.network.run {
            TroopManagement.Kick(
                client = bot.client,
                member = this@MemberImpl,
                message = message
            ).sendAndExpect<TroopManagement.Kick.Response>().success.also {
                MemberLeaveEvent.Kick(this@MemberImpl, null).broadcast()
            }
        }
    }

    override fun hashCode(): Int {
        var result = bot.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean { // 不要删除. trust me
        if (this === other) return true
        if (other !is Contact) return false
        if (this::class != other::class) return false
        return this.id == other.id && this.bot == other.bot
    }

    override fun toString(): String {
        return "Member($id)"
    }
}

internal class MemberInfoImpl(
    jceInfo: StTroopMemberInfo,
    groupOwnerId: Long
) : MemberInfo {
    override val uin: Long = jceInfo.memberUin
    override val nameCard: String = jceInfo.sName ?: ""
    override val nick: String = jceInfo.nick
    override val permission: MemberPermission = when {
        jceInfo.memberUin == groupOwnerId -> MemberPermission.OWNER
        jceInfo.dwFlag == 1L -> MemberPermission.ADMINISTRATOR
        else -> MemberPermission.MEMBER
    }
    override val specialTitle: String = jceInfo.sSpecialTitle ?: ""
    override val muteTimestamp: Int = jceInfo.dwShutupTimestap?.toInt() ?: 0
}

/**
 * 对GroupImpl
 * 中name/announcement的更改会直接向服务器异步汇报
 */
@Suppress("PropertyName")
@UseExperimental(MiraiInternalAPI::class)
internal class GroupImpl(
    bot: QQAndroidBot, override val coroutineContext: CoroutineContext,
    override val id: Long,
    groupInfo: GroupInfo,
    members: Sequence<MemberInfo>
) : ContactImpl(), Group {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    val uin: Long = groupInfo.uin

    override lateinit var owner: Member

    @UseExperimental(MiraiExperimentalAPI::class)
    override val botAsMember: Member by lazy {
        Member(object : MemberInfo {
            override val nameCard: String
                get() = bot.nick // TODO: 2020/2/21 机器人在群内的昵称获取
            override val permission: MemberPermission
                get() = botPermission
            override val specialTitle: String
                get() = "" // TODO: 2020/2/21 获取机器人在群里的头衔
            override val muteTimestamp: Int
                get() = botMuteRemaining
            override val uin: Long
                get() = bot.uin
            override val nick: String
                get() = bot.nick
        })
    }

    @UseExperimental(MiraiExperimentalAPI::class)
    override lateinit var botPermission: MemberPermission

    var _botMuteTimestamp: Int = groupInfo.botMuteRemaining

    override val botMuteRemaining: Int =
        if (_botMuteTimestamp == 0 || _botMuteTimestamp == 0xFFFFFFFF.toInt()) {
            0
        } else {
            _botMuteTimestamp - currentTimeSeconds.toInt() - bot.client.timeDifference.toInt()
        }

    override val members: ContactList<Member> = ContactList(members.mapNotNull {
        if (it.uin == bot.uin) {
            botPermission = it.permission
            if (it.permission == MemberPermission.OWNER) {
                owner = botAsMember
            }
            null
        } else Member(it).also { member ->
            if (member.permission == MemberPermission.OWNER) {
                owner = member
            }
        }
    }.toLockFreeLinkedList())

    internal var _name: String = groupInfo.name
    internal var _announcement: String = groupInfo.memo
    internal var _allowMemberInvite: Boolean = groupInfo.allowMemberInvite
    internal var _confessTalk: Boolean = groupInfo.confessTalk
    internal var _muteAll: Boolean = groupInfo.muteAll
    internal var _autoApprove: Boolean = groupInfo.autoApprove
    internal var _anonymousChat: Boolean = groupInfo.allowAnonymousChat

    override var name: String
        get() = _name
        set(newValue) {
            this.checkBotPermissionOperator()
            if (_name != newValue) {
                val oldValue = _name
                _name = newValue
                launch {
                    bot.network.run {
                        TroopManagement.GroupOperation.name(
                            client = bot.client,
                            groupCode = id,
                            newName = newValue
                        ).sendWithoutExpect()
                    }
                    GroupNameChangeEvent(oldValue, newValue, this@GroupImpl, true).broadcast()
                }
            }
        }

    override var entranceAnnouncement: String
        get() = _announcement
        set(newValue) {
            this.checkBotPermissionOperator()
            if (_announcement != newValue) {
                val oldValue = _announcement
                _announcement = newValue
                launch {
                    bot.network.run {
                        TroopManagement.GroupOperation.memo(
                            client = bot.client,
                            groupCode = id,
                            newMemo = newValue
                        ).sendWithoutExpect()
                    }
                    GroupEntranceAnnouncementChangeEvent(oldValue, newValue, this@GroupImpl, null).broadcast()
                }
            }
        }


    override var isAllowMemberInvite: Boolean
        get() = _allowMemberInvite
        set(newValue) {
            this.checkBotPermissionOperator()
            if (_allowMemberInvite != newValue) {
                val oldValue = _allowMemberInvite
                _allowMemberInvite = newValue
                launch {
                    bot.network.run {
                        TroopManagement.GroupOperation.allowMemberInvite(
                            client = bot.client,
                            groupCode = id,
                            switch = newValue
                        ).sendWithoutExpect()
                    }
                    GroupAllowMemberInviteEvent(oldValue, newValue, this@GroupImpl, null).broadcast()
                }
            }
        }

    override var isAutoApproveEnabled: Boolean
        get() = _autoApprove
        set(newValue) {
            TODO()
        }

    override var isAnonymousChatEnabled: Boolean
        get() = _anonymousChat
        set(newValue) {
            TODO()
        }

    override var isConfessTalkEnabled: Boolean
        get() = _confessTalk
        set(newValue) {
            this.checkBotPermissionOperator()
            if (_confessTalk != newValue) {
                val oldValue = _confessTalk
                _confessTalk = newValue
                launch {
                    bot.network.run {
                        TroopManagement.GroupOperation.confessTalk(
                            client = bot.client,
                            groupCode = id,
                            switch = newValue
                        ).sendWithoutExpect()
                    }
                    GroupAllowConfessTalkEvent(oldValue, newValue, this@GroupImpl, true).broadcast()
                }
            }
        }


    override var isMuteAll: Boolean
        get() = _muteAll
        set(newValue) {
            this.checkBotPermissionOperator()
            if (_muteAll != newValue) {
                val oldValue = _muteAll
                _muteAll = newValue
                launch {
                    bot.network.run {
                        TroopManagement.GroupOperation.muteAll(
                            client = bot.client,
                            groupCode = id,
                            switch = newValue
                        ).sendWithoutExpect()
                    }
                    GroupMuteAllEvent(oldValue, newValue, this@GroupImpl, null).broadcast()
                }
            }
        }

    @MiraiExperimentalAPI
    override suspend fun quit(): Boolean {
        check(botPermission != MemberPermission.OWNER) { "An owner cannot quit from a owning group" }
        TODO("not implemented")
    }

    @UseExperimental(MiraiExperimentalAPI::class)
    override fun Member(memberInfo: MemberInfo): Member {
        return MemberImpl(
            bot.QQ(memberInfo) as QQImpl,
            this,
            this.coroutineContext,
            memberInfo
        )
    }


    override operator fun get(id: Long): Member {
        return members.delegate.filteringGetOrNull { it.id == id } ?: throw NoSuchElementException("member $id not found in group $uin")
    }

    override fun contains(id: Long): Boolean {
        return members.delegate.filteringGetOrNull { it.id == id } != null
    }

    override fun getOrNull(id: Long): Member? {
        return members.delegate.filteringGetOrNull { it.id == id }
    }

    override suspend fun sendMessage(message: MessageChain): MessageReceipt<Group> {
        check(!isBotMuted) { "bot is muted. Remaining seconds=$botMuteRemaining" }
        val event = GroupMessageSendEvent(this, message).broadcast()
        if (event.isCancelled) {
            throw EventCancelledException("cancelled by FriendMessageSendEvent")
        }
        lateinit var source: MessageSvc.PbSendMsg.MessageSourceFromSend
        bot.network.run {
            val response: MessageSvc.PbSendMsg.Response = MessageSvc.PbSendMsg.ToGroup(
                bot.client,
                id,
                event.message
            ) { source = it }.sendAndExpect()
            check(
                response is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed: $response" }
        }

        source.startWaitingSequenceId(this)

        return MessageReceipt(message, source, this)
    }

    override suspend fun uploadImage(image: ExternalImage): Image = try {
        if (BeforeImageUploadEvent(this, image).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        bot.network.run {
            val response: ImgStore.GroupPicUp.Response = ImgStore.GroupPicUp(
                bot.client,
                uin = bot.uin,
                groupCode = id,
                md5 = image.md5,
                size = image.inputSize,
                picWidth = image.width,
                picHeight = image.height,
                picType = image.imageType,
                filename = image.filename
            ).sendAndExpect()

            when (response) {
                is ImgStore.GroupPicUp.Response.Failed -> {
                    ImageUploadEvent.Failed(this@GroupImpl, image, response.resultCode, response.message).broadcast()
                    error("upload group image failed with reason ${response.message}")
                }
                is ImgStore.GroupPicUp.Response.FileExists -> {
                    val resourceId = image.calculateImageResourceId()
//                    return NotOnlineImageFromFile(
//                        resourceId = resourceId,
//                        md5 = response.fileInfo.fileMd5,
//                        filepath = resourceId,
//                        fileLength = response.fileInfo.fileSize.toInt(),
//                        height = response.fileInfo.fileHeight,
//                        width = response.fileInfo.fileWidth,
//                        imageType = response.fileInfo.fileType,
//                        fileId = response.fileId.toInt()
//                    )
                    //  println("NMSL")
                    return CustomFaceFromFile(
                        md5 = image.md5,
                        filepath = resourceId
                    ).also { ImageUploadEvent.Succeed(this@GroupImpl, image, it).broadcast() }
                }
                is ImgStore.GroupPicUp.Response.RequireUpload -> {
                    HighwayHelper.uploadImage(
                        client = bot.client,
                        serverIp = response.uploadIpList.first().toIpV4AddressString(),
                        serverPort = response.uploadPortList.first(),
                        imageInput = image.input,
                        inputSize = image.inputSize.toInt(),
                        md5 = image.md5,
                        uKey = response.uKey,
                        commandId = 2
                    )
                    val resourceId = image.calculateImageResourceId()
                    // return NotOnlineImageFromFile(
                    //     resourceId = resourceId,
                    //     md5 = image.md5,
                    //     filepath = resourceId,
                    //     fileLength = image.inputSize.toInt(),
                    //     height = image.height,
                    //     width = image.width,
                    //     imageType = image.imageType,
                    //     fileId = response.fileId.toInt()
                    // )
                    return CustomFaceFromFile(
                        md5 = image.md5,
                        filepath = resourceId
                    ).also { ImageUploadEvent.Succeed(this@GroupImpl, image, it).broadcast() }
                    /*
                        fileId = response.fileId.toInt(),
                        fileType = 0, // ?
                        height = image.height,
                        width = image.width,
                        imageType = image.imageType,
                        bizType = 0,
                        serverIp = response.uploadIpList.first(),
                        serverPort = response.uploadPortList.first(),
                        signature = image.md5,
                        size = image.inputSize.toInt(),
                        useful = 1,
                        source = 200,
                        original = 1,
                        pbReserve = EMPTY_BYTE_ARRAY
                     */
                }
            }
        }
    } finally {
        image.input.close()
    }

    override fun toString(): String {
        return "Group($id)"
    }
}