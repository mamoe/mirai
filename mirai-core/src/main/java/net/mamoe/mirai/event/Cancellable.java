package net.mamoe.mirai.event;

/**
 * @author NaturalHG
 */
public interface Cancellable {
    boolean isCancelled();

    void cancel(boolean forceCancel);

    void cancel();
}
