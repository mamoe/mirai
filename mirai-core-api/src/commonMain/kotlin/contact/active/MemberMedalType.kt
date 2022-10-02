/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.active

import net.mamoe.mirai.contact.active.MemberMedalType.*
import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 群成员头衔详情Detail
 * @property OWNER 群主独有的头衔
 * @property ADMIN 管理员独有的头衔
 * @property SPECIAL 群主授予的头衔
 * @property ACTIVE 群主设定的头衔, 保持活跃即可获得
 * @since 2.13
 */
public enum class MemberMedalType(@MiraiInternalApi public val mask: Int) {
    OWNER(300),
    ADMIN(301),
    SPECIAL(302),
    ACTIVE(315);
}