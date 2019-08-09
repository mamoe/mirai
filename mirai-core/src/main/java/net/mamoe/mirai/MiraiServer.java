package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;
import net.mamoe.mirai.event.events.server.ServerEnableEvent;
import net.mamoe.mirai.network.Network;
import net.mamoe.mirai.task.MiraiTaskManager;

import java.io.IOException;

public class MiraiServer {
    @Getter
    private static MiraiServer instance;

    protected MiraiServer(){
        instance = this;

        this.onLoad();

        Thread.yield();

        this.onEnable();
    }

    private boolean enabled;

    protected void shutdown(){
        if(this.enabled) {
            this.getEventManager().boardcastEvent(new ServerDisableEvent());
        }
    }


    @Getter
    MiraiEventManager eventManager;
    @Getter
    MiraiTaskManager taskManager;


    private void onLoad(){

        this.eventManager = MiraiEventManager.getInstance();
        this.taskManager = MiraiTaskManager.getInstance();

        try {
            Network.start(Network.getAvailablePort());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
           this.shutdown();
        }


    }

    private void onEnable(){
        this.eventManager.boardcastEvent(new ServerEnableEvent());
        this.enabled = true;
    }




}
