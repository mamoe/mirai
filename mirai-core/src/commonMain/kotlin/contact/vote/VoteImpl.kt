/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.vote

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.vote.*
import net.mamoe.mirai.internal.asQQAndroidBot

internal class VoteImpl(
    override val group: Group,
    override val fid: String,
) : Vote {
    override val url: String = "https://client.qun.qq.com/qqweb/m/qun/vote/detail.html?fid=${fid}&groupuin=${group.id}"


    override var publisherId: Long = 0L
    override var publisher: NormalMember? = null
    override var publicationTime: Long = 0L
    override var endTime: Long = 0L
    override lateinit var description: VoteDescription
    override lateinit var options: List<VoteOption>
    override lateinit var records: List<VoteRecord>

    override suspend fun refresh(): Boolean {
        val data = bot.asQQAndroidBot().getGroupVote(groupCode = group.id, fid = fid)
        val detail = data.detail ?: return false
        updateFrom(data, detail)
        return true
    }

    override suspend fun refreshed(): Vote? {
        return VoteImpl(group, fid)
            .takeIf { it.refresh() }
    }

    fun updateFrom(data: GroupVote, detail: GroupVoteDetail) {
        publicationTime = data.published
        endTime = detail.end
        publisherId = data.uid
        publisher = group[data.uid]

        description = description.builder().apply {
            title = detail.title.text
            detail.options.forEach { option(it.content.text) }
            availableVotes = detail.capacity
            isAnonymous = detail.anonymous == 1
            durationSeconds = detail.end - data.published
            if (data.settings.remindTs != 0L) {
                remindSeconds = data.settings.remindTs - data.published
            }
            image = detail.title.pictures.firstOrNull()?.toPublic()
        }.build()

        records = detail.results.map { result ->
            VoteRecordImpl(
                group,
                voterId = result.uid,
                selectedOptions = result.options.mapNotNull { options.getOrNull(it) },
                time = result.time,
            )
        }

        options = detail.options.mapIndexed { index, option ->
            VoteOptionImpl(
                this,
                index,
                option.content.text,
                option.selected,
            )
        }
    }

    override fun toString(): String {
        return "Vote(" +
                "fid='$fid', " +
                "publisher=${publisher ?: publisherId}, " +
                "publicationTime=$publicationTime, " +
                "endTime=$endTime, " +
                "description=$description, " +
                "options=$options, " +
                "records=$records" +
                ")"
    }
}

