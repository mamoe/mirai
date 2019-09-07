package net.mamoe.mirai.event;

import lombok.Getter;
import lombok.Setter;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author NatrualHG
 */
public class MiraiEventHook<T extends MiraiEvent> implements Closeable {

    @Getter
    Class<T> eventClass;

    @Getter
    protected volatile Consumer<T> handler;

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
    protected Predicate<T> validChecker;

    public MiraiEventHook(Class<T> eventClass) {
        this(eventClass,null);
    }

    public MiraiEventHook(Class<T> eventClass, Consumer<T> handler){
        this.eventClass = eventClass;
        this.handler(handler);
    }

    public MiraiEventHook<T> handler(Consumer<T> handler) {
        this.handler = handler;
        return this;
    }

    public MiraiEventHook<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    public MiraiEventHook<T> ignoreCancelled(boolean ignoreCancelled) {
        this.ignoreCancelled = ignoreCancelled;
        return this;
    }


    private MiraiEventHook<T> setValidChecker(Predicate<T> validChecker) {
        this.validChecker = validChecker;
        return this;
    }

    public MiraiEventHook<T> setValidUntil(Predicate<T> valid) {
        return this.setValidChecker(valid);
    }

    public MiraiEventHook<T> setValidWhile(Predicate<T> valid) {
        return this.setValidChecker(valid.negate());
    }


    @SuppressWarnings("unchecked")
    public boolean accept(MiraiEvent event) {
        if(!(event instanceof Cancellable && event.isCancelled() && this.isIgnoreCancelled())){
            this.getHandler().accept((T) event);
        }
        return this.validChecker == null || this.validChecker.test((T) event);
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
        this.validChecker = null;
    }
}
