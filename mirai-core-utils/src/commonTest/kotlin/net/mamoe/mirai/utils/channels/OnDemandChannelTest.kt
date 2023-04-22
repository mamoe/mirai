/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.channels

import kotlinx.coroutines.*
import kotlin.test.*


class OnDemandChannelTest {
    ///////////////////////////////////////////////////////////////////////////
    // CoroutineScope lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun attachScopeJob() {
        val job = SupervisorJob()
        val channel = OnDemandChannel<Int, Int>(job) {
            fail()
        }
        assertEquals(1, job.children.toList().size)
        channel.finish()
    }

    @Test
    fun finishAfterInstantiation() {
        val supervisor = SupervisorJob()
        val channel = OnDemandChannel<Int, Int>(supervisor) {
            fail("ran")
        }
        assertEquals(1, supervisor.children.toList().size)
        val job = supervisor.children.single()
        assertEquals(true, job.isActive)

        channel.finish()

        assertEquals(0, supervisor.children.toList().size)
        assertEquals(false, job.isActive)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Producer Coroutine — Lazy Initialization
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `producer coroutine won't start until expectMore`() {
        val channel = OnDemandChannel<Int, Int> {
            fail()
        }
        channel.finish()
    }

    @Test
    fun `producer coroutine starts iff expectMore`() = runBlocking(Dispatchers.Default.limitedParallelism(1)) {
        var started = false
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            // (1)
            assertEquals(false, started)
            started = true
            yield() // goto (2)
            fail()
        }
        assertFalse { started }
        channel.expectMore(1) // launches the job, but it won't execute due to single parallelism
        yield() // goto (1)
        // (2)
        assertTrue { started }
        channel.finish()
    }
}
