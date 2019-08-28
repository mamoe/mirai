package net.mamoe.mirai.event;

import net.mamoe.mirai.MiraiServer;
import net.mamoe.mirai.event.events.MiraiEvent;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MiraiEventManager {
    private MiraiEventManager(){

    }

    private static MiraiEventManager instance;

    public static MiraiEventManager getInstance(){
        if(MiraiEventManager.instance == null){
            MiraiEventManager.instance = new MiraiEventManager();
        }
        return MiraiEventManager.instance;
    }

    Lock hooksLock = new ReentrantLock();
    private Map<Class<? extends MiraiEvent>, List<MiraiEventHook<? extends MiraiEvent>>> hooks = new HashMap<>();

    public <D extends MiraiEvent> void hookUntil(MiraiEventHook<D> hook, Predicate<D> toRemove){
        this.mountHook(hook.setValidUntil(toRemove));
    }

    public <D extends MiraiEvent> void hookWhile(MiraiEventHook<D> hook, Predicate<D> toKeep){
        this.mountHook(hook.setValidWhile(toKeep));
    }

    public <D extends MiraiEvent> void hookOnce(MiraiEventHook<D> hook){
        this.hookUntil(hook,(a) -> true);
    }

    public <D extends MiraiEvent> void registerHook(MiraiEventHook<D> hook){
       this.mountHook(hook);
    }

    private <D extends MiraiEvent> void mountHook(MiraiEventHook<D> hook){
        if(!hook.isMount()) {
            hook.setMount(true);
            hooksLock.lock();
            hooks.putIfAbsent(hook.getEventClass(), new ArrayList<>());
            hooks.get(hook.getEventClass()).add(hook);
            hooksLock.unlock();
        }
    }

    /**
     * 不推荐onEvent
     * 由于不能保证Hook的原子性 非线程安全
     * 不能保证下一个 D event发生时handler就位
     * @author NaturalHG Aug27
     * use {@link MiraiEventHook::onEvent()} to replace
     */

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEvent(Class<D> event){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.registerHook(hook);
        return hook;
    }

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEventOnce(Class<D> event){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.hookOnce(hook);
        return hook;
    }

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEventUntil(Class<D> event, Predicate<D> toRemove){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.hookUntil(hook,toRemove);
        return hook;
    }

    @Deprecated
    public <D extends MiraiEvent> MiraiEventHook<D> onEventWhile(Class<D> event, Predicate<D> toKeep){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.hookWhile(hook,toKeep);
        return hook;
    }




    public void broadcastEvent(MiraiEvent event){
        hooksLock.lock();
        if(hooks.containsKey(event.getClass())){
            hooks.put(event.getClass(),
                    hooks.get(event.getClass())
                    .stream()
                    .sorted(Comparator.comparingInt(MiraiEventHook::getPriority))
                    .filter(a -> !a.accept(event))
                    .collect(Collectors.toList())
            );
        }
        hooksLock.unlock();
    }


    public void ansycBroadcastEvent(MiraiEvent event){
        this.ansycBroadcastEvent(event,a -> {});
    }

    public <D extends MiraiEvent> void ansycBroadcastEvent(D event, Consumer<D> callback){
        MiraiServer.getInstance().getTaskManager().ansycTask(() -> {
            MiraiEventManager.this.broadcastEvent(event);
            return event;
        },callback);
    }


}



