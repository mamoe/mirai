package net.mamoe.mirai.event.events;

/**
 * @author NaturalHG
 */
public interface Cancellable {
    boolean isCancelled();

    void cancel(boolean forceCancel);

    void cancel();
}
