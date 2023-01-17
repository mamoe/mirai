/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.vote

import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.vote.OnlineVoteRecord
import net.mamoe.mirai.contact.vote.Vote

internal class OnlineVoteRecordImpl(
    override val vote: Vote,
    override val voterId: Long,
    override val voter: NormalMember?,
    override val options: List<Int>,
    override val time: Long
) : OnlineVoteRecord