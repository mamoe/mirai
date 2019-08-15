package net.mamoe.mirai.event.events;

import net.mamoe.mirai.utils.EventException;

public abstract class MiraiEvent {

    private boolean cancelled;

    public boolean isCancelled() {
        if (!(this instanceof Cancellable)) {
            throw new EventException("Event is not Cancellable");
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
        if(this.eventName == null){
            return this.getClass().getSimpleName();
        }
        return this.eventName;
    }


}
