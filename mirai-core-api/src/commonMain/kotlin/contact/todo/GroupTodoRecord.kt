/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.todo

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.utils.MiraiInternalApi

public class GroupTodoRecord @MiraiInternalApi constructor(
    public val group: Group,
    public val title: String,
    public val operator: NormalMember?,
    public val operatorId: Long,
    public val operatorNick: String,
    public val operatorTime: Int,
    @MiraiInternalApi public val msgSeq: Long,
    @MiraiInternalApi public val msgRandom: Long
) {
    override fun toString(): String {
        return "GroupTodoRecord(title=${title}, operator=${operatorId}, group=${group.id}, time=${operatorTime})"
    }
}