/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.active

import net.mamoe.mirai.contact.NormalMember

/**
 * 群荣耀当前持有者
 * @property memberName 群员昵称
 * @property memberId 群员 ID
 * @property avatar 群员头像
 * @property member 群员实例
 * @property termDays 当前蝉联天数
 * @property historyDays 历史获得天数
 * @property maxTermDays 最大蝉联天数
 * @since 2.13
 */
public class ActiveHonorInfo internal constructor(
    public val memberName: String,
    public val memberId: Long,
    public val avatar: String,
    public val member: NormalMember?,
    public val termDays: Int,
    public val historyDays: Int,
    public val maxTermDays: Int,
)