package net.mamoe.mirai.event.events;

public interface Cancellable {


    boolean isCancelled();

    void cancel(boolean forceCancel);

    void cancel();
}
