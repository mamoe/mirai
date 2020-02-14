/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.japt;

import kotlinx.coroutines.GlobalScope;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.internal.EventInternalJvmKt;
import net.mamoe.mirai.japt.internal.EventsImplKt;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Events {

    @NotNull
    public static <E extends Event> Listener<E> subscribe(@NotNull Class<E> eventClass, @NotNull Function<E, ListeningStatus> onEvent) {
        return EventInternalJvmKt._subscribeEventForJaptOnly(eventClass, GlobalScope.INSTANCE, onEvent);
    }

    @NotNull
    public static <E extends Event> Listener<E> subscribeAlways(@NotNull Class<E> eventClass, @NotNull Consumer<E> onEvent) {
        return EventInternalJvmKt._subscribeEventForJaptOnly(eventClass, GlobalScope.INSTANCE, onEvent);
    }

    @NotNull
    public static <E extends Event> E broadcast(@NotNull E event) {
        return EventsImplKt.broadcast(event);
    }
}