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
 * 活跃数据记录
 */
public class ActiveRecord internal constructor(
    /**
     * 发言者名称
     */
    public val senderName: String,

    /**
     * 发言者 ID
     */
    public val senderId: Long,

    /**
     * 发言者的群员实例
     */
    public val sender: NormalMember?,

    /**
     * 活跃连续天数
     */
    public val continuation: Int,

    /**
     * 发言条数
     */
    public val sentences: Int
)