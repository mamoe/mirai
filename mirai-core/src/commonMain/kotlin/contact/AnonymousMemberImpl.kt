/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.internal.MiraiImpl
import kotlin.coroutines.CoroutineContext

internal class AnonymousMemberImpl(
    override val group: GroupImpl,
    override val coroutineContext: CoroutineContext,
    private val memberInfo: MemberInfo,
    override val anonymousId: String
) : AnonymousMember {
    override val nameCard: String get() = memberInfo.nameCard
    override val specialTitle: String get() = memberInfo.specialTitle
    override val permission: MemberPermission get() = memberInfo.permission
    override val bot: Bot get() = group.bot
    override val id: Long get() = memberInfo.uin
    override val nick: String get() = memberInfo.nick
    override val remark: String get() = memberInfo.remark

    override suspend fun mute(durationSeconds: Int) {
        checkBotPermissionHigherThanThis("mute")
        MiraiImpl._lowLevelMuteAnonymous(bot, anonymousId, nameCard, group.uin, durationSeconds)
    }

    override fun toString(): String = "AnonymousMember($nameCard, $anonymousId)"
}
