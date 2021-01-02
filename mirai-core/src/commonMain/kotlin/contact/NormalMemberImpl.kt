/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.contact

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.message.MessageSourceToTempImpl
import net.mamoe.mirai.internal.message.ensureSequenceIdAvailable
import net.mamoe.mirai.internal.message.firstIsInstanceOrNull
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.createToTemp
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

@OptIn(LowLevelApi::class)
@Suppress("MemberVisibilityCanBePrivate")
internal class NormalMemberImpl constructor(
    group: GroupImpl,
    coroutineContext: CoroutineContext,
    memberInfo: MemberInfo
) : NormalMember, AbstractUser(group.bot, coroutineContext, memberInfo) {
    override val group: GroupImpl by group.unsafeWeakRef()

    @Suppress("unused") // false positive
    val lastMessageSequence: AtomicInt = atomic(-1)

    override fun toString(): String = "NormalMember($id)"

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    override suspend fun sendMessage(message: Message): MessageReceipt<NormalMember> {
        require(message.isContentNotEmpty()) { "message is empty" }

        val asFriend = this.asFriendOrNull()
        val asStranger = this.asStrangerOrNull()

        return (asFriend?.sendMessageImpl(
            message,
            friendReceiptConstructor = { MessageReceipt(it, asFriend) },
            tReceiptConstructor = { MessageReceipt(it, this) }
        ) ?: asStranger?.sendMessageImpl(
            message,
            strangerReceiptConstructor = { MessageReceipt(it, asStranger) },
            tReceiptConstructor = { MessageReceipt(it, this) }
        ) ?: sendMessageImpl(message)).also { logMessageSent(message) }
    }

    private suspend fun sendMessageImpl(message: Message): MessageReceipt<NormalMember> {
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
                this@NormalMemberImpl,
                chain
            ) {
                source = it
            }.sendAndExpect<MessageSvcPbSendMsg.Response>().let {
                check(it is MessageSvcPbSendMsg.Response.SUCCESS) {
                    "Send temp message failed: $it"
                }
            }
            MessageReceipt(source, this@NormalMemberImpl)
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

    override var permission: MemberPermission = memberInfo.permission

    @Suppress("PropertyName")
    internal var _nameCard: String = memberInfo.nameCard

    @Suppress("PropertyName")
    internal var _specialTitle: String = memberInfo.specialTitle

    @Suppress("PropertyName")
    var _muteTimestamp: Int = memberInfo.muteTimestamp

    @Suppress("PropertyName")
    var _nudgeTimestamp: Long = 0L

    override val muteTimeRemaining: Int
        get() = if (_muteTimestamp == 0 || _muteTimestamp == 0xFFFFFFFF.toInt()) {
            0
        } else {
            (_muteTimestamp - currentTimeSeconds().toInt()).coerceAtLeast(0)
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
                            this@NormalMemberImpl,
                            newValue
                        ).sendWithoutExpect()
                    }
                    MemberCardChangeEvent(oldValue, newValue, this@NormalMemberImpl).broadcast()
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
                            this@NormalMemberImpl,
                            newValue
                        ).sendWithoutExpect()
                    }
                    MemberSpecialTitleChangeEvent(oldValue, newValue, this@NormalMemberImpl, null).broadcast()
                }
            }
        }

    override suspend fun mute(durationSeconds: Int) {
        check(this.id != bot.id) {
            "A bot can't mute itself."
        }
        checkBotPermissionHigherThanThis("mute")
        bot.network.run {
            TroopManagement.Mute(
                client = bot.client,
                groupCode = group.id,
                memberUin = this@NormalMemberImpl.id,
                timeInSecond = durationSeconds
            ).sendAndExpect<TroopManagement.Mute.Response>()
        }

        @Suppress("RemoveRedundantQualifierName") // or unresolved reference
        net.mamoe.mirai.event.events.MemberMuteEvent(this@NormalMemberImpl, durationSeconds, null).broadcast()
    }

    override suspend fun unmute() {
        checkBotPermissionHigherThanThis("unmute")
        bot.network.run {
            TroopManagement.Mute(
                client = bot.client,
                groupCode = group.id,
                memberUin = this@NormalMemberImpl.id,
                timeInSecond = 0
            ).sendAndExpect<TroopManagement.Mute.Response>()
        }

        @Suppress("RemoveRedundantQualifierName") // or unresolved reference
        net.mamoe.mirai.event.events.MemberUnmuteEvent(this@NormalMemberImpl, null).broadcast()
    }

    override suspend fun kick(message: String) {
        checkBotPermissionHigherThanThis("kick")
        check(group.members[this.id] != null) {
            "Member ${this.id} had already been kicked from group ${group.id}"
        }
        bot.network.run {
            val response: TroopManagement.Kick.Response = TroopManagement.Kick(
                client = bot.client,
                member = this@NormalMemberImpl,
                message = message
            ).sendAndExpect()

            check(response.success) { "kick failed: ${response.ret}" }

            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
            group.members.delegate.removeIf { it.id == this@NormalMemberImpl.id }
            this@NormalMemberImpl.cancel(CancellationException("Kicked by bot"))
            MemberLeaveEvent.Kick(this@NormalMemberImpl, null).broadcast()
        }
    }
}

internal fun Member.checkBotPermissionHigherThanThis(operationName: String) {
    check(group.botPermission > this.permission) {
        throw PermissionDeniedException(
            "`$operationName` operation requires a higher permission, while " +
                    "${group.botPermission} < ${this.permission}"
        )
    }
}

@OptIn(ExperimentalContracts::class)
internal fun Member.checkIsMemberImpl(): NormalMemberImpl {
    contract {
        returns() implies (this@checkIsMemberImpl is NormalMemberImpl)
    }
    check(this is NormalMemberImpl) { "A Member instance is not instance of MemberImpl. Don't interlace two protocol implementations together!" }
    return this
}

