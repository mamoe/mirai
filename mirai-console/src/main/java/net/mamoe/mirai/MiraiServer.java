package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.server.ServerDisabledEvent;
import net.mamoe.mirai.event.events.server.ServerEnabledEvent;
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginState;
import net.mamoe.mirai.task.MiraiTaskManager;
import net.mamoe.mirai.utils.*;
import net.mamoe.mirai.utils.config.MiraiConfig;
import net.mamoe.mirai.utils.setting.MiraiSettingListSection;
import net.mamoe.mirai.utils.setting.MiraiSettingMapSection;
import net.mamoe.mirai.utils.setting.MiraiSettings;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Mirai 服务器.
 * 管理一些基础的事务
 *
 * @author NaturalHG
 */
public final class MiraiServer {
    private static MiraiServer instance;

    public static MiraiServer getInstance() {
        return instance;
    }

    public final static String MIRAI_VERSION = "1.0.0";

    public final static String QQ_VERSION = "4.9.0";


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

        this.logger = MiraiLogger.Companion;
        this.eventManager = MiraiEventManager.getInstance();
        this.taskManager = MiraiTaskManager.getInstance();

        getLogger().info("About to run Mirai (" + MiraiServer.MIRAI_VERSION + ") under " + (isUnix() ? "unix" : "windows"));
        getLogger().info("Loading data under " + LoggerTextFormat.GREEN + this.parentFolder);

        File setting = new File(this.parentFolder + "/Mirai.ini");
        getLogger().info("Selecting setting from " + LoggerTextFormat.GREEN + setting);

        /*
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
        }*/

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
        getLogger().info("QQ account initialized; changing can be made in Config file: " + qqConfig.toString());
        getLogger().info("QQ 账户管理初始化完毕");
    }

    private void onEnabled() {
        this.enabled = true;
        this.eventManager.broadcastEventAsync(new ServerEnabledEvent());
        getLogger().info(LoggerTextFormat.GREEN + "Server enabled; Welcome to Mirai");
        getLogger().info("Mirai Version=" + MiraiServer.MIRAI_VERSION + " QQ Version=" + MiraiServer.QQ_VERSION);

        getLogger().info("Initializing [Bot]s");

        try {
            getAvailableBot();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        /*
        this.qqs.keySet().stream().map(key -> this.qqs.getSection(key)).forEach(section -> {
            getLogger().info("Initializing [Bot] " + section.getString("account"));
            try {
                Bot bot = new Bot(section);
                var state = bot.network.tryLogin$mirai_core().of();
                //bot.network.tryLogin$mirai_core().whenComplete((state, e) -> {
                if (state == LoginState.SUCCESS) {
                    Bot.instances.add(bot);
                    getLogger().success("   Login Succeed");
                } else {
                    getLogger().error("   Login Failed with error " + state);
                    bot.close();
                }
                //  }).of();

            } catch (Throwable e) {
                e.printStackTrace();
                getLogger().error("Could not load QQ bots config!");
                System.exit(1);
            }
        });*/
    }


    String qqList =
            "1683921395----bb22222\n";

    private Bot getAvailableBot() throws ExecutionException, InterruptedException {
        for (String it : qqList.split("\n")) {
            var strings = it.split("----");
            var bot = new Bot(new BotAccount(Long.parseLong(strings[0]), strings[1]), new Console());

            if (bot.network.tryLogin(200).get() == LoginState.SUCCESS) {
                MiraiLoggerKt.success(bot, "Login succeed");
                return bot;
            }
        }

        throw new RuntimeException();
    }
}
