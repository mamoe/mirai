/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package test

import net.mamoe.mirai.utils.InsertLink
import kotlin.test.Test
import kotlin.test.assertTrue

object InsertLinkTest {
    fun InsertLink<String>.str(): String {
        return joinToString(", ")
    }

    @Test
    fun testInsertLink() {
        val link = InsertLink<String>()
        var node = link.head
        node = node.insertAfter("1")
        assertTrue { link.str() == "1" }
        node.insertBefore("Before")
        node = node.insertAfter("After")
        assertTrue { link.str() == "Before, 1, After" }
        node.insertBefore("Before0")
        assertTrue { link.str() == "Before, 1, Before0, After" }
    }
}