/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.vote.OnlineVote
import net.mamoe.mirai.contact.vote.OnlineVoteRecord
import net.mamoe.mirai.contact.vote.Vote
import net.mamoe.mirai.contact.vote.VoteImage
import net.mamoe.mirai.mock.contact.vote.MockOnlineVote
import net.mamoe.mirai.mock.contact.vote.MockOnlineVoteRecordImpl
import net.mamoe.mirai.mock.contact.vote.MockVotes
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.currentTimeSeconds
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class MockVotesImpl(
    val group: Group
) : MockVotes {

    val votes = ConcurrentHashMap<String, MockOnlineVote>()

    override fun mockPublish(vote: Vote, actor: NormalMember, events: Boolean): OnlineVote {
        val mock = MockOnlineVote(
            senderId = actor.id,
            parameters = vote.parameters,
            fid = UUID.randomUUID().toString(),
            options = emptyList(),
            counts = MutableList(vote.options.size) { 0 },
            records = emptyList(),
            title = vote.title,
            publicationTime = currentTimeSeconds()
        )
        mock.group = group
        if (!events) return mock

        // TODO: mirai-core no other events about votes
        return mock
    }

    override fun mockPublish(fid: String, options: List<Int>, actor: NormalMember): OnlineVoteRecord {
        val mock = votes[fid] ?: throw IllegalStateException("Vote was delete")
        val record = MockOnlineVoteRecordImpl(
            vote = mock,
            voterId = actor.id,
            options = options,
            time = currentTimeSeconds()
        )
        options.forEach { index ->
            mock.counts[index] = mock.counts[index] + 1
        }

        return record
    }

    override suspend fun publish(vote: Vote): OnlineVote {
        return mockPublish(vote = vote, actor = group.botAsMember, events = true)
    }

    override suspend fun uploadImage(resource: ExternalResource): VoteImage {
        return VoteImage.create(group.bot.mock().uploadMockImage(resource).imageId, 500, 500, "")
    }

    override suspend fun delete(fid: String): Boolean {
        return votes.remove(fid) != null
    }

    override suspend fun update(vote: OnlineVote) {
        val mock = votes[vote.fid] ?: throw IllegalStateException("Vote was delete")
        // TODO
    }

    override suspend fun get(fid: String): OnlineVote? {
        return votes[fid]
    }

    override fun asFlow(): Flow<Vote> {
        return votes.values.asFlow()
    }
}