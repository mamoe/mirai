package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.MiraiEvent;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;
import net.mamoe.mirai.event.events.server.ServerEnableEvent;
import net.mamoe.mirai.task.MiraiTaskManager;

public class MiraiServer {
    @Getter
    private static MiraiServer instance;

    protected MiraiServer(){
        instance = this;
        this.onLoad();
    }

    protected void shutdown(){
        this.getEventManager().boardcastEvent(new ServerDisableEvent());
    }


    @Getter
    MiraiEventManager eventManager;
    @Getter
    MiraiTaskManager taskManager;

    private void onLoad(){

        this.eventManager = MiraiEventManager.getInstance();
        this.taskManager = MiraiTaskManager.getInstance();

        this.eventManager.boardcastEvent(new ServerEnableEvent());
    }




}
