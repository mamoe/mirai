package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;
import net.mamoe.mirai.event.events.server.ServerEnableEvent;
import net.mamoe.mirai.network.MiraiNetwork;
import net.mamoe.mirai.network.MiraiUDPClient;
import net.mamoe.mirai.network.MiraiUDPServer;
import net.mamoe.mirai.task.MiraiTaskManager;
import net.mamoe.mirai.utils.LoggerTextFormat;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.config.MiraiConfig;
import net.mamoe.mirai.utils.config.MiraiMapSection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

public class MiraiServer {
    @Getter
    private static MiraiServer instance;

    //mirai version
    private final static String MIRAI_VERSION = "1.0.0";

    //qq version
    private final static String QQ_VERSION = "4.9.0";


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

    MiraiConfig setting;


    protected MiraiServer(){
        instance = this;
        this.onLoad();
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

        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "About to run Mirai (" + MiraiServer.MIRAI_VERSION + ") under " + (isUnix()?"unix":"windows") );
        this.getLogger().log("Loading data under " + LoggerTextFormat.GREEN + this.parentFolder);

        File setting = new File(this.parentFolder + "/Mirai.ini");

        if(!setting.exists()){
            this.initSetting(setting);
        }else {
            this.setting = new MiraiConfig(setting);
        }

        /*
        MiraiMapSection qqs = this.setting.getMapSection("qq");
        qqs.forEach((a,p) -> {
            this.getLogger().log(LoggerTextFormat.SKY_BLUE + "Finding available ports between " + "1-65536");
            try {
                int port = MiraiNetwork.getAvailablePort();
                this.getLogger().log(LoggerTextFormat.SKY_BLUE + "Listening on port " + port);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        */

        System.out.println("network test");
        try {
            MiraiUDPServer server = new MiraiUDPServer();
            MiraiUDPClient client = new MiraiUDPClient(InetAddress.getLocalHost(),9999,MiraiNetwork.getAvailablePort());
            this.getTaskManager().repeatingTask(() -> {
                byte[] sendInfo = "test test".getBytes(StandardCharsets.UTF_8);
                try {
                    client.send(new DatagramPacket(sendInfo,sendInfo.length));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            },300);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initSetting(File setting){
        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "Thanks for using Mirai");
        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "initializing Settings");
        try {
            if(setting.createNewFile()){
                this.getLogger().log(LoggerTextFormat.SKY_BLUE + "Mirai Config Created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setting = new MiraiConfig(setting);
        MiraiMapSection network  = this.setting.getMapSection("network");

        MiraiMapSection qqs = this.setting.getMapSection("qq");
        Scanner scanner = new Scanner(System.in);
        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "input one "  + LoggerTextFormat.RED + " QQ number " + LoggerTextFormat.SKY_BLUE  +"for default robot");
        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "输入用于默认机器人的QQ号");
        long qqNumber = scanner.nextLong();
        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "input the password for that QQ account");
        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "输入该QQ号对应密码");
        String qqPassword = scanner.next();
        this.getLogger().log(LoggerTextFormat.SKY_BLUE + "initialized; changing can be made in config file: " + setting.toString());
        qqs.put(String.valueOf(qqNumber),qqPassword);
        this.setting.save();
    }

    private void onEnable(){
        this.eventManager.boardcastEvent(new ServerEnableEvent());
        this.enabled = true;
        this.getLogger().log(LoggerTextFormat.GREEN + "Server enabled; Welcome to Mirai");
        this.getLogger().log( "Mirai Version=" + MiraiServer.MIRAI_VERSION + " QQ Version=" + MiraiServer.QQ_VERSION);
    }


}
