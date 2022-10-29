/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.essence

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import net.mamoe.mirai.contact.essence.EssenceMessageRecord
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiLogger

internal expect class EssencesImpl(
    group: GroupImpl,
    logger: MiraiLogger,
) : CommonEssencesImpl

internal abstract class CommonEssencesImpl(
    protected val group: GroupImpl,
    protected val logger: MiraiLogger,
) : Essences {

    private fun source(message: DigestMessage): MessageSource {
        return group.bot.buildMessageSource(MessageSourceKind.GROUP) {
            ids = intArrayOf(message.msgSeq)
            internalIds = intArrayOf(message.msgRandom)
            time = message.senderTime

            fromId = message.senderUin
            targetId = group.id

            messages(message.msgContent.map { content ->
                when (content.msgType) {
                    1 -> PlainText(content.text)
                    3 -> {
                        // TODO: image url -> md5 -> image_id
                        content.imageUrl
                        Image("")
                    }
                    else -> {
                        // XXX:
                        emptyMessageChain()
                    }
                }
            })
        }
    }

    private fun record(message: DigestMessage): EssenceMessageRecord {
        return EssenceMessageRecord(
            group = group,
            sender = group[message.senderUin],
            senderId = message.senderUin,
            senderNick = message.senderNick,
            senderTime = message.senderTime,
            operator = group[message.addDigestUin],
            operatorId = message.addDigestUin,
            operatorNick = message.addDigestNick,
            operatorTime = message.addDigestTime,
            source = source(message = message)
        )
    }

    override suspend fun page(start: Int, limit: Int): List<EssenceMessageRecord> {
        val page = group.bot.getDigestList(
            groupCode = group.id,
            pageStart = start,
            pageLimit = limit
        )

        return page.messages.map(this::record)
    }

    override suspend fun add(source: MessageSource) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(source: MessageSource) {
        TODO("Not yet implemented")
    }

    override fun asFlow(): Flow<MessageSource> {
        return flow {
            var offset = 0
            while (currentCoroutineContext().isActive) {
                val page = group.bot.getDigestList(
                    groupCode = group.id,
                    pageStart = offset,
                    pageLimit = 30
                )
                for (message in page.messages) {
                    emit(source(message))
                }
                if (page.isEnd) break
                if (page.messages.isEmpty()) break
                offset += 30
            }
        }
    }
}