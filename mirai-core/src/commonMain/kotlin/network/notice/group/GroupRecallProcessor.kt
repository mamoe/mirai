/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.group

import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.proto.TroopTips0x857
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.utils.mapToIntArray

internal class GroupRecallProcessor : MixedNoticeProcessor() {
    override suspend fun PipelineContext.processImpl(data: MsgType0x2DC) {
        val (_, group, buf) = data

        val proto = buf.loadAs(TroopTips0x857.NotifyMsgBody.serializer(), 1)

        val recallReminder = proto.optMsgRecall ?: return
        val operator = group[recallReminder.uin] ?: return
        markAsConsumed()
        for (firstPkg in recallReminder.recalledMsgList) {
            if (firstPkg.authorUin == bot.id && operator.id == bot.id) continue // already broadcast
            val author = group[firstPkg.authorUin] ?: continue

            collected += MessageRecallEvent.GroupRecall(
                bot = bot,
                authorId = firstPkg.authorUin,
                messageIds = recallReminder.recalledMsgList.mapToIntArray { it.seq },
                messageInternalIds = recallReminder.recalledMsgList.mapToIntArray { it.msgRandom },
                messageTime = firstPkg.time,
                operator = operator,
                group = group,
                author = author,
            )
        }
    }
}