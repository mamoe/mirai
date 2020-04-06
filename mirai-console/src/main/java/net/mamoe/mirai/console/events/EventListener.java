package net.mamoe.mirai.console.events;

import kotlinx.coroutines.GlobalScope;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class EventListener {

    PluginBase base;

    public EventListener(
            PluginBase base
    ){
        this.base = base;
    }

    /**
     * 监听一个事件, 当 {@code onEvent} 返回 {@link ListeningStatus#STOPPED} 时停止监听.
     * 机器人离线后不会停止监听.
     *
     * @param eventClass 事件类
     * @param onEvent    事件处理. 返回 {@link ListeningStatus#LISTENING} 时继续监听.
     * @param <E>        事件类型
     * @return 事件监听器. 可调用 {@link Listener#complete()} 或 {@link Listener#completeExceptionally(Throwable)} 让监听正常停止或异常停止.
     */
    @NotNull
    public <E extends Event> Listener<E> subscribe(@NotNull Class<E> eventClass, @NotNull Function<E, ListeningStatus> onEvent) {
        return EventsImplKt.subscribeEventForJaptOnly(eventClass, base, onEvent);
    }


    /**
     * 监听一个事件, 直到手动停止.
     * 机器人离线后不会停止监听.
     *
     * @param eventClass 事件类
     * @param onEvent    事件处理. 返回 {@link ListeningStatus#LISTENING} 时继续监听.
     * @param <E>        事件类型
     * @return 事件监听器. 可调用 {@link Listener#complete()} 或 {@link Listener#completeExceptionally(Throwable)} 让监听正常停止或异常停止.
     */
    @NotNull
    public <E extends Event> Listener<E> subscribeAlways(@NotNull Class<E> eventClass, @NotNull Consumer<E> onEvent) {
         return EventsImplKt.subscribeEventForJaptOnly(eventClass, base, onEvent);
    }

}
