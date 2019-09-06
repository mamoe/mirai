package net.mamoe.mirai.event;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 实现这个接口的事件可以被异步执行或阻塞执行
 *
 * @author Him188moe
 * @see AsyncEventKt 若你使用 kotlin, 请查看针对 kotlin 的优化实现
 */
public interface AsyncEvent {

    default CompletableFuture<? extends AsyncEvent> broadcastAsync() {
        return MiraiEventManager.getInstance().broadcastEventAsync(this);
    }

    @SuppressWarnings("unchecked")
    default <E extends AsyncEvent> CompletableFuture<E> broadcastEventAsync(Consumer<E> callback) {
        return MiraiEventManager.getInstance().broadcastEventAsync((E) this, callback);
    }

    @SuppressWarnings("unchecked")
    default <E extends AsyncEvent> CompletableFuture<E> broadcastEventAsync(Runnable callback) {
        return MiraiEventManager.getInstance().broadcastEventAsync((E) this, callback);
    }
}
