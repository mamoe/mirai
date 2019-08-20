package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;
import net.mamoe.mirai.event.events.server.ServerEnableEvent;
import net.mamoe.mirai.network.Robot;
import net.mamoe.mirai.network.packet.client.touch.ClientTouchPacket;
import net.mamoe.mirai.task.MiraiTaskManager;
import net.mamoe.mirai.utils.LoggerTextFormat;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.config.MiraiConfig;
import net.mamoe.mirai.utils.config.MiraiConfigSection;
import net.mamoe.mirai.utils.setting.MiraiSetting;
import net.mamoe.mirai.utils.setting.MiraiSettingListSection;
import net.mamoe.mirai.utils.setting.MiraiSettingMapSection;

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

    MiraiSetting setting;

    MiraiConfig qqs;


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
        getLogger().info("Selecting setting from " + LoggerTextFormat.GREEN + setting);

        if(!setting.exists()){
            this.initSetting(setting);
        }else {
            this.setting = new MiraiSetting(setting);
        }

        File qqs = new File(this.parentFolder + "/QQ.yml");
        getLogger().info("Reading QQ accounts from  " + LoggerTextFormat.GREEN + qqs);
        if(!qqs.exists()){
            this.initQQConfig(qqs);
        }else {
            this.qqs = new MiraiConfig(qqs);
        }
        if(this.qqs.isEmpty()){
            this.initQQConfig(qqs);
        }

        /*
        MiraiSettingMapSection qqs = this.setting.getMapSection("qq");
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

        getLogger().info("ready to connect");

        Robot robot = new Robot(1994701021, "xiaoqqq");
        try {
            //System.out.println(Protocol.Companion.getSERVER_IP().get(3));
            //System.out.println(Protocol.Companion.getSERVER_IP().toString());

            robot.setServerIP("14.116.136.106");
            robot.sendPacket(new ClientTouchPacket(1994701021, "14.116.136.106"));
            while (true) ;
            //robot.connect("14.116.136.106");
            //robot.connect(Protocol.Companion.getSERVER_IP().get(2));
            //robot.connect("125.39.132.242");
        } catch (Exception e) {
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

    private void initSetting(File setting) {
        getLogger().info(LoggerTextFormat.SKY_BLUE + "Thanks for using Mirai");
        getLogger().info(LoggerTextFormat.SKY_BLUE + "initializing Settings");
        try {
            if(setting.createNewFile()){
                getLogger().info(LoggerTextFormat.SKY_BLUE + "Mirai Config Created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setting = new MiraiSetting(setting);
        MiraiSettingMapSection network  = this.setting.getMapSection("network");
        network.set("enable_proxy","not supporting yet");

        MiraiSettingListSection proxy  = this.setting.getListSection("proxy");
        proxy.add("1.2.3.4:95");
        proxy.add("1.2.3.4:100");

        MiraiSettingMapSection worker  = this.setting.getMapSection("worker");
        worker.set("core_task_pool_worker_amount",5);

        MiraiSettingMapSection plugin = this.setting.getMapSection("plugin");
        plugin.set("debug", false);

        this.setting.save();
        getLogger().info(LoggerTextFormat.SKY_BLUE + "initialized; changing can be made in setting file: " + setting.toString());
    }

    private void initQQConfig(File qqConfig){
        this.qqs = new MiraiConfig(qqConfig);

        MiraiConfigSection<Object> section = new MiraiConfigSection<>();

        System.out.println("/");
        Scanner scanner = new Scanner(System.in);
        getLogger().info(LoggerTextFormat.SKY_BLUE + "input one " + LoggerTextFormat.RED + " QQ number " + LoggerTextFormat.SKY_BLUE + "for default robot");
        getLogger().info(LoggerTextFormat.SKY_BLUE + "输入用于默认机器人的QQ号");
        long qqNumber = scanner.nextLong();
        getLogger().info(LoggerTextFormat.SKY_BLUE + "input the password for that QQ account");
        getLogger().info(LoggerTextFormat.SKY_BLUE + "输入该QQ号对应密码");
        String qqPassword = scanner.next();

        section.put("password",qqPassword);
        section.put("owner","default");

        this.qqs.put(String.valueOf(qqNumber),section);
        this.qqs.save();
        getLogger().info(LoggerTextFormat.SKY_BLUE + "QQ account initialized; changing can be made in Config file: " + qqConfig.toString());
    }

    private void onEnable(){
        this.eventManager.boardcastEvent(new ServerEnableEvent());
        this.enabled = true;
        getLogger().info(LoggerTextFormat.GREEN + "Server enabled; Welcome to Mirai");
        getLogger().info("Mirai Version=" + MiraiServer.MIRAI_VERSION + " QQ Version=" + MiraiServer.QQ_VERSION);
    }


}
