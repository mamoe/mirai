/*
 * Copyright 2020 Mamoe Technologies and contributors.
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.javatest;

import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import net.mamoe.mirai.event.*;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleListenerHostTest {
    @Test
    public void testJavaSimpleListenerHostWork() {
        AtomicBoolean called = new AtomicBoolean();
        final SimpleListenerHost host = new SimpleListenerHost() {
            @EventHandler
            public void testListen(
                    AbstractEvent event
            ) {
                System.out.println(event);
                called.set(true);
            }
        };
        CoroutineScope scope = CoroutineScopeKt.CoroutineScope(EmptyCoroutineContext.INSTANCE);
        Events.registerEvents(scope, host);
        EventKt.broadcast(new AbstractEvent() {
        });
        if (!called.get()) {
            throw new AssertionError("JavaTest: SimpleListenerHost Failed.");
        }
    }
}
