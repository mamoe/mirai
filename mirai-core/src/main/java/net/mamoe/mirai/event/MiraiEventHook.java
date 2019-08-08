package net.mamoe.mirai.event;

import lombok.Getter;
import net.mamoe.mirai.event.events.MiraiEvent;

import java.util.function.Consumer;

public class MiraiEventHook<T extends MiraiEvent> {

    @Getter
    Class<T> eventClass;

    @Getter
    private Consumer<T> handler;
    @Getter
    private MiraiHookPreferences preferences;


    public MiraiEventHook(Class<T> eventClass) {
        this(eventClass,a -> {});
    }

    public MiraiEventHook(Class<T> eventClass, Consumer<T> handler){
        this(eventClass,handler,new MiraiHookPreferences());
    }

    public MiraiEventHook(Class<T> eventClass, Consumer<T> handler, MiraiHookPreferences preferences){
        this.eventClass = eventClass;
        this.setHandler(handler);
        this.setPreferences(preferences);
    }

    public MiraiEventHook<T> setHandler(Consumer<T> handler){
        this.handler = handler;
        return this;
    }

    public MiraiEventHook<T> setPreferences(MiraiHookPreferences preferences){
        this.preferences = preferences;
        return this;
    }



}
