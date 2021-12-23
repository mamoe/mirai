/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock.utils

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.mock.contact.*
import net.mamoe.mirai.mock.database.removeMessageInfo
import net.mamoe.mirai.mock.utils.MockActions.nudged
import net.mamoe.mirai.mock.utils.MockActions.nudgedBy
import net.mamoe.mirai.utils.cast
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@DslMarker
private annotation class MockActionsDsl

@JvmBlockingBridge
public object MockActions {

    /**
     * 修改 [this.nick] 并广播相关事件 (如 [FriendNickChangedEvent])
     */
    @JvmStatic
    @JvmName("fireNickChanged")
    @MockActionsDsl
    public suspend infix fun MockUserOrBot.nickChangesTo(value: String) {
        when (this) {
            is MockFriend -> {
                val ov = this.nick
                this.mockApi.nick = value
                FriendNickChangedEvent(this, ov, nick).broadcast()
            }
            is MockStranger -> {
                this.mockApi.nick = value
                // TODO: StrangerNickChangedEvent
            }
            is MockNormalMember -> {
                val friend0 = bot.getFriend(this.id)
                if (friend0 != null) {
                    return friend0.nickChangesTo(value)
                }
                this.mockApi.nick = value
            }
            is MockBot -> {
                this.nick = value
            }
        }
    }

    /**
     * 修改 [MockNormalMember.nameCard] 并广播 [MemberCardChangeEvent]
     */
    @JvmStatic
    @JvmName("fireNameCardChanged")
    @MockActionsDsl
    public suspend infix fun MockNormalMember.nameCardChangesTo(value: String) {
        val ov = this.nameCard
        mockApi.nameCard = value
        MemberCardChangeEvent(ov, value, this).broadcast()
    }

    /**
     * 修改 [MockNormalMember.specialTitle] 并广播 [MemberSpecialTitleChangeEvent]
     */
    @JvmStatic
    @JvmName("fireSpecialTitleChanged")
    @MockActionsDsl
    public suspend infix fun MockNormalMember.specialTitleChangesTo(value: String) {
        val ov = specialTitle
        mockApi.specialTitle = value
        MemberSpecialTitleChangeEvent(
            ov,
            value,
            this,
            operator = group.owner.takeIf { it.id != bot.id },
        ).broadcast()
    }

    /**
     * 修改一名成员的权限并广播 [MemberPermissionChangeEvent]
     */
    @JvmStatic
    @JvmName("firePermissionChanged")
    @MockActionsDsl
    public suspend infix fun MockNormalMember.permissionChangesTo(perm: MemberPermission) {
        if (perm == MemberPermission.OWNER || this == group.owner) {
            error("Use group.changeOwner to modify group owner")
        }
        val ov = permission
        mockApi.permission = perm
        if (id == bot.id) {
            BotGroupPermissionChangeEvent(group, ov, perm)
        } else {
            MemberPermissionChangeEvent(this, ov, perm)
        }.broadcast()
    }

    /**
     * 广播 [this] 被 [actor] 戳了的事件([NudgeEvent])
     *
     * - [actor] 戳了戳 [this] 的 XXXX
     */
    @MockActionsDsl
    @JvmSynthetic
    public suspend inline fun MockUserOrBot.nudgedBy(actor: MockUserOrBot, block: NudgeDsl.() -> Unit = {}) {
        // contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        actor.nudged0(this, NudgeDsl().also(block))
    }

    /**
     * 广播 [target] 被 [this] 戳了的事件([NudgeEvent])
     *
     * - [this] 戳了戳 [target] 的 XXXX
     */
    @MockActionsDsl
    @JvmSynthetic
    public suspend inline fun MockUserOrBot.nudged(target: MockUserOrBot, block: NudgeDsl.() -> Unit = {}) {
        // contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        nudged0(target, NudgeDsl().also(block))
    }

    /**
     * @see [MockUser.says]
     */
    @JvmSynthetic
    @MockActionsDsl
    public suspend inline infix fun MockUser.says(block: MessageChainBuilder.() -> Unit): MessageChain {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        return says(buildMessageChain(block))
    }

    /**
     * @see [MockUser.says]
     */
    @JvmSynthetic
    @MockActionsDsl
    public suspend inline fun MockUser.saysMessage(block: () -> Message): MessageChain {
        // no contract because compiler error
        //contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        return says(block())
    }

    @JvmSynthetic
    @PublishedApi
    internal suspend fun MockUser.says0(msg: Message): MessageChain = says(msg)

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @MockBotDSL
    @JvmStatic
    @JvmName("fireMessageRecalled")
    public suspend fun MessageChain.mockFireRecalled(operator: User? = null) {
        source.mockFireRecalled(operator)
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @MockBotDSL
    @JvmStatic
    @JvmName("fireMessageRecalled")
    public suspend fun MessageSource.mockFireRecalled(operator: User? = null) {
        val source = this
        if (source is OnlineMessageSource) {
            val from = source.sender
            when (val target = source.target) {
                is Group -> {
                    from.bot.mock().msgDatabase.removeMessageInfo(source)
                    MessageRecallEvent.GroupRecall(
                        source.bot,
                        from.id,
                        source.ids,
                        source.internalIds,
                        source.time,
                        operator?.cast(),
                        target,
                        when (from) {
                            is Bot -> target.botAsMember
                            else -> from.cast()
                        }
                    ).broadcast()
                    return
                }
                is Friend -> {
                    from.bot.mock().msgDatabase.removeMessageInfo(source)
                    MessageRecallEvent.FriendRecall(
                        source.bot,
                        source.ids,
                        source.internalIds,
                        source.time,
                        from.id,
                        from.cast()
                    ).broadcast()
                    return
                }
            }
        }
        error("Unsupported message source type: ${source.javaClass}")
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @MockBotDSL
    @JvmStatic
    @JvmName("fireMessageRecalled")
    public suspend fun MessageReceipt<*>.mockFireRecalled(operator: User? = null) {
        this.source.mockFireRecalled(operator)
    }

}

/**
 * 构造 Nudge 的 DSL
 *
 * @see [MockUserOrBot.nudgedBy]
 * @see [MockActions.nudged]
 * @see [MockActions.nudgedBy]
 */
public class NudgeDsl {
    @set:JvmSynthetic
    public var action: String = "戳了戳"

    @set:JvmSynthetic
    public var suffix: String = ""

    @MockActionsDsl
    public fun action(value: String): NudgeDsl = apply { action = value }

    @MockActionsDsl
    public fun suffix(value: String): NudgeDsl = apply { suffix = value }
}

@PublishedApi
internal suspend fun MockUserOrBot.nudged0(target: MockUserOrBot, dsl: NudgeDsl) {

    when {
        this is Member && target is Member -> {
            if (this.group != target.group)
                error("Cross group nudging")
        }
        this is AnonymousMember -> error("anonymous member can't starting a nudge action")
        target is AnonymousMember -> error("anonymous member is not nudgeable")

        this is Bot && target is Bot -> error("Not yet support bot nudging bot")
    }

    val subject: Contact = when {
        this is Member -> this.group
        target is Member -> target.group

        this is Friend -> this
        target is Friend -> target

        this is Stranger -> this
        target is Stranger -> target

        else -> error("Not yet support $target nudging $this")
    }

    NudgeEvent(
        from = this,
        target = target,
        subject = subject,
        action = dsl.action,
        suffix = dsl.suffix,
    ).broadcast()

}
