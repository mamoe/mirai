/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.utils.MiraiExperimentalApi

@OptIn(MiraiExperimentalApi::class)
@LowLevelApi
public interface StrangerInfo : UserInfo {
    /**
     * 陌生人的QQ号码
     */
    public override val uin: Long

    /**
     * 陌生人的昵称
     *
     */
    public override val nick: String

    /**
     * 陌生人来源的群
     *
     * 当不是来源于群时为0
     */
    public val fromGroup: Long
}
