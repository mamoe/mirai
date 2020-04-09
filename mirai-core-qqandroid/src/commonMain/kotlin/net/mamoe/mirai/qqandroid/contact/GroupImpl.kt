/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR")
@file:OptIn(MiraiInternalAPI::class, LowLevelAPI::class)

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
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceToGroupImpl
import net.mamoe.mirai.qqandroid.message.ensureSequenceIdAvailable
import net.mamoe.mirai.qqandroid.message.firstIsInstanceOrNull
import net.mamoe.mirai.qqandroid.network.highway.HighwayHelper
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.qqandroid.utils.estimateLength
import net.mamoe.mirai.qqandroid.utils.toIpV4AddressString
import net.mamoe.mirai.utils.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

@OptIn(ExperimentalContracts::class)
internal fun GroupImpl.Companion.checkIsInstance(instance: Group) {
    contract {
        returns() implies (instance is GroupImpl)
    }
    check(instance is GroupImpl) { "group is not an instanceof GroupImpl!! DO NOT interlace two or more protocol implementations!!" }
}

@OptIn(ExperimentalContracts::class)
internal fun Group.checkIsGroupImpl() {
    contract {
        returns() implies (this@checkIsGroupImpl is GroupImpl)
    }
    GroupImpl.checkIsInstance(this)
}

@Suppress("PropertyName")
internal class GroupImpl(
    bot: QQAndroidBot, override val coroutineContext: CoroutineContext,
    override val id: Long,
    groupInfo: GroupInfo,
    members: Sequence<MemberInfo>
) : Group() {
    companion object;

    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    @OptIn(LowLevelAPI::class)
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
                get() = bot.id
            override val nick: String
                get() = bot.nick
        })
    }

    @OptIn(MiraiExperimentalAPI::class)
    override lateinit var botPermission: MemberPermission

    var _botMuteTimestamp: Int = groupInfo.botMuteTimestamp

    override val botMuteRemaining: Int =
        if (_botMuteTimestamp == 0 || _botMuteTimestamp == 0xFFFFFFFF.toInt()) {
            0
        } else {
            _botMuteTimestamp - currentTimeSeconds.toInt() - bot.client.timeDifference.toInt()
        }

    override val members: ContactList<Member> = ContactList(members.mapNotNull {
        if (it.uin == bot.id) {
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

    override val settings: GroupSettings = object : GroupSettings {

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
        return members.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("member $id not found in group $uin")
    }

    override fun contains(id: Long): Boolean {
        return members.firstOrNull { it.id == id } != null
    }

    override fun getOrNull(id: Long): Member? {
        return members.firstOrNull { it.id == id }
    }

    @OptIn(MiraiExperimentalAPI::class, LowLevelAPI::class)
    @JvmSynthetic
    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        check(!isBotMuted) { throw BotIsBeingMutedException(this) }

        return sendMessageImpl(message).also {
            logMessageSent(message)
        }
    }

    @OptIn(MiraiExperimentalAPI::class)
    private suspend fun sendMessageImpl(message: Message): MessageReceipt<Group> {

        val msg: MessageChain

        if (message !is LongMessage) {
            val event = GroupMessageSendEvent(this, message.asMessageChain()).broadcast()
            if (event.isCancelled) {
                throw EventCancelledException("cancelled by GroupMessageSendEvent")
            }

            val length = event.message.estimateLength(703) // 阈值为700左右，限制到3的倍数
            var imageCnt = 0 // 通过下方逻辑短路延迟计算

            if (length > 5000 || event.message.count { it is Image }.apply { imageCnt = this } > 50) {
                throw MessageTooLargeException(
                    this,
                    message,
                    event.message,
                    "message(${event.message.joinToString(
                        "",
                        limit = 10
                    )}) is too large. Allow up to 50 images or 5000 chars"
                )
            }

            if (length > 702 || imageCnt > 2)
                return bot.lowLevelSendLongGroupMessage(this.id, event.message)

            msg = event.message
        } else msg = message.asMessageChain()
        msg.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()

        lateinit var source: MessageSourceToGroupImpl
        bot.network.run {
            val response: MessageSvc.PbSendMsg.Response = MessageSvc.PbSendMsg.createToGroup(
                bot.client,
                this@GroupImpl,
                msg
            ) {
                source = it
                source.startWaitingSequenceId(this)
            }.sendAndExpect()
            if (response is MessageSvc.PbSendMsg.Response.Failed) {
                when (response.resultType) {
                    120 -> error("bot is being muted.")
                    34 -> {
                        kotlin.runCatching { // allow retry once
                            return bot.lowLevelSendLongGroupMessage(id, msg)
                        }.getOrElse {
                            throw IllegalStateException("internal error: send message failed(34)", it)
                        }
                    }
                    else -> error("send message failed: $response")
                }
            }
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
                uin = bot.id,
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
                    // 每 10KB 等 1 秒, 最少等待 5 秒
                    val success = response.uploadIpList.zip(response.uploadPortList).any { (ip, port) ->
                        withTimeoutOrNull((image.inputSize * 1000 / 1024 / 10).coerceAtLeast(5000)) {
                            bot.network.logger.verbose { "[Highway] Uploading group image to ${ip.toIpV4AddressString()}:$port: size=${image.inputSize / 1024} KiB" }
                            HighwayHelper.uploadImage(
                                client = bot.client,
                                serverIp = ip.toIpV4AddressString(),
                                serverPort = port,
                                imageInput = image.input,
                                inputSize = image.inputSize.toInt(),
                                fileMd5 = image.md5,
                                ticket = response.uKey,
                                commandId = 2
                            )
                            bot.network.logger.verbose { "[Highway] Uploading group image: succeed" }
                            true
                        } ?: kotlin.run {
                            bot.network.logger.verbose { "[Highway] Uploading group image: timeout, retrying next server" }
                            false
                        }
                    }

                    check(success) { "cannot upload group image, failed on all servers." }

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