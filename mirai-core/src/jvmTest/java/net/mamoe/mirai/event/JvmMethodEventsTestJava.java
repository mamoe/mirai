/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static kotlin.test.AssertionsKt.assertEquals;

public class JvmMethodEventsTestJava extends SimpleListenerHost {
    private final AtomicInteger called = new AtomicInteger(0);

    @EventHandler
    public void ev(TestEvent event) {
        called.incrementAndGet();
    }

    @EventHandler
    public Void ev2(TestEvent event) {
        called.incrementAndGet();
        return null;
    }

    @EventHandler
    public ListeningStatus ev3(TestEvent event) {
        called.incrementAndGet();
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    public void ev(TestEvent event, TestEvent event2) {
        called.incrementAndGet();
    }

    @EventHandler
    public Void ev2(TestEvent event, TestEvent event2) {
        called.incrementAndGet();
        return null;
    }

    @EventHandler
    public ListeningStatus ev3(TestEvent event, TestEvent event2) {
        called.incrementAndGet();
        return ListeningStatus.LISTENING;
    }

    @Test
    public void test() {
        Events.registerEvents(this);
        EventKt.broadcast(new TestEvent());
        assertEquals(6, called.get(), null);
    }
}