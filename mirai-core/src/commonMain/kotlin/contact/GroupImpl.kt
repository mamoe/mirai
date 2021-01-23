/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(LowLevelApi::class)

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.*
import net.mamoe.mirai.internal.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.voiceCodec
import net.mamoe.mirai.internal.network.protocol.packet.list.ProfileService
import net.mamoe.mirai.internal.utils.GroupPkgMsgParsingCache
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

internal fun GroupImpl.Companion.checkIsInstance(instance: Group) {
    contract { returns() implies (instance is GroupImpl) }
    check(instance is GroupImpl) { "group is not an instanceof GroupImpl!! DO NOT interlace two or more protocol implementations!!" }
}

internal fun Group.checkIsGroupImpl() {
    contract { returns() implies (this@checkIsGroupImpl is GroupImpl) }
    GroupImpl.checkIsInstance(this)
}

@Suppress("PropertyName")
internal class GroupImpl(
    bot: QQAndroidBot,
    coroutineContext: CoroutineContext,
    override val id: Long,
    groupInfo: GroupInfo,
    members: Sequence<MemberInfo>
) : Group, AbstractContact(bot, coroutineContext) {
    companion object

    val uin: Long = groupInfo.uin
    override val settings: GroupSettingsImpl = GroupSettingsImpl(this, groupInfo)
    override var name: String by settings::name

    override lateinit var owner: NormalMember
    override lateinit var botAsMember: NormalMember

    override val members: ContactList<NormalMember> = ContactList(members.mapNotNullTo(ConcurrentLinkedQueue()) {
        if (it.uin == bot.id) {
            botAsMember = newMember(it).cast()
            if (it.permission == MemberPermission.OWNER) {
                owner = botAsMember
            }
            null
        } else newMember(it).cast<NormalMember>().also { member ->
            if (member.permission == MemberPermission.OWNER) {
                owner = member
            }
        }
    })

    val groupPkgMsgParsingCache = GroupPkgMsgParsingCache()

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
                    bot.groups.delegate.add(this@GroupImpl)
                }
            }
        }
        BotLeaveEvent.Active(this).broadcast()
        return true
    }

    override operator fun get(id: Long): NormalMember? {
        if (id == bot.id) {
            return botAsMember
        }
        return members.firstOrNull { it.id == id }
    }

    override fun contains(id: Long): Boolean {
        return bot.id == id || members.firstOrNull { it.id == id } != null
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        require(!message.isContentEmpty()) { "message is empty" }
        check(!isBotMuted) { throw BotIsBeingMutedException(this) }

        val chain = broadcastGroupMessagePreSendEvent(message)

        val result = sendMessageImpl(message, chain, GroupMessageSendingStep.FIRST)

        // logMessageSent(result.getOrNull()?.source?.plus(chain) ?: chain) // log with source
        logMessageSent(chain)

        GroupMessagePostSendEvent(this, chain, result.exceptionOrNull(), result.getOrNull()).broadcast()

        return result.getOrThrow()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun uploadImage(resource: ExternalResource): Image {
        if (BeforeImageUploadEvent(this, resource).broadcast().isCancelled) {
            throw EventCancelledException("cancelled by BeforeImageUploadEvent.ToGroup")
        }
        bot.network.run<QQAndroidBotNetworkHandler, Image> {
            val response: ImgStore.GroupPicUp.Response = ImgStore.GroupPicUp(
                bot.client,
                uin = bot.id,
                groupCode = id,
                md5 = resource.md5,
                size = resource.size.toInt()
            ).sendAndExpect()

            when (response) {
                is ImgStore.GroupPicUp.Response.Failed -> {
                    ImageUploadEvent.Failed(this@GroupImpl, resource, response.resultCode, response.message).broadcast()
                    if (response.message == "over file size max") throw OverFileSizeMaxException()
                    error("upload group image failed with reason ${response.message}")
                }
                is ImgStore.GroupPicUp.Response.FileExists -> {
                    val resourceId = resource.calculateResourceId()
                    return OfflineGroupImage(imageId = resourceId)
                        .also { ImageUploadEvent.Succeed(this@GroupImpl, resource, it).broadcast() }
                }
                is ImgStore.GroupPicUp.Response.RequireUpload -> {
                    Highway.uploadResource(
                        bot,
                        response.uploadIpList.zip(response.uploadPortList),
                        response.uKey,
                        resource,
                        kind = "group image",
                        commandId = 2
                    )
                    val resourceId = resource.calculateResourceId()
                    return OfflineGroupImage(imageId = resourceId)
                        .also { ImageUploadEvent.Succeed(this@GroupImpl, resource, it).broadcast() }
                }
            }
        }
    }

    override suspend fun uploadVoice(resource: ExternalResource): Voice {
        if (resource.size > 1048576) {
            throw  OverFileSizeMaxException()
        }
        return bot.network.run {
            val response: PttStore.GroupPttUp.Response.RequireUpload =
                PttStore.GroupPttUp(bot.client, bot.id, id, resource).sendAndExpect()

            Highway.uploadPttToServers(
                bot,
                response.uploadIpList.zip(response.uploadPortList),
                resource,
                response.uKey,
                response.fileKey,
            )
            Voice(
                "${resource.md5.toUHexString("")}.amr",
                resource.md5,
                resource.size,
                resource.voiceCodec,
                ""
            )
        }

    }

    override fun toString(): String = "Group($id)"
}

internal fun Group.newMember(memberInfo: MemberInfo): Member {
    this.checkIsGroupImpl()
    memberInfo.anonymousId?.let { anId ->
        return AnonymousMemberImpl(
            this, this.coroutineContext,
            memberInfo, anId
        )
    }
    return NormalMemberImpl(
        this,
        this.coroutineContext,
        memberInfo
    )
}

internal fun GroupImpl.newAnonymous(name: String, id: String): AnonymousMemberImpl = newMember(
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
) as AnonymousMemberImpl
