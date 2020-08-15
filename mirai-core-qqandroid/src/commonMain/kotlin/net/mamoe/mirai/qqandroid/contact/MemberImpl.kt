/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.qqandroid.contact

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceToTempImpl
import net.mamoe.mirai.qqandroid.message.ensureSequenceIdAvailable
import net.mamoe.mirai.qqandroid.message.firstIsInstanceOrNull
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.StTroopMemberInfo
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.createToTemp
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

@OptIn(LowLevelAPI::class)
@Suppress("MemberVisibilityCanBePrivate")
internal class MemberImpl constructor(
    val qq: FriendImpl, // 不要 WeakRef
    group: GroupImpl,
    coroutineContext: CoroutineContext,
    memberInfo: MemberInfo
) : Member() {
    override val group: GroupImpl by group.unsafeWeakRef()
    override val coroutineContext: CoroutineContext = coroutineContext + SupervisorJob(coroutineContext[Job])

    @Suppress("unused") // false positive
    val lastMessageSequence: AtomicInt = atomic(-1)

    override val id: Long = qq.id
    override val nick: String = qq.nick

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    override suspend fun sendMessage(message: Message): MessageReceipt<Member> {
        require(message.isContentNotEmpty()) { "message is empty" }

        val asFriend = this.asFriendOrNull()

        return (asFriend?.sendMessageImpl(
            message,
            friendReceiptConstructor = { MessageReceipt(it, asFriend, null) },
            tReceiptConstructor = { MessageReceipt(it, this, null) }
        ) ?: sendMessageImpl(message)).also { logMessageSent(message) }
    }

    private suspend fun sendMessageImpl(message: Message): MessageReceipt<Member> {
        val chain = kotlin.runCatching {
            TempMessagePreSendEvent(this, message).broadcast()
        }.onSuccess {
            check(!it.isCancelled) {
                throw EventCancelledException("cancelled by TempMessagePreSendEvent")
            }
        }.getOrElse {
            throw EventCancelledException("exception thrown when broadcasting TempMessagePreSendEvent", it)
        }.message.asMessageChain()

        chain.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()

        val result = bot.network.runCatching {
            val source: MessageSourceToTempImpl
            MessageSvcPbSendMsg.createToTemp(
                bot.client,
                this@MemberImpl,
                chain
            ) {
                source = it
            }.sendAndExpect<MessageSvcPbSendMsg.Response>().let {
                check(it is MessageSvcPbSendMsg.Response.SUCCESS) {
                    "Send temp message failed: $it"
                }
            }
            MessageReceipt(source, this@MemberImpl, null)
        }

        result.fold(
            onSuccess = {
                TempMessagePostSendEvent(this, chain, null, it)
            },
            onFailure = {
                TempMessagePostSendEvent(this, chain, it, null)
            }
        ).broadcast()

        return result.getOrThrow()
    }

    @JvmSynthetic
    override suspend fun uploadImage(image: ExternalImage): Image = qq.uploadImage(image)

    override var permission: MemberPermission = memberInfo.permission

    @Suppress("PropertyName")
    internal var _nameCard: String = memberInfo.nameCard

    @Suppress("PropertyName")
    internal var _specialTitle: String = memberInfo.specialTitle

    @Suppress("PropertyName")
    var _muteTimestamp: Int = memberInfo.muteTimestamp

    override val muteTimeRemaining: Int
        get() = if (_muteTimestamp == 0 || _muteTimestamp == 0xFFFFFFFF.toInt()) {
            0
        } else {
            (_muteTimestamp - currentTimeSeconds.toInt()).coerceAtLeast(0)
        }

    override var nameCard: String
        get() = _nameCard
        set(newValue) {
            if (id != bot.id) {
                group.checkBotPermission(MemberPermission.ADMINISTRATOR)
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
                    MemberCardChangeEvent(oldValue, newValue, this@MemberImpl).broadcast()
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
        check(this.id != bot.id) {
            "A bot can't mute itself."
        }
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

            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
            group.members.delegate.removeIf { it.id == this@MemberImpl.id }
            this@MemberImpl.cancel(CancellationException("Kicked by bot"))
            MemberLeaveEvent.Kick(this@MemberImpl, null).broadcast()
        }
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
