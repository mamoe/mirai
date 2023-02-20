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
    ): OnlineVote

    @MockBotDSL
    public fun mockPublish(
        vote: Vote,
        actor: NormalMember,
    ): OnlineVote = mockPublish(vote, actor, false)


    /**
     * 直接以 [actor] 的身份做一次投票
     *
     * @param fid 见 [OnlineVote.fid]
     */
    @MockBotDSL
    public fun mockPublish(
        fid: String,
        options: List<Int>,
        actor: NormalMember
    ): OnlineVoteRecord
}

public class MockOnlineVote @MiraiInternalApi public constructor(
    override val senderId: Long,
    override val fid: String,
    override val publicationTime: Long,
    override val select: MutableList<Int>,
    override var records: List<OnlineVoteRecord>,
    override val title: String,
    override val options: List<String>,
    override val parameters: VoteParameters
) : OnlineVote {

    override lateinit var group: Group

    override val endTime: Long = publicationTime + parameters.end

    override val sender: NormalMember? get() = group[senderId]
}

public class MockOnlineVoteRecordImpl @MiraiInternalApi public constructor(
    override val vote: MockOnlineVote,
    override val voterId: Long,
    override val options: List<Int>,
    override val time: Long
) : OnlineVoteRecord {

    override val voter: NormalMember? get() = vote.group[voterId]
}