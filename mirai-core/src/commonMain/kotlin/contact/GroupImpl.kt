/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(LowLevelApi::class)

package net.mamoe.mirai.internal.contact

import kotlinx.atomicfu.atomic
import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.announcement.AnnouncementsImpl
import net.mamoe.mirai.internal.contact.file.RemoteFilesImpl
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.message.data.OfflineAudioImpl
import net.mamoe.mirai.internal.message.image.OfflineGroupImage
import net.mamoe.mirai.internal.message.image.calculateImageInfo
import net.mamoe.mirai.internal.message.image.getIdByImageType
import net.mamoe.mirai.internal.message.image.getImageTypeById
import net.mamoe.mirai.internal.message.protocol.outgoing.GroupMessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.network.components.BdhSession
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind.GROUP_AUDIO
import net.mamoe.mirai.internal.network.highway.ResourceKind.GROUP_IMAGE
import net.mamoe.mirai.internal.network.highway.postPtt
import net.mamoe.mirai.internal.network.highway.tryServersUpload
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopEssenceMsgManager
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.audioCodec
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.voiceCodec
import net.mamoe.mirai.internal.network.protocol.packet.list.ProfileService
import net.mamoe.mirai.internal.utils.GroupPkgMsgParsingCache
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.internal.utils.RemoteFileImpl
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.spi.AudioToSilkService
import net.mamoe.mirai.utils.*
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal fun GroupImpl.Companion.checkIsInstance(instance: Group) {
    contract { returns() implies (instance is GroupImpl) }
    check(instance is GroupImpl) { "group is not an instanceof GroupImpl!! DO NOT interlace two or more protocol implementations!!" }
}

internal fun Group.checkIsGroupImpl(): GroupImpl {
    contract { returns() implies (this@checkIsGroupImpl is GroupImpl) }
    GroupImpl.checkIsInstance(this)
    return this
}

internal fun GroupImpl(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    id: Long,
    groupInfo: GroupInfo,
    members: Sequence<MemberInfo>,
): GroupImpl {
    return GroupImpl(bot, parentCoroutineContext, id, groupInfo, ContactList(ConcurrentLinkedDeque())).apply Group@{
        members.forEach { info ->
            if (info.uin == bot.id) {
                botAsMember = newNormalMember(info)
                if (info.permission == MemberPermission.OWNER) {
                    owner = botAsMember
                }
            } else newNormalMember(info).let { member ->
                if (member.permission == MemberPermission.OWNER) {
                    owner = member
                }
                this@Group.members.delegate.add(member)
            }
        }
    }.apply {
        if (!botAsMemberInitialized) {
            logger.error(
                contextualBugReportException("GroupImpl", """
                    groupId: ${groupInfo.groupCode.takeIf { it != 0L } ?: id}
                    groupUin: ${groupInfo.uin}
                    membersCount: ${members.count()}
                    botId: ${bot.id}
                    owner: ${kotlin.runCatching { owner }.getOrNull()?.id}
                """.trimIndent(), additional = "并告知此时 Bot 是否为群管理员或群主, 和是否刚刚加入或离开这个群"
                )
            )
        }
    }
}

private val logger by lazy {
    MiraiLogger.Factory.create(GroupImpl::class, "Group")
}

internal fun Bot.nickIn(context: Contact): String =
    if (context is Group) context.botAsMember.nameCardOrNick else bot.nick

