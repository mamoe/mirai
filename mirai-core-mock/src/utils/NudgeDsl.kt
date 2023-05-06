/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.mock.contact.MockUserOrBot
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 构造 Nudge 的 DSL
 *
 * @see MockActionsScope.nudgedBy
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

@OptIn(MiraiInternalApi::class)
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
