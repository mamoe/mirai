/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.internal.test.AbstractTest
import kotlin.test.Test

internal class BotFactoryTest : AbstractTest() {

    @Test
    fun `inlined newBot in Kotlin`() {
        runBlocking {
            BotFactory.newBot(123, "") {
                inheritCoroutineContext()
            }.close()
        }
    }
}