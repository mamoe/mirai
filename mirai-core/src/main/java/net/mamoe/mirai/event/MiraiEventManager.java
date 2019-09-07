package net.mamoe.mirai.event;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 线程安全的事件管理器.
 *
 * @author NaturalHG
 * @see MiraiEventManagerKt 若你使用 kotlin, 请查看针对 kotlin 的优化实现
 */
public class MiraiEventManager {
    MiraiEventManager() {

    }

    public static MiraiEventManager getInstance() {
        return EventManager.INSTANCE;//实例来自 kotlin 的 singleton
    }

    private final ReentrantReadWriteLock hooksLock = new ReentrantReadWriteLock();
    private Map<Class<? extends MiraiEvent>, List<MiraiEventHook<? extends MiraiEvent>>> hooks = new HashMap<>();

    public <D extends MiraiEvent> void hookUntil(MiraiEventHook<D> hook, Predicate<D> toRemove) {
        this.mountHook(hook.setValidUntil(toRemove));
    }

    public <D extends MiraiEvent> void hookWhile(MiraiEventHook<D> hook, Predicate<D> toKeep) {
        this.mountHook(hook.setValidWhile(toKeep));
    }

    public <D extends MiraiEvent> void hookAlways(MiraiEventHook<D> hook) {
        this.hookUntil(hook, (a) -> false);
    }

    public <D extends MiraiEvent> void hookOnce(MiraiEventHook<D> hook) {
        this.hookUntil(hook, (a) -> true);
    }

    public <D extends MiraiEvent> void registerHook(MiraiEventHook<D> hook) {
        this.mountHook(hook);
    }

    private <D extends MiraiEvent> void mountHook(MiraiEventHook<D> hook) {
        if (!hook.isMount()) {
            hook.setMount(true);
            hooksLock.writeLock().lock();
            try {
                if (!hooks.containsKey(hook.getEventClass())) {
                    hooks.put(hook.getEventClass(), new LinkedList<>() {{
                        add(hook);
                    }});
                } else {
                    hooks.get(hook.getEventClass()).add(hook);
                }
            } finally {
                hooksLock.writeLock().unlock();
            }
        }
    }

    /**
     * 不推荐onEvent
     * 由于不能保证Hook的原子性 非线程安全
     * 不能保证下一个 D event发生时handler就位
     *
     * @author NaturalHG Aug27
     * use {@link MiraiEventHook::onEvent()} to replace
     */

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEvent(Class<D> event) {
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.registerHook(hook);
        return hook;
    }

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEventOnce(Class<D> event) {
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.hookOnce(hook);
        return hook;
    }

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEventUntil(Class<D> event, Predicate<D> toRemove) {
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.hookUntil(hook, toRemove);
        return hook;
    }

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEventWhile(Class<D> event, Predicate<D> toKeep) {
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.hookWhile(hook, toKeep);
        return hook;
    }


    public void broadcastEvent(MiraiEvent event) {
        hooksLock.readLock().lock();
        try {
            if (hooks.containsKey(event.getClass())) {
                hooks.put(event.getClass(),
                        hooks.get(event.getClass())
                                .stream()
                                .sorted(Comparator.comparingInt(MiraiEventHook::getPriority))
                                .filter(a -> !a.accept(event))
                                .collect(Collectors.toList())
                );
            }
        } finally {
            hooksLock.readLock().unlock();
        }
    }


    public <E extends AsyncEvent> CompletableFuture<E> broadcastEventAsync(E event) {
        Objects.requireNonNull(event);
        if (!(event instanceof MiraiEvent)) {
            throw new IllegalArgumentException("event must be instanceof MiraiEvent");
        }

        CompletableFuture<E> future = new CompletableFuture<>();
        future.completeAsync(() -> {
            MiraiEventManager.this.broadcastEvent((MiraiEvent) event);
            return event;
        });
        return future;
    }

    public <E extends AsyncEvent> CompletableFuture<E> broadcastEventAsync(E event, Consumer<E> callback) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(callback);
        if (!(event instanceof MiraiEvent)) {
            throw new IllegalArgumentException("event must be instanceof MiraiEvent");
        }

        CompletableFuture<E> future = new CompletableFuture<>();
        future.whenComplete((a, b) -> callback.accept(event));
        future.completeAsync(() -> {
            MiraiEventManager.this.broadcastEvent((MiraiEvent) event);
            return event;
        });
        return future;
    }


    public <D extends AsyncEvent> CompletableFuture<D> broadcastEventAsync(D event, Runnable callback) {
        return broadcastEventAsync(event, t -> callback.run());
    }
}



