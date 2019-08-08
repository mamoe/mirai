package net.mamoe.mirai.event;

import lombok.Getter;

interface Cancelable {


    boolean isCancelled();

    void setCancelled();

    void setCancelled(boolean forceCancel);
}
