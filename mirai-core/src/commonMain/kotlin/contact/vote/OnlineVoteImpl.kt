/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.vote

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.vote.OnlineVote
import net.mamoe.mirai.contact.vote.OnlineVoteRecord
import net.mamoe.mirai.contact.vote.VoteParameters

internal class OnlineVoteImpl(
    override val group: Group,
    override val senderId: Long,
    override val sender: NormalMember?,
    override val fid: String,
    override val publicationTime: Long,
    override val endTime: Long,
    override var title: String,
    override var options: List<String>,
    override var counts: List<Int>,
    override var records: List<OnlineVoteRecord>,
    override val parameters: VoteParameters
) : OnlineVote {

    override val url: String = "https://client.qun.qq.com/qqweb/m/qun/vote/detail.html?fid=${fid}&groupuin=${group.id}"
}