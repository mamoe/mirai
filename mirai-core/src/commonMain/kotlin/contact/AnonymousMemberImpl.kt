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
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MemberDeprecatedApi
import kotlin.coroutines.CoroutineContext

@OptIn(MemberDeprecatedApi::class)
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
    override val muteTimeRemaining: Int get() = -1
    override val nick: String get() = memberInfo.nick
    override val remark: String get() = memberInfo.remark

    override fun nudge(): MemberNudge = notSupported("Nudge")
    override suspend fun uploadImage(image: ExternalImage): Image = notSupported("Upload image to")
    override suspend fun unmute() {
    }

    override suspend fun mute(durationSeconds: Int) {
        checkBotPermissionHigherThanThis("mute")
        MiraiImpl._lowLevelMuteAnonymous(bot, anonymousId, nameCard, group.uin, durationSeconds)
    }

    override fun toString(): String = "AnonymousMember($nameCard, $anonymousId)"

    private fun notSupported(action: String): Nothing =
        throw IllegalStateException("$action anonymous is not allowed")

    override suspend fun sendMessage(message: Message): Nothing = notSupported("Send message to")
    override suspend fun sendMessage(message: String): Nothing = notSupported("Send message to")
    override suspend fun kick(message: String): Unit = notSupported("Kick")
}
