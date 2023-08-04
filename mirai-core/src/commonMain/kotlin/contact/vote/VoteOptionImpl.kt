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
import net.mamoe.mirai.contact.vote.Vote
import net.mamoe.mirai.contact.vote.VoteOption

internal class VoteOptionImpl(
    vote: Vote,
    override val index: Int,
    override val name: String,
    override val totalVotes: Int,
) : VoteOption {
    override val voterIds: List<Long> by lazy {
        vote.records
            .filter { record -> record.selectedOptions.any { it.index == index } }
            .map { it.voterId }
    }

    override val voters: List<NormalMember> by lazy {
        voterIds.mapNotNull { vote.group[it] }
    }

    override fun toString(): String {
        return "VoteOptionImpl(index=$index, name='$name', totalVotes=$totalVotes, voterIds=$voterIds)"
    }
}