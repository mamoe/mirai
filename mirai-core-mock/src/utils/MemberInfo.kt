/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.utils

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.data.MemberInfo

public fun simpleMemberInfo(
    uin: Long,
    name: String,
    nick: String = name,
    nameCard: String = "",
    remark: String = "",
    permission: MemberPermission,
    specialTitle: String = "",
): MemberInfo {
    return object : MemberInfo {
        override val nameCard: String get() = nameCard
        override val permission: MemberPermission get() = permission
        override val specialTitle: String get() = specialTitle
        override val muteTimestamp: Int get() = 0
        override val joinTimestamp: Int get() = 0
        override val lastSpeakTimestamp: Int get() = 0
        override val isOfficialBot: Boolean get() = false
        override val rank: Int
            get() = 0
        override val point: Int
            get() = 0
        override val honors: Set<GroupHonorType>
            get() = setOf()
        override val temperature: Int
            get() = 0
        override val uin: Long get() = uin
        override val nick: String get() = nick
        override val remark: String get() = remark
    }
}
