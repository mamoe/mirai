package net.mamoe.mirai.event;

import lombok.Getter;
import net.mamoe.mirai.event.events.Cancellable;
import net.mamoe.mirai.event.events.MiraiEvent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class MiraiEventHook<T extends MiraiEvent> {

    @Getter
    Class<T> eventClass;

    @Getter
    private Consumer<T> handler;

    @Getter
    private int priority = 0;

    @Getter
    private boolean ignoreCancelled = true;

    /**
     * return true -> this hook need to be removed
     */
    @Getter
    private Predicate<T> valid;

    public MiraiEventHook(Class<T> eventClass) {
        this(eventClass,a -> {});
    }

    public MiraiEventHook(Class<T> eventClass, Consumer<T> handler){
        this.eventClass = eventClass;
        this.setHandler(handler);
    }

    public MiraiEventHook<T> setHandler(Consumer<T> handler){
        this.handler = handler;
        return this;
    }

    public MiraiEventHook<T> setPriority(int priority){
        this.priority = priority;
        return this;
    }

    public MiraiEventHook<T> setIgnoreCancelled(boolean ignoreCancelled){
        this.ignoreCancelled = ignoreCancelled;
        return this;
    }


    private MiraiEventHook<T> setValid(Predicate<T> valid) {
        this.valid = valid;
        return this;
    }

    public MiraiEventHook<T> setValidUntil(Predicate<T> valid) {
        return this.setValid(valid);
    }

    public MiraiEventHook<T> setValidWhile(Predicate<T> valid) {
        return this.setValid(valid.negate());
    }


    @SuppressWarnings("unchecked")
    public boolean accept(MiraiEvent event) {
        if(!(event instanceof Cancellable && event.isCancelled() && this.isIgnoreCancelled())){
             this.getHandler().accept((T) event);
        }
        return this.valid.test((T)event);
    }


}
