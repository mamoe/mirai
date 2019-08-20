package net.mamoe.mirai.event;

import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.events.Cancellable;
import net.mamoe.mirai.event.events.MiraiEvent;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MiraiEventHook<T extends MiraiEvent> implements Closeable {

    @Getter
    Class<T> eventClass;

    @Getter
    private volatile Consumer<T> handler;

    @Getter
    private volatile int priority = 0;

    @Getter
    private volatile boolean ignoreCancelled = true;

    @Getter
    @Setter
    private volatile boolean mount = false;

    /**
     * return true -> this hook need to be removed
     */
    @Getter
    private Predicate<T> valid;

    public MiraiEventHook(Class<T> eventClass) {
        this(eventClass,null);
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
        return this.valid == null || this.valid.test((T) event);
    }

    /**
     * 更加安全高效的方式
     * Remember to use {@link this.mount()} at last
     * */

    public static <D extends MiraiEvent> MiraiEventHook<D> onEvent(Class<D> event){
        return new MiraiEventHook<>(event);
    }

    public void mount(){
        if(this.handler == null)this.handler = a -> {};
        MiraiEventManager.getInstance().registerHook(this);
    }

    public void mountOnce(){
        if(this.handler == null)this.handler = a -> {};
        MiraiEventManager.getInstance().hookOnce(this);
    }


    @Override
    public void close(){
        this.handler = null;
        this.valid = null;
    }
}
