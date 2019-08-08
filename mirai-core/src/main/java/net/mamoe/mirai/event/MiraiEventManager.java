package net.mamoe.mirai.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MiraiEventManager {
    private MiraiEventManager(){

    }

    private static MiraiEventManager instance;

    static MiraiEventManager getInstance(){
        if(MiraiEventManager.instance == null){
            MiraiEventManager.instance = new MiraiEventManager();
        }
        return MiraiEventManager.instance;
    }

    private Map<Class<? extends MiraiEvent>, List<MiraiEventConsumer<? extends MiraiEvent>>> hooks = new HashMap<>();

    public <D extends MiraiEvent> void registerUntil(MiraiEventHook<D> hook, Predicate<D> toRemove){
        hooks.putIfAbsent(hook.getEventClass(),new ArrayList<>());
        hooks.get(hook.getEventClass()).add(new MiraiEventConsumer<>(hook,toRemove));
    }

    public <D extends MiraiEvent> void registerOnce(MiraiEventHook<D> hook){
        this.registerUntil(hook,(a) -> true);
    }

    public <D extends MiraiEvent> void register(MiraiEventHook<D> hook){
        this.registerUntil(hook,(a) -> false);
    }


    public void boardcastEvent(MiraiEvent event){
        if(hooks.containsKey(event.getClass())){
            hooks.put(event.getClass(),
                    hooks.get(event.getClass())
                    .stream()
                    .sorted(Comparator.comparingInt(MiraiEventConsumer::getPriority))
                    .dropWhile(a -> a.accept(event))
                    .collect(Collectors.toList())
            );
        }
    }

}
@Data
@AllArgsConstructor
class MiraiEventConsumer<T extends MiraiEvent>{
    private MiraiEventHook<T> hook;
    private Predicate<T> remove;


    public int getPriority(){
        return hook.getPreferences().getPriority();
    }

    @SuppressWarnings("unchecked")
    public boolean accept(MiraiEvent event) {
        if(!(event instanceof Cancelable && event.isCancelled() && hook.getPreferences().isIgnoreCanceled())){
            hook.getHandler().accept((T) event);
        }
        return remove.test((T)event);
    }
}

