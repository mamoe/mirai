/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.MemberActive
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.data.MemberInfo

internal class MemberActiveImpl(private val memberInfo: MemberInfo) : MemberActive {
    override val rank: Int get() = memberInfo.rank
    override val point: Int get() = memberInfo.point
    override val honors: Set<GroupHonorType> get() = memberInfo.honor
    override val temperature: Int get() = memberInfo.temperature
}