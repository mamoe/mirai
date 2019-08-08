package net.mamoe.mirai.event.events;

import lombok.Getter;

public interface Cancellable {


    boolean isCancelled();

    void setCancelled();

    void setCancelled(boolean forceCancel);
}
