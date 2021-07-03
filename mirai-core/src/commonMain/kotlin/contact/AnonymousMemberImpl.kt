/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.internal.MiraiImpl
import net.mamoe.mirai.internal.getMiraiImpl
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

internal class AnonymousMemberImpl(
    group: GroupImpl,
    parentCoroutineContext: CoroutineContext,
    memberInfo: MemberInfo,
) : AnonymousMember, AbstractMember(group, parentCoroutineContext, memberInfo) {
    init {
        requireNotNull(memberInfo.anonymousId) { "anonymousId must not be null" }
    }

    override val anonymousId: String get() = info.anonymousId!!

    override suspend fun mute(durationSeconds: Int) {
        checkBotPermissionHigherThanThis("mute")
        getMiraiImpl().muteAnonymousMember(bot, anonymousId, nameCard, group.uin, durationSeconds)
    }

    override fun toString(): String = "AnonymousMember($nameCard, $anonymousId)"
    override suspend fun uploadImage(resource: ExternalResource): Image =
        throw UnsupportedOperationException("Cannot upload image to AnonymousMember")
}
