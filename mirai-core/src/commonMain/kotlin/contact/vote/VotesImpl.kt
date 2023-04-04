/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.vote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.mamoe.mirai.contact.vote.*
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.highway.ChannelKind
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.highway.tryServersUpload
import net.mamoe.mirai.utils.*

internal class VotesImpl(
    val group: GroupImpl,
    val logger: MiraiLogger,
) : Votes {
    private val bot = group.bot

    override suspend fun publish(vote: VoteDescription): Vote {
        val result = group.bot.publishGroupVote(groupCode = group.id, vote = vote).check()

        return VoteImpl(
            group = group,
            fid = result.fid,
        ).apply {
            publisherId = bot.id
            publisher = group.botAsMember
            publicationTime = result.ltsm
            endTime = result.ltsm + vote.durationSeconds
            description = vote
            options = vote.options.mapIndexed { index, s -> VoteOptionImpl(this, index, s, 0) }
            records = emptyList()
        }
    }

    override suspend fun uploadImage(resource: ExternalResource): VoteImage {
        return tryServersUpload(
            bot,
            listOf("client.qun.qq.com" to 80),
            resource.size,
            ResourceKind.VOTE_IMAGE,
            ChannelKind.HTTP
        ) { _, _ ->
            val data = bot.uploadGroupVoteImage(groupCode = group.id, resource = resource).check()

            data.id.decodeHtmlEscape()
                .loadSafelyAs(GroupVotePicture.serializer()).check()
                .toPublic()
        }

    }

    override suspend fun delete(fid: String): Boolean {
        val info = bot.deleteGroupVote(groupCode = group.id, fid = fid)
        // 14 {"ec":14,"em":"0x8f9 response error, msg=No Feeds exist
        if (info.id == 14) return false
        info.check()
        return true
    }

    override suspend fun get(fid: String): Vote? = VoteImpl(group, fid).takeIf { it.refresh() }

    override fun asFlow(): Flow<Vote> {
        return flow {
            var i = 1
            while (true) {
                val result = bot.getGroupVoteList(groupCode = group.id, page = i++)
                if (result.votes.isEmpty()) break

                for (info in result.votes) {
                    val detail = info.detail ?: continue
                    val impl = VoteImpl(group = group, fid = info.fid).apply {
                        updateFrom(info, detail)
                    }
                    emit(impl)
                }
            }
        }
    }
}