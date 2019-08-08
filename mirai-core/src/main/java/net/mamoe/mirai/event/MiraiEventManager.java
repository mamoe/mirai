package net.mamoe.mirai.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

}
@Data
@AllArgsConstructor
class MiraiEventConsumer<T extends MiraiEvent>{
    private MiraiEventHook<T> hook;
    private Predicate<T> remove;
}

