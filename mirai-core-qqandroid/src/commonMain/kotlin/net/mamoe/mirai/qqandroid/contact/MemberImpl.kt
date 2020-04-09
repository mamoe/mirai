/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR")

package net.mamoe.mirai.qqandroid.contact

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.launch
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.MemberCardChangeEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.event.events.MemberSpecialTitleChangeEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineFriendImage
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceToTempImpl
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.StTroopMemberInfo
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.utils.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

@OptIn(LowLevelAPI::class)
@Suppress("MemberVisibilityCanBePrivate")
internal class MemberImpl constructor(
    val qq: QQImpl, // 不要 WeakRef
    group: GroupImpl,
    override val coroutineContext: CoroutineContext,
    memberInfo: MemberInfo
) : Member() {
    override val group: GroupImpl by group.unsafeWeakRef()

    val lastMessageSequence: AtomicInt = atomic(-1)

    // region QQ delegate
    override val id: Long = qq.id
    override val nick: String = qq.nick

    @MiraiExperimentalAPI
    override suspend fun queryProfile(): Profile = qq.queryProfile()

    @MiraiExperimentalAPI
    override suspend fun queryPreviousNameList(): PreviousNameList = qq.queryPreviousNameList()

    @MiraiExperimentalAPI
    override suspend fun queryRemark(): FriendNameRemark = qq.queryRemark()

    @OptIn(MiraiInternalAPI::class)
    @JvmSynthetic
    @Suppress("DuplicatedCode", "UNCHECKED_CAST")
    override suspend fun sendMessage(message: Message): MessageReceipt<Member> {
        return sendMessageImpl(message).also {
            logMessageSent(message)
        }
    }

    private suspend fun sendMessageImpl(message: Message): MessageReceipt<Member> {
        lateinit var source: MessageSourceToTempImpl
        bot.network.run {
            check(
                MessageSvc.PbSendMsg.createToTemp(
                    bot.client,
                    this@MemberImpl,
                    message.asMessageChain()
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
            if (id != bot.id) {
                group.checkBotPermissionOperator()
            }
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
        checkBotPermissionHigherThanThis()
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

    private fun checkBotPermissionHigherThanThis() {
        check(group.botPermission > this.permission) {
            throw PermissionDeniedException(
                "`kick` operation requires bot to have a higher permission than the target member, " +
                        "but bot's is ${group.botPermission}, target's is ${this.permission}"
            )
        }
    }

    @JvmSynthetic
    override suspend fun unmute() {
        checkBotPermissionHigherThanThis()
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

    @OptIn(MiraiInternalAPI::class)
    @JvmSynthetic
    override suspend fun kick(message: String) {
        checkBotPermissionHigherThanThis()
        check(group.members.getOrNull(this.id) != null) {
            "Member ${this.id} had already been kicked from group ${group.id}"
        }
        bot.network.run {
            val response: TroopManagement.Kick.Response = TroopManagement.Kick(
                client = bot.client,
                member = this@MemberImpl,
                message = message
            ).sendAndExpect()

            check(response.success) { "kick failed: ${response.ret}" }

            group.members.delegate.removeIf { it.id == this@MemberImpl.id }
            MemberLeaveEvent.Kick(this@MemberImpl, null).broadcast()
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

@OptIn(ExperimentalContracts::class)
internal fun Member.checkIsMemberImpl(): MemberImpl {
    contract {
        returns() implies (this@checkIsMemberImpl is MemberImpl)
    }
    check(this is MemberImpl) { "A Member instance is not instance of MemberImpl. Don't interlace two protocol implementations together!" }
    return this
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
