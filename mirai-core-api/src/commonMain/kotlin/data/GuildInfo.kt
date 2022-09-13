/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.LowLevelApi
/**
 * 频道资料.
 */
@LowLevelApi
public interface GuildInfo {
    /**
     * 频道名称.
     */
    public val name: String
    /**
     * id, 用户看不到的频道号码.
     */
    public  val id: Long

    /**
     * guildCode
     */
    public  val code: Long

    /**
     * 频道封面图片地址 https://groupprocover-76483.picgzc.qpic.cn/
     */
    public val coverUrl : String
    /**
     * 频道头像图片地址 https://groupprohead-76292.picgzc.qpic.cn/
     */
    public val avatarUrl : String

}