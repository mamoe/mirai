package net.mamoe.mirai.event;

import net.mamoe.mirai.MiraiServer;
import net.mamoe.mirai.event.events.MiraiEvent;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MiraiEventManager {
    private MiraiEventManager() {

    }

    private static MiraiEventManager instance = new MiraiEventManager();

    public static MiraiEventManager getInstance() {
        return MiraiEventManager.instance;
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
            if (!hooks.containsKey(hook.getEventClass())) {
                hooks.put(hook.getEventClass(), new LinkedList<>() {{
                    add(hook);
                }});
            } else {
                hooks.get(hook.getEventClass()).add(hook);
            }
            hooksLock.writeLock().unlock();
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
        if (hooks.containsKey(event.getClass())) {
            hooks.put(event.getClass(),
                    hooks.get(event.getClass())
                            .stream()
                            .sorted(Comparator.comparingInt(MiraiEventHook::getPriority))
                            .filter(a -> !a.accept(event))
                            .collect(Collectors.toList())
            );
        }
        hooksLock.readLock().unlock();
    }


    public void asyncBroadcastEvent(MiraiEvent event) {
        this.asyncBroadcastEvent(event, a -> {
        });
    }

    public <D extends MiraiEvent> void asyncBroadcastEvent(D event, Consumer<D> callback) {
        MiraiServer.getInstance().getTaskManager().ansycTask(() -> {
            MiraiEventManager.this.broadcastEvent(event);
            return event;
        }, callback);
    }


    public <D extends MiraiEvent> void asyncBroadcastEvent(D event, Runnable callback) {
        asyncBroadcastEvent(event, t -> callback.run());
    }

}



