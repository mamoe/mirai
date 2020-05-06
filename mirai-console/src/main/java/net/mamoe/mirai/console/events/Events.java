/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.events;

import kotlinx.coroutines.GlobalScope;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 事件处理
 */
public final class Events {
    /**
     * 阻塞地广播一个事件.
     *
     * @param event 事件
     * @param <E>   事件类型
     * @return {@code event} 本身
     */
    @NotNull
    public static <E extends Event> E broadcast(@NotNull E event) {
        return EventsImplKt.broadcast(event);
    }
}