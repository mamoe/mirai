package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.MiraiEvent;
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
    }

    boolean isEnabled;

    protected void shutdown(){
        if(this.isEnabled) {
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
            Network.start(Network.getAvaliablePort());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
           this.shutdown();
        }

        this.eventManager.boardcastEvent(new ServerEnableEvent());
        this.isEnabled = true;
    }




}
