package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisabledEvent;
import net.mamoe.mirai.event.events.server.ServerEnabledEvent;
import net.mamoe.mirai.network.packet.login.LoginState;
import net.mamoe.mirai.task.MiraiTaskManager;
import net.mamoe.mirai.utils.LoggerTextFormat;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.config.MiraiConfig;
import net.mamoe.mirai.utils.config.MiraiConfigSection;
import net.mamoe.mirai.utils.setting.MiraiSettingListSection;
import net.mamoe.mirai.utils.setting.MiraiSettingMapSection;
import net.mamoe.mirai.utils.setting.MiraiSettings;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author NaturalHG
 */
public class MiraiServer {
    private static MiraiServer instance;

    public static MiraiServer getInstance() {
        return instance;
    }

    private final static String MIRAI_VERSION = "1.0.0";

    private final static String QQ_VERSION = "4.9.0";


    @Getter //is running under UNIX
    private boolean unix;

    @Getter//file pathq
    public File parentFolder;

    @Getter
    MiraiEventManager eventManager;
    @Getter
    MiraiTaskManager taskManager;

    @Getter
    MiraiLogger logger;

    MiraiSettings settings;

    MiraiConfig qqs;


    MiraiServer() {
        instance = this;
        this.onLoaded();
        this.onEnabled();
    }

    private boolean enabled;

    void shutdown() {
        if (this.enabled) {
            getLogger().info("About to shutdown Mirai");
            this.eventManager.broadcastEventAsync(new ServerDisabledEvent());
            getLogger().info("Data have been saved");
        }

    }


    private void onLoaded() {
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
            this.settings = new MiraiSettings(setting);
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
        this.settings = new MiraiSettings(setting);
        MiraiSettingMapSection network = this.settings.getMapSection("network");
        network.set("enable_proxy", "not supporting yet");

        MiraiSettingListSection proxy = this.settings.getListSection("proxy");
        proxy.add("1.2.3.4:95");
        proxy.add("1.2.3.4:100");

        MiraiSettingMapSection worker = this.settings.getMapSection("worker");
        worker.set("core_task_pool_worker_amount", 5);

        MiraiSettingMapSection plugin = this.settings.getMapSection("plugin");
        plugin.set("debug", false);

        this.settings.save();
        getLogger().info("initialized; changing can be made in setting file: " + setting.toString());
    }

    private void initQQConfig(File qqConfig) {
        this.qqs = new MiraiConfig(qqConfig);

        MiraiConfigSection<Object> section = new MiraiConfigSection<>();

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

    private void onEnabled() {
        this.enabled = true;
        this.eventManager.broadcastEventAsync(new ServerEnabledEvent());
        getLogger().info(LoggerTextFormat.GREEN + "Server enabled; Welcome to Mirai");
        getLogger().info("Mirai Version=" + MiraiServer.MIRAI_VERSION + " QQ Version=" + MiraiServer.QQ_VERSION);

        getLogger().info("Initializing [Robot]s");

        this.qqs.keySet().stream().map(key -> this.qqs.getSection(key)).forEach(section -> {
            getLogger().info("Initializing [Robot] " + section.getString("account"));
            try {
                Robot robot = new Robot(section);
                var state = robot.network.tryLogin$mirai_core().get();
                //robot.network.tryLogin$mirai_core().whenComplete((state, e) -> {
                if (state == LoginState.SUCCEED) {
                    Robot.instances.add(robot);
                    getLogger().success("    Login Succeed");
                } else {
                    getLogger().error("    Login Failed with error " + state);
                    robot.close();
                }
                //  }).get();

            } catch (Throwable e) {
                e.printStackTrace();
                getLogger().error("Could not load QQ robots config!");
                System.exit(1);
            }
        });
    }


}
