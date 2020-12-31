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

@LowLevelApi
public interface FriendInfo : UserInfo {
    public override val uin: Long

    public override val nick: String

    public override val remark: String
}

@LowLevelApi
public open class FriendInfoImpl(
    override val uin: Long,
    override var nick: String,
    override var remark: String,
) : FriendInfo