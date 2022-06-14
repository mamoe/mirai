/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package  net.mamoe.mirai.message.data

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class MessageUtilsTest {
    @Test
    fun testIsContentEmpty() {
        assertTrue { emptyMessageChain().isContentEmpty() }
        assertTrue { buildMessageChain { }.isContentEmpty() }
        assertTrue { PlainText("").isContentEmpty() }
        assertTrue { (PlainText("") + PlainText("")).isContentEmpty() }
        assertTrue { buildMessageChain { append(PlainText("")); append(PlainText("")) }.isContentEmpty() }
    }

    @Test
    fun `flow toMessageChain`() = runTest {
        assertEquals(messageChainOf(PlainText("1")), flowOf(PlainText("1")).toMessageChain())
    }
}