/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.qqandroid.contact

import kotlinx.coroutines.launch
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OfflineFriendImage
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.StTroopMemberInfo
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

@OptIn(LowLevelAPI::class)
@Suppress("MemberVisibilityCanBePrivate")
internal class MemberImpl  constructor(
    val qq: QQImpl, // 不要 WeakRef
    group: GroupImpl,
    override val coroutineContext: CoroutineContext,
    memberInfo: MemberInfo
) : Member() {
    override val group: GroupImpl by group.unsafeWeakRef()

    // region QQ delegate
    override val id: Long = qq.id
    override val nick: String = qq.nick

    @MiraiExperimentalAPI
    override suspend fun queryProfile(): Profile = qq.queryProfile()

    @MiraiExperimentalAPI
    override suspend fun queryPreviousNameList(): PreviousNameList = qq.queryPreviousNameList()

    @MiraiExperimentalAPI
    override suspend fun queryRemark(): FriendNameRemark = qq.queryRemark()

    @JvmSynthetic
    @Suppress("DuplicatedCode")
    override suspend fun sendMessage(message: Message): MessageReceipt<Member> {
        val event = MessageSendEvent.FriendMessageSendEvent(this, message.asMessageChain()).broadcast()
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
                ) {
                    source = it
                }.sendAndExpect<MessageSvc.PbSendMsg.Response>() is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed" }
        }
        return MessageReceipt(source, this, null)
    }

    @JvmSynthetic
    override suspend fun uploadImage(image: ExternalImage): OfflineFriendImage = qq.uploadImage(image)
    // endregion

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

    @JvmSynthetic
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

    @JvmSynthetic
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

    @JvmSynthetic
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false
        if (this::class != other::class) return false
        return this.id == other.id && this.bot == other.bot
    }

    override fun toString(): String {
        return "Member($id)"
    }
}

@OptIn(LowLevelAPI::class)
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