@Suppress("PropertyName")
internal class GroupImpl constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    override val id: Long,
    groupInfo: GroupInfo,
    override val members: ContactList<NormalMemberImpl>,
) : Group, AbstractContact(bot, parentCoroutineContext) {
    companion object

    val uin: Long = groupInfo.uin
    override val settings: GroupSettingsImpl = GroupSettingsImpl(this, groupInfo)
    override var name: String by settings::name

    override lateinit var owner: NormalMemberImpl
    override lateinit var botAsMember: NormalMemberImpl
    internal val botAsMemberInitialized get() = ::botAsMember.isInitialized

    @Suppress("DEPRECATION")
    @Deprecated("Please use files instead.", replaceWith = ReplaceWith("files.root"), level = DeprecationLevel.WARNING)
    @DeprecatedSinceMirai(warningSince = "2.8")
    override val filesRoot: RemoteFile by lazy { RemoteFileImpl(this, "/") }
    override val files: RemoteFiles by lazy { RemoteFilesImpl(this) }

    val lastTalkative = atomic<NormalMemberImpl?>(null)

    override val announcements: Announcements by lazy {
        AnnouncementsImpl(
            this,
            bot.network.logger.subLogger("Group $id")
        )
    }

    val groupPkgMsgParsingCache = GroupPkgMsgParsingCache()

    private val messageProtocolStrategy: MessageProtocolStrategy<GroupImpl> = GroupMessageProtocolStrategy(this)

    override suspend fun quit(): Boolean {
        check(botPermission != MemberPermission.OWNER) { "An owner cannot quit from a owning group" }

        if (!bot.groups.delegate.remove(this)) {
            return false
        }

        val response: ProfileService.GroupMngReq.GroupMngReqResponse = bot.network.sendAndExpect(
            ProfileService.GroupMngReq(bot.client, this@GroupImpl.id), 5000, 2
        )
        check(response.errorCode == 0) {
            "Group.quit failed: $response".also {
                bot.groups.delegate.add(this@GroupImpl)
            }
        }
        BotLeaveEvent.Active(this).broadcast()
        return true
    }

    override operator fun get(id: Long): NormalMemberImpl? {
        if (id == bot.id) return botAsMember
        return members.firstOrNull { it.id == id }
    }

    override fun contains(id: Long): Boolean {
        return bot.id == id || members.firstOrNull { it.id == id } != null
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        check(!isBotMuted) { throw BotIsBeingMutedException(this, message) }
        return sendMessageImpl(
            message,
            messageProtocolStrategy,
            ::GroupMessagePreSendEvent,
            ::GroupMessagePostSendEvent.cast()
        )
    }

    override suspend fun uploadImage(resource: ExternalResource): Image = resource.withAutoClose {
        if (BeforeImageUploadEvent(this, resource).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }

        fun OfflineGroupImage.putIntoCache() {
            // We can't understand wny Image(group.uploadImage().imageId)
            bot.components[ImagePatcher].putCache(this)
        }

        val imageInfo = runBIO { resource.calculateImageInfo() }

        val response: ImgStore.GroupPicUp.Response = bot.network.sendAndExpect(
            ImgStore.GroupPicUp(
                bot.client,
                uin = bot.id,
                groupCode = id,
                md5 = resource.md5,
                size = resource.size,
                filename = "${resource.md5.toUHexString("")}.${resource.formatName}",
                picWidth = imageInfo.width,
                picHeight = imageInfo.height,
                picType = getIdByImageType(imageInfo.imageType),
            ), 5000, 2
        )

        when (response) {
            is ImgStore.GroupPicUp.Response.Failed -> {
                ImageUploadEvent.Failed(this@GroupImpl, resource, response.resultCode, response.message).broadcast()
                if (response.message == "over file size max") throw OverFileSizeMaxException()
                error("upload group image failed with reason ${response.message}")
            }
            is ImgStore.GroupPicUp.Response.FileExists -> {
                val resourceId = resource.calculateResourceId()
                return response.fileInfo.run {
                    OfflineGroupImage(
                        imageId = resourceId,
                        height = fileHeight,
                        width = fileWidth,
                        imageType = getImageTypeById(fileType) ?: ImageType.UNKNOWN,
                        size = resource.size
                    )
                }
                    .also {
                        it.fileId = response.fileId.toInt()
                    }
                    .also { it.putIntoCache() }
                    .also { ImageUploadEvent.Succeed(this@GroupImpl, resource, it).broadcast() }
            }
            is ImgStore.GroupPicUp.Response.RequireUpload -> {
                // val servers = response.uploadIpList.zip(response.uploadPortList)
                Highway.uploadResourceBdh(
                    bot = bot,
                    resource = resource,
                    kind = GROUP_IMAGE,
                    commandId = 2,
                    initialTicket = response.uKey,
                    noBdhAwait = true,
                    fallbackSession = {
                        BdhSession(
                            EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY,
                            ssoAddresses = response.uploadIpList.zip(response.uploadPortList).toMutableSet(),
                        )
                    },
                )

                return imageInfo.run {
                    OfflineGroupImage(
                        imageId = resource.calculateResourceId(),
                        width = width,
                        height = height,
                        imageType = imageType,
                        size = resource.size
                    )
                }.also { it.fileId = response.fileId.toInt() }
                    .also { it.putIntoCache() }
                    .also { ImageUploadEvent.Succeed(this@GroupImpl, resource, it).broadcast() }
            }
        }
    }

    @Deprecated("use uploadAudio", replaceWith = ReplaceWith("uploadAudio(resource)"), level = DeprecationLevel.HIDDEN)
    @Suppress("OverridingDeprecatedMember", "DEPRECATION", "DEPRECATION_ERROR")
    override suspend fun uploadVoice(resource: ExternalResource): net.mamoe.mirai.message.data.Voice =
        AudioToSilkService.convert(
            resource
        ).useAutoClose { res ->
            return bot.network.run {
                uploadAudioResource(res)

                // val body = resp?.loadAs(Cmd0x388.RspBody.serializer())
                //     ?.msgTryupPttRsp
                //     ?.singleOrNull()?.fileKey ?: error("Group voice highway transfer succeed but failed to find fileKey")

                net.mamoe.mirai.message.data.Voice(
                    "${res.md5.toUHexString("")}.amr",
                    res.md5,
                    res.size,
                    res.voiceCodec,
                    ""
                )
            }
        }

    private suspend fun uploadAudioResource(resource: ExternalResource) {
        kotlin.runCatching {
            val (_) = Highway.uploadResourceBdh(
                bot = bot,
                resource = resource,
                kind = GROUP_AUDIO,
                commandId = 29,
                extendInfo = PttStore.GroupPttUp.createTryUpPttPack(bot.id, id, resource)
                    .toByteArray(Cmd0x388.ReqBody.serializer()),
            )
        }.recoverCatchingSuppressed {
            when (val resp = bot.network.sendAndExpect(PttStore.GroupPttUp(bot.client, bot.id, id, resource))) {
                is PttStore.GroupPttUp.Response.RequireUpload -> {
                    tryServersUpload(
                        bot,
                        resp.uploadIpList.zip(resp.uploadPortList),
                        resource.size,
                        GROUP_AUDIO,
                        ChannelKind.HTTP
                    ) { ip, port ->
                        @Suppress("DEPRECATION", "DEPRECATION_ERROR")
                        Mirai.Http.postPtt(ip, port, resource, resp.uKey, resp.fileKey)
                    }
                }
            }
        }.getOrThrow()
    }

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio = AudioToSilkService.convert(
        resource
    ).useAutoClose { res ->
        return bot.network.run {
            uploadAudioResource(res)

            // val body = resp?.loadAs(Cmd0x388.RspBody.serializer())
            //     ?.msgTryupPttRsp
            //     ?.singleOrNull()?.fileKey ?: error("Group voice highway transfer succeed but failed to find fileKey")

            OfflineAudioImpl(
                filename = "${res.md5.toUHexString("")}.amr",
                fileMd5 = res.md5,
                fileSize = res.size,
                codec = res.audioCodec,
                originalPtt = null,
            )
        }

    }

    override suspend fun setEssenceMessage(source: MessageSource): Boolean {
        checkBotPermission(MemberPermission.ADMINISTRATOR)
        val result = bot.network.sendAndExpect(
            TroopEssenceMsgManager.SetEssence(
                bot.client,
                this@GroupImpl.uin,
                source.internalIds.first(),
                source.ids.first()
            ), 5000, 2
        )
        return result.success
    }

    override fun toString(): String = "Group($id)"
}

internal fun Group.addNewNormalMember(memberInfo: MemberInfo): NormalMemberImpl? {
    if (members.contains(memberInfo.uin)) return null
    return newNormalMember(memberInfo).also {
        members.delegate.add(it)
    }
}

internal fun Group.newNormalMember(memberInfo: MemberInfo): NormalMemberImpl {
    this.checkIsGroupImpl()
    return NormalMemberImpl(
        this,
        this.coroutineContext,
        memberInfo
    )
}

internal fun GroupImpl.newAnonymous(name: String, id: String): AnonymousMemberImpl {
    return AnonymousMemberImpl(
        this, this.coroutineContext,
        MemberInfoImpl(
            uin = 80000000L,
            nick = name,
            permission = MemberPermission.MEMBER,
            remark = "匿名",
            nameCard = name,
            specialTitle = "匿名",
            muteTimestamp = 0,
            anonymousId = id,
        )
    )
}

