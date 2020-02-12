/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.utils.WeakRef
import kotlin.properties.Delegates


abstract class BotEvent : Event {
    private lateinit var _bot: Bot
    open val bot: Bot get() = _bot

    constructor(bot: Bot) : super() {
        this._bot = bot
    }

    constructor() : super()
}

class BotLoginSucceedEvent(bot: Bot) : BotEvent(bot)

class BotOfflineEvent(bot: Bot) : BotEvent(bot)

class BotReadyEvent(bot: Bot) : BotEvent(bot)

interface GroupEvent {
    val group: Group
}

class AddGroupEvent(bot: Bot, override val group: Group) : BotEvent(bot), GroupEvent

class RemoveGroupEvent(bot: Bot, override val group: Group) : BotEvent(bot), GroupEvent

class BotGroupPermissionChangeEvent(
    bot: Bot,
    override val group: Group,
    val origin: MemberPermission,
    val new: MemberPermission
) : BotEvent(bot), GroupEvent


interface GroupSettingChangeEvent<T> : GroupEvent {
    val origin: T
    val new: T
}

class GroupNameChangeEvent(
    bot: Bot,
    override val group: Group,
    override val origin: String,
    override val new: String
) : BotEvent(bot), GroupSettingChangeEvent<String>

class GroupMuteAllEvent(
    bot: Bot,
    override val group: Group,
    override val origin: Boolean,
    override val new: Boolean
) : BotEvent(bot), GroupSettingChangeEvent<Boolean>

class GroupConfessTalkEvent(
    bot: Bot,
    override val group: Group,
    override val origin: Boolean,
    override val new: Boolean
) : BotEvent(bot), GroupSettingChangeEvent<Boolean>

interface GroupMemberEvent : GroupEvent {
    val member: Member
    override val group: Group
        get() = member.group
}

class MemberJoinEvent(bot: Bot, override val member: Member) : BotEvent(bot), GroupMemberEvent

class MemberLeftEvent(bot: Bot, override val member: Member) : BotEvent(bot), GroupMemberEvent

class MemberMuteEvent(bot: Bot, override val member: Member) : BotEvent(bot), GroupMemberEvent

class MemberPermissionChangeEvent(
    bot: Bot,
    override val member: Member,
    val origin: MemberPermission,
    val new: MemberPermission
) : BotEvent(bot), GroupMemberEvent

