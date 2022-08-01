/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.childScope
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JvmMethodEventsTestJava : AbstractEventTest() {
    private val called = AtomicInteger(0)

    @Test
    fun test() = runTest {
        val host = TestHost(called)
        val scope = this.childScope()
        scope.globalEventChannel().registerListenerHost(host)
        TestEvent().broadcast()
        assertEquals(3, called.get(), null)
        host.cancel() // reset listeners
        scope.cancel()
    }
}

@Suppress(
    "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RedundantVisibilityModifier", "UNUSED_PARAMETER", "unused",
    "RedundantNullableReturnType"
)
@net.mamoe.mirai.utils.EventListenerLikeJava
internal class TestHost(
    private val called: AtomicInteger
) : SimpleListenerHost() {
    @EventHandler
    public fun ev(event: TestEvent?) {
        called.incrementAndGet()
    }

    @EventHandler
    public fun ev2(event: TestEvent?): Void? {
        called.incrementAndGet()
        return null
    }

    @EventHandler
    public fun ev3(event: TestEvent?): ListeningStatus? {
        called.incrementAndGet()
        return ListeningStatus.LISTENING
    }

}
