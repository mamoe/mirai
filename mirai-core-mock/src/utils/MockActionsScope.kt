/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.utils

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent
import net.mamoe.mirai.event.events.MemberSpecialTitleChangeEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockActions
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.contact.MockUser
import net.mamoe.mirai.mock.contact.MockUserOrBot

/**
 * 广播一些模拟事件
 */
public inline fun broadcastMockEvents(action: MockActionsScope.() -> Unit) {
    return MockActionsScopeInstance.action()
}

@PublishedApi
internal val MockActionsScopeInstance: MockActionsScope = object : MockActionsScope {}


public interface MockActionsScope { // use context receivers in the future
    /**
     * 修改 [MockUserOrBot.nick] 并广播相关事件 (如 [FriendNickChangedEvent])
     */
    @MockActionsDsl
    public suspend infix fun MockUserOrBot.nickChangesTo(value: String) {
        return MockActions.fireNickChanged(this, value)
    }

    /**
     * 修改 [MockNormalMember.nameCard] 并广播 [MemberCardChangeEvent]
     */
    @MockActionsDsl
    public suspend infix fun MockNormalMember.nameCardChangesTo(value: String) {
        return MockActions.fireNameCardChanged(this, value)
    }

    /**
     * 修改 [MockNormalMember.specialTitle] 并广播 [MemberSpecialTitleChangeEvent]
     */
    @MockActionsDsl
    public suspend infix fun MockNormalMember.specialTitleChangesTo(value: String) {
        return MockActions.fireSpecialTitleChanged(this, value)
    }

    /**
     * 修改一名成员的权限并广播 [MemberPermissionChangeEvent]
     */
    @MockActionsDsl
    public suspend infix fun MockNormalMember.permissionChangesTo(perm: MemberPermission) {
        return MockActions.firePermissionChanged(this, perm)
    }

    /**
     * 广播 [this] 被 [actor] 戳了的事件([NudgeEvent])
     *
     * - [actor] 戳了戳 [this] 的 XXXX
     */
    @MockActionsDsl
    public suspend fun MockUserOrBot.nudgedBy(actor: MockUserOrBot, block: NudgeDsl.() -> Unit = {}) {
        actor.nudged0(this, NudgeDsl().also(block))
    }

    /**
     * 广播 [target] 被 [this] 戳了的事件([NudgeEvent])
     *
     * - [this] 戳了戳 [target] 的 XXXX
     */
    @MockActionsDsl
    public suspend fun MockUserOrBot.nudges(target: MockUserOrBot, block: NudgeDsl.() -> Unit = {}) {
        nudged0(target, NudgeDsl().also(block))
    }

    /**
     * @see [MockUser.says]
     */
    @MockActionsDsl
    public suspend infix fun MockUser.says(block: MessageChainBuilder.() -> Unit): MessageChain {
        return says(buildMessageChain(block))
    }

    /**
     * @see [MockUser.says]
     */
    @MockActionsDsl
    public suspend infix fun MockUser.saysMessage(block: () -> Message): MessageChain {
        // no contract because compiler error
//        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        return says(block())
    }

    /**
     * 令消息原作者撤回一条消息
     */
    @MockActionsDsl
    public suspend fun MessageChain.recalledBySender() {
        return MockActions.fireMessageRecalled(this, null)
    }

    /**
     * 令消息原作者撤回一条消息
     */
    @MockActionsDsl
    public suspend fun MessageSource.recalledBySender() {
        return MockActions.fireMessageRecalled(this, null)
    }

    /**
     * 令消息原作者撤回一条消息
     */
    @MockActionsDsl
    public suspend fun MessageReceipt<*>.recalledBySender() {
        this.source.recalledBy(null)
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @MockActionsDsl
    public suspend infix fun MessageChain.recalledBy(operator: User?) {
        return MockActions.fireMessageRecalled(this, operator)
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @MockActionsDsl
    public suspend infix fun MessageSource.recalledBy(operator: User?) {
        return MockActions.fireMessageRecalled(this, operator)
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @MockActionsDsl
    public suspend infix fun MessageReceipt<*>.recalledBy(operator: User?) {
        this.source.recalledBy(operator)
    }
}