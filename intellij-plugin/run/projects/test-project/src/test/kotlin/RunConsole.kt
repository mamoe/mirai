import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.pure.MiraiConsolePureLoader
import org.example.myplugin.MyPluginMain

suspend fun main() {
    MiraiConsolePureLoader.startAsDaemon()

    MyPluginMain.load() // 主动加载插件, Console 会调用 MyPluginMain.onLoad
    MyPluginMain.enable() // 主动启用插件, Console 会调用 MyPluginMain.onEnable

    val bot = MiraiConsole.addBot(123456, "").alsoLogin() // 登录一个测试环境的 Bot

    MiraiConsole.job.join()
}