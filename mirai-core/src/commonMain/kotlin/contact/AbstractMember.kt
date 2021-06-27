/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.utils.cast
import kotlin.coroutines.CoroutineContext

internal abstract class AbstractMember(
    final override val group: GroupImpl,
    parentCoroutineContext: CoroutineContext,
    memberInfo: MemberInfo,
) : AbstractUser(group.bot, parentCoroutineContext, memberInfo), Member {
    final override val info: MemberInfoImpl = memberInfo.cast()

    override val nameCard: String get() = info.nameCard
    override val specialTitle: String get() = info.specialTitle
    override var permission: MemberPermission by info::permission
}