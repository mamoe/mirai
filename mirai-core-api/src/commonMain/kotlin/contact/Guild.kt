/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.utils.NotStableForInheritance

@NotStableForInheritance
public interface Guild : Contact, CoroutineScope, FileSupported, AudioSupported {
    /**
     * 频道名称.
     */
    public var name: String
    /**
     * 同为 guildCode, 用户看到的频道号码.
     */
    public override val id: Long

    /**
     * 频道封面图片地址 https://groupprocover-76483.picgzc.qpic.cn/
     */
    public var coverUrl : String
    /**
     * 频道头像图片地址 https://groupprohead-76292.picgzc.qpic.cn/
     */
    public override var avatarUrl : String

//    public var channelNodes : List<Channel>
}