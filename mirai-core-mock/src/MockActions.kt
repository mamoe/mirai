/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.mock.contact.MockFriend
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.contact.MockStranger
import net.mamoe.mirai.mock.contact.MockUserOrBot
import net.mamoe.mirai.mock.database.removeMessageInfo
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.cast

@JvmBlockingBridge
public object MockActions {

    /**
     * 修改 [MockUserOrBot.nick] 并广播相关事件 (如 [FriendNickChangedEvent])
     */
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
    @JvmStatic
    public suspend fun fireNameCardChanged(member: MockNormalMember, value: String) {
        val ov = member.nameCard
        member.mockApi.nameCard = value
        MemberCardChangeEvent(ov, value, member).broadcast()
    }

    /**
     * 修改 [MockNormalMember.specialTitle] 并广播 [MemberSpecialTitleChangeEvent]
     */
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
    @JvmStatic
    public suspend fun fireMessageRecalled(source: MessageSource, operator: User? = null) {
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
    @JvmStatic
    public suspend fun mockFireRecalled(receipt: MessageReceipt<*>, operator: User? = null) {
        return fireMessageRecalled(receipt.source, operator)
    }

}