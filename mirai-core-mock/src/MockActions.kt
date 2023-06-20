/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.contact.MockFriend
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.contact.MockStranger
import net.mamoe.mirai.mock.contact.MockUserOrBot
import net.mamoe.mirai.mock.database.removeMessageInfo
import net.mamoe.mirai.mock.utils.NudgeDsl
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.mock.utils.nudged0
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.cast

@JvmBlockingBridge
public object MockActions {

    /**
     * 修改 [MockUserOrBot.nick] 并广播相关事件 (如 [FriendNickChangedEvent])
     */
    @OptIn(MiraiInternalApi::class)
    @JvmStatic
    public suspend fun fireNickChanged(target: MockUserOrBot, value: String) {
        when (target) {
            is MockFriend -> {
                val ov = target.nick
                target.mockApi.nick = value
                FriendNickChangedEvent(target, ov, target.nick).broadcast()
            }

            is MockStranger -> {
                target.mockApi.nick = value
                // TODO: StrangerNickChangedEvent
            }

            is MockNormalMember -> {
                val friend0 = target.bot.getFriend(target.id)
                if (friend0 != null) {
                    return fireNickChanged(friend0, value)
                }
                target.mockApi.nick = value
            }

            is MockBot -> {
                target.nick = value
            }
        }
    }

    /**
     * 修改 [MockNormalMember.nameCard] 并广播 [MemberCardChangeEvent]
     */
    @OptIn(MiraiInternalApi::class)
    @JvmStatic
    public suspend fun fireNameCardChanged(member: MockNormalMember, value: String) {
        val ov = member.nameCard
        member.mockApi.nameCard = value
        MemberCardChangeEvent(ov, value, member).broadcast()
    }

    /**
     * 修改 [MockNormalMember.specialTitle] 并广播 [MemberSpecialTitleChangeEvent]
     */
    @OptIn(MiraiInternalApi::class)
    @JvmStatic
    public suspend fun fireSpecialTitleChanged(member: MockNormalMember, value: String) {
        val ov = member.specialTitle
        member.mockApi.specialTitle = value
        MemberSpecialTitleChangeEvent(
            ov,
            value,
            member,
            operator = member.group.owner.takeIf { it.id != member.bot.id },
        ).broadcast()
    }

    /**
     * 修改一名成员的权限并广播 [MemberPermissionChangeEvent]
     */
    @OptIn(MiraiInternalApi::class)
    @JvmStatic
    public suspend fun firePermissionChanged(member: MockNormalMember, perm: MemberPermission) {
        if (perm == MemberPermission.OWNER || member == member.group.owner) {
            error("Use group.changeOwner to modify group owner")
        }
        val ov = member.permission
        member.mockApi.permission = perm
        if (member.id == member.bot.id) {
            BotGroupPermissionChangeEvent(member.group, ov, perm)
        } else {
            MemberPermissionChangeEvent(member, ov, perm)
        }.broadcast()
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @JvmStatic
    public suspend fun fireMessageRecalled(chain: MessageChain, operator: User? = null) {
        return fireMessageRecalled(chain.source, operator)
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @OptIn(MiraiInternalApi::class)
    @JvmStatic
    public suspend fun fireMessageRecalled(source: MessageSource, operator: User? = null) {
        fun notSupported(): Nothing = error("Unsupported message source kind: ${source.kind}: ${source.javaClass}")

        val bot: MockBot = when {
            source is OnlineMessageSource -> source.bot.mock()
            operator != null -> operator.bot.mock()
            else -> source.botOrNull?.mock() ?: error("Cannot find bot from source or operator")
        }

        val sourceKind = source.kind

        fun target(): ContactOrBot = when {
            source is OnlineMessageSource -> source.target
            source.targetId == bot.id -> bot

            sourceKind == MessageSourceKind.FRIEND -> bot.getFriendOrFail(source.targetId)
            sourceKind == MessageSourceKind.STRANGER -> bot.getStrangerOrFail(source.targetId)
            sourceKind == MessageSourceKind.TEMP -> error("Cannot detect message target from TEMP source kind")
            sourceKind == MessageSourceKind.GROUP -> bot.getGroupOrFail(source.targetId)

            else -> notSupported()
        }

        fun sender(): ContactOrBot = when {
            source is OnlineMessageSource -> source.sender
            source.fromId == bot.id -> bot


            sourceKind == MessageSourceKind.FRIEND -> bot.getFriendOrFail(source.fromId)
            sourceKind == MessageSourceKind.STRANGER -> bot.getStrangerOrFail(source.fromId)
            sourceKind == MessageSourceKind.TEMP -> error("Cannot detect message sender from TEMP source kind")
            sourceKind == MessageSourceKind.GROUP -> throw AssertionError("Message from group")

            else -> notSupported()
        }

        fun subject(): Contact = when {
            source is OnlineMessageSource -> source.subject

            source.fromId == bot.id -> target() as Contact
            sourceKind == MessageSourceKind.GROUP -> target() as Contact

            else -> sender() as Contact
        }


        when (sourceKind) {
            MessageSourceKind.GROUP -> {
                val sender = sender()
                val group = subject() as Group

                val operator0 = when {
                    operator === bot -> null
                    operator === group.botAsMember -> null

                    operator == null -> sender.cast()
                    operator is Member -> operator

                    else -> error("Provided operator $operator(${operator.javaClass}) not a member")
                }

                bot.msgDatabase.removeMessageInfo(source)
                MessageRecallEvent.GroupRecall(
                    bot, sender.id, source.ids, source.internalIds, source.time,
                    operator0,
                    group,
                    when (sender) {
                        is Bot -> group.botAsMember
                        else -> sender.cast()
                    }
                ).broadcast()
            }
            MessageSourceKind.FRIEND -> {
                val subject = subject() as Friend

                bot.msgDatabase.removeMessageInfo(source)
                if (source.fromId == bot.id) {
                    return // no event
                }

                MessageRecallEvent.FriendRecall(bot, source.ids, source.internalIds, source.time, subject.id, subject)
                    .broadcast()
            }
            MessageSourceKind.TEMP -> {
                bot.mock().msgDatabase.removeMessageInfo(source)
                // TODO: event not available
            }
            MessageSourceKind.STRANGER -> {
                bot.mock().msgDatabase.removeMessageInfo(source)
                // TODO: event not available
            }

            else -> notSupported()
        }
    }

    /**
     * 令 [operator] 撤回一条消息
     *
     * @param operator 当 [operator] 为 null 时代表是发送者自己撤回
     */
    @JvmStatic
    public suspend fun mockFireRecalled(receipt: MessageReceipt<*>, operator: User? = null) {
        return fireMessageRecalled(receipt.source, operator)
    }

    /**
     * 令 [actor] 戳一下 [actee]
     *
     * @param actor 发起戳一戳的人
     * @param actee 被戳的人
     */
    @JvmStatic
    public suspend fun fireNudge(actor: MockUserOrBot, actee: MockUserOrBot, dsl: NudgeDsl) {
        actor.nudged0(actee, dsl)
    }

}