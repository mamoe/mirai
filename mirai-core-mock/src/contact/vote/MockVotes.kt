/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact.vote

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.vote.*
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * @since 2.15
 */
public interface MockVotes : Votes {

    /**
     * 直接以 [actor] 的身份推送一则投票
     *
     * @param events 当为 `true` 时会广播相关事件
     * @param vote 见 [OfflineVote], [OfflineVote.create]
     */
    @MockBotDSL
    public fun mockPublish(
        vote: Vote,
        actor: NormalMember,
        events: Boolean
    ): Vote

    @MockBotDSL
    public fun mockPublish(
        vote: Vote,
        actor: NormalMember,
    ): Vote = mockPublish(vote, actor, false)


    /**
     * 直接以 [actor] 的身份做一次投票
     *
     * @param fid 见 [Vote.fid]
     */
    @MockBotDSL
    public fun mockPublish(
        fid: String,
        options: List<Int>,
        actor: NormalMember
    ): VoteRecord
}

public class MockVote @MiraiInternalApi public constructor(
    override val publisherId: Long,
    override val fid: String,
    override val publicationTime: Long,
    override val description: VoteDescription,
    override val options: List<VoteOption>,
    override val records: List<VoteRecord>,
) : Vote {

    override lateinit var group: Group

    override val endTime: Long = publicationTime + description.durationSeconds

    override suspend fun refresh(): Boolean {
        return true
    }

    override suspend fun refreshed(): Vote? {
        return MockVote(publisherId, fid, publicationTime, description, options, records)
    }

    override val publisher: NormalMember? get() = group[publisherId]
}

public class MockVoteRecordImpl @MiraiInternalApi public constructor(
    private val vote: MockVote,
    override val voterId: Long,
    override val selectedOptions: List<VoteOption>,
    override val time: Long
) : VoteRecord {
    override val voter: NormalMember? by lazy { vote.group[voterId] }
}