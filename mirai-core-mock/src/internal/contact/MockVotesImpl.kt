/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.vote.OnlineVote
import net.mamoe.mirai.contact.vote.OnlineVoteStatus
import net.mamoe.mirai.contact.vote.Vote
import net.mamoe.mirai.contact.vote.VoteImage
import net.mamoe.mirai.mock.contact.vote.MockVotes
import net.mamoe.mirai.utils.ExternalResource

internal class MockVotesImpl(
    val group: Group
) : MockVotes {
    override suspend fun publish(vote: Vote): OnlineVote {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(resource: ExternalResource): VoteImage {
        TODO("Not yet implemented")
    }

    override suspend fun delete(fid: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun get(fid: String): OnlineVoteStatus? {
        TODO("Not yet implemented")
    }

    override fun asFlow(): Flow<Vote> {
        TODO("Not yet implemented")
    }
}