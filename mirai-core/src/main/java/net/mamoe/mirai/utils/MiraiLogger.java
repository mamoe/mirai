package net.mamoe.mirai.utils;

import net.mamoe.mirai.MiraiServer;
import net.mamoe.mirai.task.MiraiTaskManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MiraiLogger {

    private static MiraiLogger instance;

    public static MiraiLogger getInstance(){
        if(MiraiLogger.instance == null){
            MiraiLogger.instance = new MiraiLogger();
        }
        return MiraiLogger.instance;
    }


    public void log(Object o){
        this.log(o.toString());
    }

    public void log(String s){
        System.out.println("[" + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + "] Mirai: " + s + LoggerTextFormat.RESET);
    }

}


