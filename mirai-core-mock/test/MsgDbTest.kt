/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test

import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.mock.database.MessageDatabase
import net.mamoe.mirai.mock.database.MessageInfo
import net.mamoe.mirai.mock.database.mockMsgDatabaseId
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

internal class MsgDbTest {
    @Test
    fun testIdConversion() {
        repeat(50) {
            val id1 = Random.nextInt()
            val id2 = Random.nextInt()
            val msgInfo = MessageInfo(
                mixinedMsgId = mockMsgDatabaseId(id1, id2),
                sender = 0, subject = 0, kind = MessageSourceKind.FRIEND, time = 0,
                messageChainOf()
            )
            assertEquals(id1, msgInfo.id)
            assertEquals(id2, msgInfo.internal)
        }
    }

    @Test
    fun testDatabase() {
        val db = MessageDatabase.newDefaultDatabase()
        db.connect()

        repeat(90) {
            val info = db.newMessageInfo(Random.nextLong(), Random.nextLong(), MessageSourceKind.FRIEND, 0, messageChainOf())
            assertEquals(info, db.queryMessageInfo(info.mixinedMsgId))
        }

        db.disconnect()
    }
}
