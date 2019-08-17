package net.mamoe.mirai;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;
import net.mamoe.mirai.event.events.server.ServerEnableEvent;
import net.mamoe.mirai.network.Protocol;
import net.mamoe.mirai.network.Robot;
import net.mamoe.mirai.task.MiraiTaskManager;
import net.mamoe.mirai.utils.LoggerTextFormat;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.config.MiraiConfig;
import net.mamoe.mirai.utils.config.MiraiMapSection;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
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
            getLogger().info(LoggerTextFormat.SKY_BLUE + "About to shutdown Mirai");
            this.getEventManager().boardcastEvent(new ServerDisableEvent());
            getLogger().info(LoggerTextFormat.SKY_BLUE + "Data have been saved");
        }

    }


    private void onLoad(){
        this.parentFolder = new File(System.getProperty("user.dir"));
        this.unix = !System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");

        this.logger = MiraiLogger.INSTANCE;
        this.eventManager = MiraiEventManager.getInstance();
        this.taskManager = MiraiTaskManager.getInstance();

        getLogger().info(LoggerTextFormat.SKY_BLUE + "About to run Mirai (" + MiraiServer.MIRAI_VERSION + ") under " + (isUnix() ? "unix" : "windows"));
        getLogger().info("Loading data under " + LoggerTextFormat.GREEN + this.parentFolder);

        File setting = new File(this.parentFolder + "/Mirai.ini");

        if(!setting.exists()){
            this.initSetting(setting);
        }else {
            this.setting = new MiraiConfig(setting);
        }

        /*
        MiraiMapSection qqs = this.setting.getMapSection("qq");
        qqs.forEach((a,p) -> {
            this.getLogger().info(LoggerTextFormat.SKY_BLUE + "Finding available ports between " + "1-65536");
            try {
                int port = MiraiNetwork.getAvailablePort();
                this.getLogger().info(LoggerTextFormat.SKY_BLUE + "Listening on port " + port);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        */

        Robot robot = new Robot(1994701021L);
        try {
            robot.connect(Protocol.Companion.getSERVER_IP().get(2), 8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
/*
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
        }*/
    }

    public void initSetting(File setting){
        getLogger().info(LoggerTextFormat.SKY_BLUE + "Thanks for using Mirai");
        getLogger().info(LoggerTextFormat.SKY_BLUE + "initializing Settings");
        try {
            if(setting.createNewFile()){
                getLogger().info(LoggerTextFormat.SKY_BLUE + "Mirai Config Created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setting = new MiraiConfig(setting);
        MiraiMapSection network  = this.setting.getMapSection("network");

        MiraiMapSection qqs = this.setting.getMapSection("qq");
        Scanner scanner = new Scanner(System.in);
        getLogger().info(LoggerTextFormat.SKY_BLUE + "input one " + LoggerTextFormat.RED + " QQ number " + LoggerTextFormat.SKY_BLUE + "for default robot");
        getLogger().info(LoggerTextFormat.SKY_BLUE + "输入用于默认机器人的QQ号");
        long qqNumber = scanner.nextLong();
        getLogger().info(LoggerTextFormat.SKY_BLUE + "input the password for that QQ account");
        getLogger().info(LoggerTextFormat.SKY_BLUE + "输入该QQ号对应密码");
        String qqPassword = scanner.next();
        getLogger().info(LoggerTextFormat.SKY_BLUE + "initialized; changing can be made in config file: " + setting.toString());
        qqs.put(String.valueOf(qqNumber),qqPassword);
        this.setting.save();
    }

    private void onEnable(){
        this.eventManager.boardcastEvent(new ServerEnableEvent());
        this.enabled = true;
        getLogger().info(LoggerTextFormat.GREEN + "Server enabled; Welcome to Mirai");
        getLogger().info("Mirai Version=" + MiraiServer.MIRAI_VERSION + " QQ Version=" + MiraiServer.QQ_VERSION);
    }


}
