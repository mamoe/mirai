/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.contact.active.MemberMedalInfo
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.internal.contact.active.GroupActiveImpl
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl

internal class MemberActiveImpl(private val info: MemberInfoImpl, private val group: GroupImpl) : MemberActive {
    override val rank: Int get() = info.rank
    override val point: Int get() = info.point
    override val honors: Set<GroupHonorType> get() = info.honors
    override val temperature: Int get() = info.temperature
    override suspend fun queryMedal(): MemberMedalInfo {
        return (group.active as GroupActiveImpl).queryMemberMedal(uid = info.uin)
    }
}