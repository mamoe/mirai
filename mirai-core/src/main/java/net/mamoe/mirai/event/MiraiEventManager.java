package net.mamoe.mirai.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.mamoe.mirai.event.events.Cancellable;
import net.mamoe.mirai.event.events.MiraiEvent;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

    public <D extends MiraiEvent> void registerUntil(MiraiEventHook<D> hook, Predicate<D> toRemove){
        hooksLock.lock();
        hooks.putIfAbsent(hook.getEventClass(),new ArrayList<>());
        hooks.get(hook.getEventClass()).add(hook.setValidUntil(toRemove));
        hooksLock.unlock();
    }

    public <D extends MiraiEvent> void registerWhile(MiraiEventHook<D> hook, Predicate<D> toKeep){
        hooksLock.lock();
        hooks.putIfAbsent(hook.getEventClass(),new ArrayList<>());
        hooks.get(hook.getEventClass()).add(hook.setValidWhile(toKeep));
        hooksLock.unlock();
    }

    public <D extends MiraiEvent> void registerOnce(MiraiEventHook<D> hook){
        this.registerUntil(hook,(a) -> true);
    }

    public <D extends MiraiEvent> void register(MiraiEventHook<D> hook){
        this.registerUntil(hook,(a) -> false);
    }

    public <D extends MiraiEvent> MiraiEventHook<D> onEvent(Class<D> event){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.register(hook);
        return hook;
    }

    public <D extends MiraiEvent> MiraiEventHook<D> onEventOnce(Class<D> event){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.registerOnce(hook);
        return hook;
    }

    public <D extends MiraiEvent> MiraiEventHook<D> onEventUntil(Class<D> event, Predicate<D> toRemove){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.registerUntil(hook,toRemove);
        return hook;
    }

    public <D extends MiraiEvent> MiraiEventHook<D> onEventWhile(Class<D> event, Predicate<D> toKeep){
        MiraiEventHook<D> hook = new MiraiEventHook<>(event);
        this.registerWhile(hook,toKeep);
        return hook;
    }



    public void boardcastEvent(MiraiEvent event){
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

}


