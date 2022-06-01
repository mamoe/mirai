/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertIs

internal class EventChannelFlowTest : AbstractEventTest() {

    @Test
    fun asFlow(): Unit = runBlockingUnit() {
        val channel = GlobalEventChannel
        val job = async(start = CoroutineStart.UNDISPATCHED) {
            channel.asFlow().first()
        }
        TestEvent().broadcast()
        assertIs<TestEvent>(job.await())
    }
}