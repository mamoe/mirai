package net.mamoe.mirai.event;

import net.mamoe.mirai.utils.EventException;

/**
 * @author NatrualHG
 * @see AsyncEvent
 */
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

    public final MiraiEvent broadcast() {
        MiraiEventManager.getInstance().broadcastEvent(this);
        return this;
    }
}
