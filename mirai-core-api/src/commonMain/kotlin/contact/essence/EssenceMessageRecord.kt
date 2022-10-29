/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.essence

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.message.data.MessageSource

public class EssenceMessageRecord(
    public val group: Group,
    public val sender: NormalMember?,
    public val senderId: Long,
    public val senderNick: String,
    public val senderTime: Int,
    public val operator: NormalMember?,
    public val operatorId: Long,
    public val operatorNick: String,
    public val operatorTime: Int,
    public val source: MessageSource
)