/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * @see User
 * @see Bot
 *
 * @see ContactOrBot
 */
@NotStableForInheritance
public interface UserOrBot : ContactOrBot {
    /**
     * 获取昵称
     *
     * ### Kotlin 实用扩展:
     *
     * - [nameCardOrNick]: 若该用户是 [群成员][Member], 则优先返回其非空群名片, 否则返回 [nick].
     * - [remarkOrNameCardOrNick]: 若 [Bot] 对该用户有[备注][User.remark]则返回备注, 否则返回 [nameCardOrNick].
     *
     * @since 2.6
     */
    public val nick: String

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see Nudge.sendTo 发送这个戳一戳消息
     */
    public fun nudge(): Nudge
}