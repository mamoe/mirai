package net.mamoe.mirai.event;

import net.mamoe.jpre.event.Cancellable;
import net.mamoe.mirai.utils.EventException;

public abstract class MiraiEvent {

    private boolean cancelled;

    public boolean isCancelled() {
        if (!(this instanceof Cancellable)) {
            throw new EventException("Event is not Cancellable");
        }
        return this.cancelled;
    }

    public void setCancelled() {
        setCancelled(true);
    }

    public void setCancelled(boolean value) {
        if (!(this instanceof Cancellable)) {
            throw new EventException("Event is not Cancellable");
        }
        this.cancelled = value;
    }

}
