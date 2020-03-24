/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file: Suppress("INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.qqandroid.contact

import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.core.Closeable
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineGroupImage
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceFromSendGroup
import net.mamoe.mirai.qqandroid.network.highway.HighwayHelper
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.qqandroid.utils.toIpV4AddressString
import net.mamoe.mirai.utils.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

@OptIn(ExperimentalContracts::class)
internal fun GroupImpl.Companion.checkIsInstance(expression: Boolean) {
    contract {
        returns() implies expression
    }
    check(expression) { "group is not an instanceof GroupImpl!! DO NOT interlace two or more protocol implementations!!" }
}

@Suppress("PropertyName")
@OptIn(MiraiInternalAPI::class)
internal class GroupImpl(
    bot: QQAndroidBot, override val coroutineContext: CoroutineContext,
    override val id: Long,
    groupInfo: GroupInfo,
    members: Sequence<MemberInfo>
) : Group() {
    companion object;

    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    val uin: Long = groupInfo.uin

    override lateinit var owner: Member

    @OptIn(MiraiExperimentalAPI::class)
    override val botAsMember: Member by lazy {
        newMember(object : MemberInfo {
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

    @OptIn(MiraiExperimentalAPI::class)
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
        } else newMember(it).also { member ->
            if (member.permission == MemberPermission.OWNER) {
                owner = member
            }
        }
    }.toLockFreeLinkedList())

    internal var _name: String = groupInfo.name
    private var _announcement: String = groupInfo.memo
    private var _allowMemberInvite: Boolean = groupInfo.allowMemberInvite
    internal var _confessTalk: Boolean = groupInfo.confessTalk
    internal var _muteAll: Boolean = groupInfo.muteAll
    private var _autoApprove: Boolean = groupInfo.autoApprove
    internal var _anonymousChat: Boolean = groupInfo.allowAnonymousChat

    override var name: String
        get() = _name
        set(newValue) {
            checkBotPermissionOperator()
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

    override val settings: GroupSettings = object : GroupSettings{

        override var entranceAnnouncement: String
            get() = _announcement
            set(newValue) {
                checkBotPermissionOperator()
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
                checkBotPermissionOperator()
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
            @Suppress("UNUSED_PARAMETER")
            set(newValue) {
                TODO()
            }

        override var isAnonymousChatEnabled: Boolean
            get() = _anonymousChat
            @Suppress("UNUSED_PARAMETER")
            set(newValue) {
                TODO()
            }

        override var isConfessTalkEnabled: Boolean
            get() = _confessTalk
            set(newValue) {
                checkBotPermissionOperator()
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
                checkBotPermissionOperator()
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
    }

    @MiraiExperimentalAPI
    override suspend fun quit(): Boolean {
        check(botPermission != MemberPermission.OWNER) { "An owner cannot quit from a owning group" }
        TODO("not implemented")
    }

    @OptIn(MiraiExperimentalAPI::class)
    override fun newMember(memberInfo: MemberInfo): Member {
        return MemberImpl(
            @OptIn(LowLevelAPI::class)
            bot._lowLevelNewQQ(memberInfo) as QQImpl,
            this,
            this.coroutineContext,
            memberInfo
        )
    }


    override operator fun get(id: Long): Member {
        return members.delegate.filteringGetOrNull { it.id == id }
            ?: throw NoSuchElementException("member $id not found in group $uin")
    }

    override fun contains(id: Long): Boolean {
        return members.delegate.filteringGetOrNull { it.id == id } != null
    }

    override fun getOrNull(id: Long): Member? {
        return members.delegate.filteringGetOrNull { it.id == id }
    }

    @JvmSynthetic
    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        check(!isBotMuted) { "bot is muted. Remaining seconds=$botMuteRemaining" }
        val event = GroupMessageSendEvent(this, message.asMessageChain()).broadcast()
        if (event.isCancelled) {
            throw EventCancelledException("cancelled by FriendMessageSendEvent")
        }
        lateinit var source: MessageSourceFromSendGroup
        bot.network.run {
            val response: MessageSvc.PbSendMsg.Response = MessageSvc.PbSendMsg.ToGroup(
                bot.client,
                id,
                event.message
            ) {
                source = it
                source.startWaitingSequenceId(this)
            }.sendAndExpect()
            check(
                response is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed: $response" }
        }

        return MessageReceipt(source, this, botAsMember)
    }

    @JvmSynthetic
    override suspend fun uploadImage(image: ExternalImage): OfflineGroupImage = try {
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

            @Suppress("UNCHECKED_CAST") // bug
            when (response) {
                is ImgStore.GroupPicUp.Response.Failed -> {
                    ImageUploadEvent.Failed(this@GroupImpl, image, response.resultCode, response.message).broadcast()
                    if (response.message == "over file size max") throw OverFileSizeMaxException()
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
                    return OfflineGroupImage(
                        md5 = image.md5,
                        filepath = resourceId
                    ).also { ImageUploadEvent.Succeed(this@GroupImpl, image, it).broadcast() }
                }
                is ImgStore.GroupPicUp.Response.RequireUpload -> {
                    // 每 10KB 等 1 秒
                    withTimeoutOrNull(image.inputSize * 1000 / 1024 / 10) {
                        HighwayHelper.uploadImage(
                            client = bot.client,
                            serverIp = response.uploadIpList.first().toIpV4AddressString(),
                            serverPort = response.uploadPortList.first(),
                            imageInput = image.input,
                            inputSize = image.inputSize.toInt(),
                            fileMd5 = image.md5,
                            uKey = response.uKey,
                            commandId = 2
                        )
                    } ?: error("timeout uploading image: ${image.filename}")
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
                    return OfflineGroupImage(
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
        (image.input as? Closeable)?.close()
    }

    override fun toString(): String {
        return "Group($id)"
    }

    override fun hashCode(): Int {
        var result = bot.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        @Suppress("DuplicatedCode", "DuplicatedCode")
        if (this === other) return true
        if (other !is Contact) return false
        if (this::class != other::class) return false
        return this.id == other.id && this.bot == other.bot
    }


}