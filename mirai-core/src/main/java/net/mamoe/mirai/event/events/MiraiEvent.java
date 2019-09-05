package net.mamoe.mirai.event.events;

import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.utils.EventException;

import java.util.function.Consumer;

public abstract class MiraiEvent {

    private boolean cancelled;

    public boolean isCancelled() {
        if (!(this instanceof Cancellable)) {
            return false;
        }
        return this.cancelled;
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean value) {
        if (!(this instanceof Cancellable)) {
            throw new EventException("Event is not Cancellable");
        }
        this.cancelled = value;
    }

    protected String eventName;

    public String getEventName() {
        if (this.eventName == null) {
            return this.getClass().getSimpleName();
        }
        return this.eventName;
    }

    public final MiraiEvent broadcast() {
        MiraiEventManager.getInstance().broadcastEvent(this);
        return this;
    }

    @SuppressWarnings("unchecked")
    public final <D extends MiraiEvent> void asyncBroadcast(Consumer<D> callback) {
        MiraiEventManager.getInstance().asyncBroadcastEvent((D) this, callback);
    }

    @SuppressWarnings("unchecked")
    public final <D extends MiraiEvent> void asyncBroadcast(Runnable callback) {
        MiraiEventManager.getInstance().asyncBroadcastEvent((D) this, callback);
    }
}
