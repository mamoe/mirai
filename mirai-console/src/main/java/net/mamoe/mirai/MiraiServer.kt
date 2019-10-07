package net.mamoe.mirai

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginState
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.config.MiraiConfig
import net.mamoe.mirai.utils.setting.MiraiSettings
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException

/**
 * @author Him188moe
 */

/**
 * Mirai 服务器.
 * 管理一些基础的事务
 *
 * @author NaturalHG
 */
object MiraiServer {
    var isUnix: Boolean = false
        private set

    var parentFolder: File = File(System.getProperty("user.dir"))

    var logger: MiraiLogger
        internal set

    internal lateinit var settings: MiraiSettings

    internal lateinit var qqs: MiraiConfig

    private var enabled: Boolean = false


    init {
        this.isUnix = !System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS")

        this.logger = MiraiLogger

        logger.info("About to run Mirai (" + Mirai.VERSION + ") under " + if (isUnix) "unix" else "windows")
        logger.info("Loading data under " + LoggerTextFormat.GREEN + this.parentFolder)

        val setting = this.parentFolder + "/Mirai.ini"
        logger.info("Selecting setting from " + LoggerTextFormat.GREEN + setting)

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


        this.reload()
    }

    fun shutdown() {
        if (this.enabled) {
            logger.info("About to shutdown Mirai")
            logger.info("Data have been saved")
        }

    }

    private fun initSetting(setting: File) {
        logger.info("Thanks for using Mirai")
        logger.info("initializing Settings")
        try {
            if (setting.createNewFile()) {
                logger.info("Mirai Config Created")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        this.settings = MiraiSettings(setting)
        val network = this.settings.getMapSection("network")
        network["enable_proxy"] = "not supporting yet"

        val proxy = this.settings.getListSection("proxy")
        proxy.add("1.2.3.4:95")
        proxy.add("1.2.3.4:100")

        val worker = this.settings.getMapSection("worker")
        worker["core_task_pool_worker_amount"] = 5

        val plugin = this.settings.getMapSection("plugin")
        plugin["debug"] = false

        this.settings.save()
        logger.info("initialized; changing can be made in setting file: $setting")
    }

    private fun initQQConfig(qqConfig: File) {
        this.qqs = MiraiConfig(qqConfig)
        MiraiLogger.info("QQ account initialized; changing can be made in Config file: $qqConfig")
        logger.info("QQ 账户管理初始化完毕")
    }

    private fun reload() {
        this.enabled = true
        MiraiLogger.info(LoggerTextFormat.GREEN.toString() + "Server enabled; Welcome to Mirai")
        MiraiLogger.info("Mirai Version=" + Mirai.VERSION)

        MiraiLogger.info("Initializing [Bot]s")

        try {
            availableBot
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        /*
        this.qqs.keySet().stream().map(key -> this.qqs.getSection(key)).forEach(section -> {
            getLogger().info("Initializing [Bot] " + section.getString("account"));
            try {
                Bot bot = new Bot(section);
                var state = bot.network.login$mirai_core().of();
                //bot.network.login$mirai_core().whenComplete((state, e) -> {
                if (state == LoginState.SUCCESS) {
                    Bot.instances.add(bot);
                    getLogger().green("   Login Succeed");
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


    //todo only for test now
    private var qqList = "1683921395----bb22222\n"

    private val availableBot: Bot
        @Throws(ExecutionException::class, InterruptedException::class)
        get() {
            for (it in qqList.split("\n").dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val strings = it.split("----").dropLastWhile { it.isEmpty() }.toTypedArray()
                val bot = Bot(BotAccount(strings[0].toLong(), strings[1]), Console())

                if (runBlocking { bot.login() } === LoginState.SUCCESS) {
                    bot.green("Login succeed")
                    return bot
                }
            }

            throw RuntimeException()
        }
}
