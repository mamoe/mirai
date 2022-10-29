/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.essence

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.essence.EssenceMessageRecord
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.mock.contact.essence.MockEssences
import net.mamoe.mirai.mock.internal.contact.MockGroupImpl
import java.util.stream.Stream

internal class MockEssencesImpl(
    private val group: MockGroupImpl
) : MockEssences {

    override suspend fun page(start: Int, limit: Int): List<EssenceMessageRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun add(source: MessageSource) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(source: MessageSource) {
        TODO("Not yet implemented")
    }

    override fun asFlow(): Flow<MessageSource> {
        TODO("Not yet implemented")
    }

    override fun asStream(): Stream<MessageSource> {
        TODO("Not yet implemented")
    }
}