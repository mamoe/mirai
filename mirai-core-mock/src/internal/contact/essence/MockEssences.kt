/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.essence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.essence.EssenceMessageRecord
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.mock.contact.essence.MockEssences
import net.mamoe.mirai.mock.internal.contact.MockGroupImpl
import net.mamoe.mirai.utils.ConcurrentHashMap
import net.mamoe.mirai.utils.currentTimeSeconds

internal class MockEssencesImpl(
    private val group: MockGroupImpl
) : MockEssences {

    private val cache: MutableMap<MessageSource, EssenceMessageRecord> = ConcurrentHashMap()

    override fun mockSetEssences(source: MessageSource, actor: NormalMember) {
        val record = EssenceMessageRecord(
            group = group,
            sender = group[source.fromId],
            senderId = source.fromId,
            senderNick = group[source.fromId]?.nick.orEmpty(),
            senderTime = source.time,
            operator = actor,
            operatorId = actor.id,
            operatorNick = actor.nick,
            operatorTime = currentTimeSeconds().toInt(),
            loadMessageSource = { source }
        )
        cache[source] = record
    }

    override suspend fun getPage(start: Int, limit: Int): List<EssenceMessageRecord> {
        return cache.values.toList().subList(start, start + limit)
    }

    override suspend fun share(source: MessageSource): String {
        return "https://qun.qq.com/essence/share?_wv=3&_wwv=128&_wvx=2&sharekey=..."
    }

    override suspend fun remove(source: MessageSource) {
        cache.remove(source)
    }

    override fun asFlow(): Flow<EssenceMessageRecord> {
        return cache.values.asFlow()
    }
}