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

    override suspend fun publish(vote: Vote): OnlineVote {
        val result = group.bot.publishGroupVote(groupCode = group.id, vote = vote).check()

        return OnlineVoteImpl(
            group = group,
            senderId = bot.id,
            sender = group.botAsMember,
            fid = result.fid,
            publicationTime = result.ltsm,
            endTime = result.ltsm + vote.parameters.end,
            title = vote.title,
            options = vote.options,
            select = emptyList(),
            parameters = vote.parameters
        )
    }

    override suspend fun uploadImage(resource: ExternalResource): VoteImage {
        return tryServersUpload(
            bot,
            listOf("client.qun.qq.com" to 80),
            resource.size,
            ResourceKind.ANNOUNCEMENT_IMAGE,
            ChannelKind.HTTP
        ) { _, _ ->
            // use common logging

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

    override suspend fun get(fid: String): OnlineVoteStatus? {
        val data = bot.getGroupVote(groupCode = group.id, fid = fid)
        if (data.detail == null) return null

        val impl = OnlineVoteImpl(
            group = group,
            senderId = data.uid,
            sender = group[data.uid],
            fid = data.fid,
            publicationTime = data.published,
            endTime = data.detail.end,
            title = data.detail.title.text,
            options = data.detail.options.map { it.content.text },
            select = data.detail.options.map { it.selected },
            parameters = buildVoteParameters {
                capacity = data.detail.capacity
                anonymous = data.detail.anonymous == 1
                end = data.detail.end - data.published
                // XXX: 提醒时间没有返回
                // remind = 0
                image = data.detail.title.pictures.firstOrNull()?.toPublic()
            }
        )
        return OnlineVoteStatus(
            vote = impl,
            records = data.detail.results.map { result ->
                OnlineVoteRecordImpl(
                    vote = impl,
                    voterId = result.uid,
                    voter = group[result.uid],
                    options = result.options,
                    time = result.time
                )
            }
        )
    }

    override fun asFlow(): Flow<Vote> {
        return flow {
            var i = 1
            while (true) {
                val result = bot.getGroupVoteList(groupCode = group.id, page = i++)
                if (result.votes.isEmpty()) break

                for (info in result.votes) {
                    val impl = OnlineVoteImpl(
                        group = group,
                        senderId = info.uid,
                        sender = group[info.uid],
                        fid = info.fid,
                        publicationTime = info.published,
                        endTime = info.detail.end,
                        title = info.detail.title.text,
                        options = info.detail.options.map { it.content.text },
                        select = info.detail.options.map { it.selected },
                        parameters = buildVoteParameters {
                            capacity = info.detail.capacity
                            anonymous = info.detail.anonymous == 1
                            end = info.detail.end - info.published
                            if (info.settings.remindTs != 0L) {
                                remind = info.settings.remindTs - info.published
                            }
                            image = info.detail.title.pictures.firstOrNull()?.toPublic()
                        }
                    )
                    emit(impl)
                }
            }
        }
    }
}