package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;
import net.mamoe.mirai.event.events.server.ServerEnableEvent;
import net.mamoe.mirai.network.RobotNetworkHandler;
import net.mamoe.mirai.network.packet.login.LoginState;
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
    private static MiraiServer instance;

    public static MiraiServer getInstance() {
        return instance;
    }

    //mirai version
    private final static String MIRAI_VERSION = "1.0.0";

    //qq version
    private final static String QQ_VERSION = "4.9.0";


    @Getter //is running under UNIX
    private boolean unix;

    @Getter//file path
    public File parentFolder;

    @Getter
    MiraiEventManager eventManager;
    @Getter
    MiraiTaskManager taskManager;

    @Getter
    MiraiLogger logger;

    MiraiSetting setting;

    MiraiConfig qqs;


    protected MiraiServer() {
        instance = this;
        this.onLoad();
        this.onEnable();
    }

    private boolean enabled;

    protected void shutdown() {
        if (this.enabled) {
            getLogger().info("About to shutdown Mirai");
            this.getEventManager().broadcastEvent(new ServerDisableEvent());
            getLogger().info("Data have been saved");
        }

    }


    private void onLoad() {
        this.parentFolder = new File(System.getProperty("user.dir"));
        this.unix = !System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");

        this.logger = MiraiLogger.INSTANCE;
        this.eventManager = MiraiEventManager.getInstance();
        this.taskManager = MiraiTaskManager.getInstance();

        getLogger().info("About to run Mirai (" + MiraiServer.MIRAI_VERSION + ") under " + (isUnix() ? "unix" : "windows"));
        getLogger().info("Loading data under " + LoggerTextFormat.GREEN + this.parentFolder);

        File setting = new File(this.parentFolder + "/Mirai.ini");
        getLogger().info("Selecting setting from " + LoggerTextFormat.GREEN + setting);

        if (!setting.exists()) {
            this.initSetting(setting);
        } else {
            this.setting = new MiraiSetting(setting);
        }

        File qqs = new File(this.parentFolder + "/QQ.yml");
        getLogger().info("Reading QQ accounts from  " + LoggerTextFormat.GREEN + qqs);
        if (!qqs.exists()) {
            this.initQQConfig(qqs);
        } else {
            this.qqs = new MiraiConfig(qqs);
        }
        if (this.qqs.isEmpty()) {
            this.initQQConfig(qqs);
        }

        /*
        MiraiSettingMapSection qqs = this.setting.getMapSection("qq");
        qqs.forEach((a,p) -> {
            this.getLogger().info("Finding available ports between " + "1-65536");
            try {
                int port = MiraiNetwork.getAvailablePort();
                this.getLogger().info("Listening on port " + port);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        */

        getLogger().info("ready to connect");


        this.qqs.keySet().stream().map(key -> this.qqs.getSection(key)).forEach(section -> {
            try {
                Robot robot = new Robot(section);
                RobotNetworkHandler robotNetworkHandler = robot.getNetworkHandler();
                robotNetworkHandler.tryLogin$mirai_core(state -> {
                    if (state == LoginState.SUCCEED) {
                        Robot.instances.add(robot);
                    } else {
                        robot.close();
                    }
                    return null;
                });

            } catch (Throwable e) {
                e.printStackTrace();
                getLogger().error("Could not load QQ robots config!");
                System.exit(1);
            }
        });
    }

    private void initSetting(File setting) {
        getLogger().info("Thanks for using Mirai");
        getLogger().info("initializing Settings");
        try {
            if (setting.createNewFile()) {
                getLogger().info("Mirai Config Created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setting = new MiraiSetting(setting);
        MiraiSettingMapSection network = this.setting.getMapSection("network");
        network.set("enable_proxy", "not supporting yet");

        MiraiSettingListSection proxy = this.setting.getListSection("proxy");
        proxy.add("1.2.3.4:95");
        proxy.add("1.2.3.4:100");

        MiraiSettingMapSection worker = this.setting.getMapSection("worker");
        worker.set("core_task_pool_worker_amount", 5);

        MiraiSettingMapSection plugin = this.setting.getMapSection("plugin");
        plugin.set("debug", false);

        this.setting.save();
        getLogger().info("initialized; changing can be made in setting file: " + setting.toString());
    }

    private void initQQConfig(File qqConfig) {
        this.qqs = new MiraiConfig(qqConfig);

        MiraiConfigSection<Object> section = new MiraiConfigSection<>();

        System.out.println("/");
        Scanner scanner = new Scanner(System.in);
        getLogger().info("Input a " + LoggerTextFormat.RED + " QQ number " + LoggerTextFormat.GREEN + "for default robotNetworkHandler");
        getLogger().info("输入用于默认机器人的QQ号");
        long qqNumber = scanner.nextLong();
        getLogger().info("Input the password for that QQ account");
        getLogger().info("输入该QQ号的密码");
        String qqPassword = scanner.next();

        section.put("password", qqPassword);
        section.put("owner", "default");

        this.qqs.put(String.valueOf(qqNumber), section);
        this.qqs.save();
        getLogger().info("QQ account initialized; changing can be made in Config file: " + qqConfig.toString());
    }

    private void onEnable() {
        this.eventManager.broadcastEvent(new ServerEnableEvent());
        this.enabled = true;
        getLogger().info(LoggerTextFormat.GREEN + "Server enabled; Welcome to Mirai");
        getLogger().info("Mirai Version=" + MiraiServer.MIRAI_VERSION + " QQ Version=" + MiraiServer.QQ_VERSION);
    }


}
