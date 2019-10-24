@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.LoggerTextFormat
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.config.MiraiConfig
import net.mamoe.mirai.utils.setting.MiraiSettings
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException


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

        logger.logInfo("About to run Mirai (" + Mirai.VERSION + ") under " + if (isUnix) "unix" else "windows")
        logger.logInfo("Loading data under " + LoggerTextFormat.GREEN + this.parentFolder)

        val setting = File(this.parentFolder, "/Mirai.ini")
        logger.logInfo("Selecting setting from " + LoggerTextFormat.GREEN + setting)

        /*
        if (!setting.exists()) {
            this.initSetting(setting);
        } else {
            this.settings = new MiraiSettings(setting);
        }

        File qqs = new File(this.parentFolder + "/QQ.yml");
        getLogger().logInfo("Reading QQ accounts from  " + LoggerTextFormat.GREEN + qqs);
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
            this.getLogger().logInfo("Finding available ports between " + "1-65536");
            try {
                int port = MiraiNetwork.getAvailablePort();
                this.getLogger().logInfo("Listening on port " + port);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        */


        this.reload()
    }

    fun shutdown() {
        if (this.enabled) {
            logger.logInfo("About to shutdown Mirai")
            logger.logInfo("Data have been saved")
        }

    }

    private fun initSetting(setting: File) {
        logger.logInfo("Thanks for using Mirai")
        logger.logInfo("initializing Settings")
        try {
            if (setting.createNewFile()) {
                logger.logInfo("Mirai Config Created")
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
        plugin["logDebug"] = false

        this.settings.save()
        logger.logInfo("initialized; changing can be made in setting file: $setting")
    }

    private fun initQQConfig(qqConfig: File) {
        this.qqs = MiraiConfig(qqConfig)
        MiraiLogger.logInfo("QQ account initialized; changing can be made in Config file: $qqConfig")
        logger.logInfo("QQ 账户管理初始化完毕")
    }

    private fun reload() {
        this.enabled = true
        MiraiLogger.logInfo(LoggerTextFormat.GREEN.toString() + "Server enabled; Welcome to Mirai")
        MiraiLogger.logInfo("Mirai Version=" + Mirai.VERSION)

        MiraiLogger.logInfo("Initializing [Bot]s")

        try {
            availableBot
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        /*
        this.qqs.keySet().stream().map(key -> this.qqs.getSection(key)).forEach(section -> {
            getLogger().logInfo("Initializing [Bot] " + section.getString("account"));
            try {
                Bot bot = new Bot(section);
                var state = bot.network.login$mirai_core().of();
                //bot.network.login$mirai_core().whenComplete((state, e) -> {
                if (state == LoginState.SUCCESS) {
                    Bot.instances.add(bot);
                    getLogger().logGreen("   Login Succeed");
                } else {
                    getLogger().logError("   Login Failed with logError " + state);
                    bot.close();
                }
                //  }).of();

            } catch (Throwable e) {
                e.printStackTrace();
                getLogger().logError("Could not load QQ bots config!");
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
                val bot = Bot(BotAccount(strings[0].toUInt(), strings[1]), MiraiLogger)

                if (runBlocking { bot.login() } === LoginResult.SUCCESS) {
                    bot.logger.logGreen("Login succeed")
                    return bot
                }
            }

            throw RuntimeException()
        }
}
