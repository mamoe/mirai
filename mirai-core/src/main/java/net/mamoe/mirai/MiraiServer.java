package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;
import net.mamoe.mirai.event.events.server.ServerEnableEvent;
import net.mamoe.mirai.network.Network;
import net.mamoe.mirai.task.MiraiTaskManager;
import net.mamoe.mirai.utils.LoggerTextFormat;
import net.mamoe.mirai.utils.MiraiLogger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class MiraiServer {
    @Getter
    private static MiraiServer instance;

    @Getter //mirai version
    private final static String miraiVersion = "1.0.0";

    @Getter //is running under UNIX
    private boolean unix;

    @Getter//file path
    private File parentFolder;

    @Getter
    MiraiEventManager eventManager;
    @Getter
    MiraiTaskManager taskManager;
    @Getter
    MiraiLogger logger;


    protected MiraiServer(){
        instance = this;
        this.onLoad();
        Thread.yield();
        this.onEnable();
    }

    private boolean enabled;

    protected void shutdown(){
        if(this.enabled) {
            this.getLogger().log(LoggerTextFormat.SKY_BLUE + "About to shutdown Mirai");
            this.getEventManager().boardcastEvent(new ServerDisableEvent());
            this.getLogger().log(LoggerTextFormat.SKY_BLUE + "Data have been saved");
        }
    }




    private void onLoad(){
        this.parentFolder = new File(System.getProperty("user.dir"));
        this.unix = !System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");

        this.logger = MiraiLogger.getInstance();
        this.eventManager = MiraiEventManager.getInstance();
        this.taskManager = MiraiTaskManager.getInstance();

        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "About to run Mirai (" + MiraiServer.getMiraiVersion() + ") under " + (isUnix()?"unix":"windows") );
        this.getLogger().log("Loading data under " + LoggerTextFormat.GREEN + this.parentFolder);

        
        /*
        try {
            Network.start(Network.getAvailablePort());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
           this.shutdown();
        }

           */

    }

    private void onEnable(){
        this.eventManager.boardcastEvent(new ServerEnableEvent());
        this.enabled = true;
    }




}
