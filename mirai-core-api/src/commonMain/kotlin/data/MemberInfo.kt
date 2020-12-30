/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.MemberPermission

@LowLevelApi
public interface MemberInfo : UserInfo {
    public val nameCard: String

    public val permission: MemberPermission

    public val specialTitle: String

    public val muteTimestamp: Int

    public val anonymousId: String? get() = null
}