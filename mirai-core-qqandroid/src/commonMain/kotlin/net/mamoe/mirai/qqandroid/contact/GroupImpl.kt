/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(LowLevelAPI::class)

package net.mamoe.mirai.qqandroid.contact

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.io.core.Closeable
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceToGroupImpl
import net.mamoe.mirai.qqandroid.message.ensureSequenceIdAvailable
import net.mamoe.mirai.qqandroid.message.firstIsInstanceOrNull
import net.mamoe.mirai.qqandroid.network.highway.HighwayHelper
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.createToGroup
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.ProfileService
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.qqandroid.utils.estimateLength
import net.mamoe.mirai.qqandroid.utils.toUHexString
import net.mamoe.mirai.utils.*
import java.io.InputStream
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic
import kotlin.time.ExperimentalTime

internal fun GroupImpl.Companion.checkIsInstance(instance: Group) {
    contract {
        returns() implies (instance is GroupImpl)
    }
    check(instance is GroupImpl) { "group is not an instanceof GroupImpl!! DO NOT interlace two or more protocol implementations!!" }
}

internal fun Group.checkIsGroupImpl() {
    contract {
        returns() implies (this@checkIsGroupImpl is GroupImpl)
    }
    GroupImpl.checkIsInstance(this)
}

@Suppress("PropertyName")
internal class GroupImpl(
    bot: QQAndroidBot,
    coroutineContext: CoroutineContext,
    override val id: Long,
    groupInfo: GroupInfo,
    members: Sequence<MemberInfo>
) : Group() {
    companion object;

    override val coroutineContext: CoroutineContext = coroutineContext + SupervisorJob(coroutineContext[Job])

    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    val uin: Long = groupInfo.uin

    override lateinit var owner: Member

    override lateinit var botAsMember: Member

    override val botPermission: MemberPermission get() = botAsMember.permission

    // e.g. 600
    override val botMuteRemaining: Int get() = botAsMember.muteTimeRemaining

    override val members: ContactList<Member> = ContactList(members.mapNotNull {
        if (it.uin == bot.id) {
            botAsMember = newMember(it)
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

            checkBotPermission(MemberPermission.ADMINISTRATOR)
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
                    GroupNameChangeEvent(oldValue, newValue, this@GroupImpl, null).broadcast()
                }
            }
        }

    override val settings: GroupSettings = object : GroupSettings {

        override var entranceAnnouncement: String
            get() = _announcement
            set(newValue) {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                //if (_announcement != newValue) {
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
                //}
            }


        override var isAllowMemberInvite: Boolean
            get() = _allowMemberInvite
            set(newValue) {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                //if (_allowMemberInvite != newValue) {
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
                //}
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

        @Suppress("OverridingDeprecatedMember")
        override var isConfessTalkEnabled: Boolean
            get() = _confessTalk
            set(newValue) {

                checkBotPermission(MemberPermission.ADMINISTRATOR)
                //if (_confessTalk != newValue) {
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
                // }
            }


        override var isMuteAll: Boolean
            get() = _muteAll
            set(newValue) {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                //if (_muteAll != newValue) {
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
                //}
            }
    }

    override suspend fun quit(): Boolean {
        check(botPermission != MemberPermission.OWNER) { "An owner cannot quit from a owning group" }

        if (!bot.groups.delegate.remove(this)) {
            return false
        }
        bot.network.run {
            val response: ProfileService.GroupMngReq.GroupMngReqResponse = ProfileService.GroupMngReq(
                bot.client,
                this@GroupImpl.id
            ).sendAndExpect()
            check(response.errorCode == 0) {
                "Group.quit failed: $response".also {
                    bot.groups.delegate.addLast(this@GroupImpl)
                }
            }
        }
        BotLeaveEvent.Active(this).broadcast()
        return true
    }

    override fun newMember(memberInfo: MemberInfo): Member {
        return MemberImpl(
            bot._lowLevelNewFriend(memberInfo) as FriendImpl,
            this,
            this.coroutineContext,
            memberInfo
        )
    }

    internal fun newAnonymous(name: String): Member = newMember(
        object : MemberInfo {
            override val nameCard = name
            override val permission = MemberPermission.MEMBER
            override val specialTitle = "匿名"
            override val muteTimestamp = 0
            override val uin = 80000000L
            override val nick = name
        }
    )

    override operator fun get(id: Long): Member {
        if (id == bot.id) {
            return botAsMember
        }
        return members.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("member $id not found in group $uin")
    }

    override fun contains(id: Long): Boolean {
        return bot.id == id || members.firstOrNull { it.id == id } != null
    }

    override fun getOrNull(id: Long): Member? {
        if (id == bot.id) {
            return botAsMember
        }
        return members.firstOrNull { it.id == id }
    }

    @JvmSynthetic
    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        require(message.isContentNotEmpty()) { "message is empty" }
        check(!isBotMuted) { throw BotIsBeingMutedException(this) }

        return sendMessageImpl(message, false).also {
            logMessageSent(message)
        }
    }

    private suspend fun sendMessageImpl(message: Message, isForward: Boolean): MessageReceipt<Group> {
        if (message is MessageChain) {
            if (message.anyIsInstance<ForwardMessage>()) {
                return sendMessageImpl(message.singleOrNull() ?: error("ForwardMessage must be standalone"), true)
            }
        }
        if (message is ForwardMessage) {
            check(message.nodeList.size < 200) {
                throw MessageTooLargeException(
                    this, message, message,
                    "ForwardMessage allows up to 200 nodes, but found ${message.nodeList.size}"
                )
            }

            return bot.lowLevelSendGroupLongOrForwardMessage(this.id, message.nodeList, false, message)
        }

        val msg: MessageChain = if (message !is LongMessage && message !is ForwardMessageInternal) {
            val chain = kotlin.runCatching {
                GroupMessagePreSendEvent(this, message).broadcast()
            }.onSuccess {
                check(!it.isCancelled) {
                    throw EventCancelledException("cancelled by GroupMessagePreSendEvent")
                }
            }.getOrElse {
                throw EventCancelledException("exception thrown when broadcasting GroupMessagePreSendEvent", it)
            }.message.asMessageChain()

            val length = chain.estimateLength(703) // 阈值为700左右，限制到3的倍数
            var imageCnt = 0 // 通过下方逻辑短路延迟计算

            if (length > 5000 || chain.count { it is Image }.apply { imageCnt = this } > 50) {
                throw MessageTooLargeException(
                    this, message, chain,
                    "message(${chain.joinToString("", limit = 10)}) is too large. Allow up to 50 images or 5000 chars"
                )
            }

            if (length > 702 || imageCnt > 2) {
                return bot.lowLevelSendGroupLongOrForwardMessage(
                    this.id,
                    listOf(
                        ForwardMessage.Node(
                            senderId = bot.id,
                            time = currentTimeSeconds.toInt(),
                            message = chain,
                            senderName = bot.nick
                        )
                    ),
                    true, null
                )
            }
            chain
        } else message.asMessageChain()

        msg.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()


        val result = bot.network.runCatching {
            val source: MessageSourceToGroupImpl
            MessageSvcPbSendMsg.createToGroup(
                bot.client,
                this@GroupImpl,
                msg,
                isForward
            ) {
                source = it
            }.sendAndExpect<MessageSvcPbSendMsg.Response>().let {
                check(it is MessageSvcPbSendMsg.Response.SUCCESS) {
                    "Send group message failed: $it"
                }
            }

            try {
                source.ensureSequenceIdAvailable()
            } catch (e: Exception) {
                bot.network.logger.warning {
                    "Timeout awaiting sequenceId for group message(${
                        message.contentToString()
                            .take(10)
                    }). Some features may not work properly"
                }
                bot.network.logger.warning(e)
            }

            MessageReceipt(source, this@GroupImpl, botAsMember)
        }

        result.fold(
            onSuccess = {
                GroupMessagePostSendEvent(this, msg, null, it)
            },
            onFailure = {
                GroupMessagePostSendEvent(this, msg, it, null)
            }
        ).broadcast()

        return result.getOrThrow()
    }

    @Suppress("DEPRECATION")
    @OptIn(ExperimentalTime::class)
    @JvmSynthetic
    override suspend fun uploadImage(image: ExternalImage): OfflineGroupImage = try {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        if (image.input is net.mamoe.mirai.utils.internal.DeferredReusableInput) {
            image.input.init(bot.configuration.fileCacheStrategy)
        }
        if (BeforeImageUploadEvent(this, image).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        bot.network.run {
            val response: ImgStore.GroupPicUp.Response = ImgStore.GroupPicUp(
                bot.client,
                uin = bot.id,
                groupCode = id,
                md5 = image.md5,
                size = image.input.size.toInt()
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
                    return OfflineGroupImage(imageId = resourceId)
                        .also { ImageUploadEvent.Succeed(this@GroupImpl, image, it).broadcast() }
                }
                is ImgStore.GroupPicUp.Response.RequireUpload -> {
                    HighwayHelper.uploadImageToServers(
                        bot,
                        response.uploadIpList.zip(response.uploadPortList),
                        response.uKey,
                        image.input,
                        kind = "group image",
                        commandId = 2
                    )
                    val resourceId = image.calculateImageResourceId()
                    return OfflineGroupImage(imageId = resourceId)
                        .also { ImageUploadEvent.Succeed(this@GroupImpl, image, it).broadcast() }
                }
            }
        }
    } finally {
        (image.input as? Closeable)?.close()
    }

    /**
     * 上传一个语音消息以备发送.
     * 请注意，这是一个实验性api且随时会被删除
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws OverFileSizeMaxException 当语音文件过大而被服务器拒绝上传时. (最大大小约为 1 MB)
     */
    @MiraiExperimentalAPI
    @SinceMirai("1.2.0")
    override suspend fun uploadVoice(input: InputStream): Voice {
        val content = ByteArray(input.available())
        input.read(content)
        if (content.size > 1048576) {
            throw  OverFileSizeMaxException()
        }
        val md5 = MiraiPlatformUtils.md5(content)
        val codec = with(content.copyOfRange(0, 10).toUHexString("")) {
            when {
                startsWith("2321414D52") -> 0             // amr
                startsWith("02232153494C4B5F5633") -> 1  // silk V3
                else -> 0                               // use amr by default
            }
        }
        return bot.network.run {
            val response: PttStore.GroupPttUp.Response.RequireUpload =
                PttStore.GroupPttUp(bot.client, bot.id, id, md5, content.size.toLong(), codec).sendAndExpect()
            HighwayHelper.uploadPttToServers(
                bot,
                response.uploadIpList.zip(response.uploadPortList),
                content,
                md5,
                response.uKey,
                response.fileKey,
                codec
            )
            Voice("${md5.toUHexString("")}.amr", md5, content.size.toLong(), "")
        }

    }


    override fun toString(): String = "Group($id)"
}
