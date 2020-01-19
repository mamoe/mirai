import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.plugin.PluginManager
import kotlin.concurrent.thread

fun main() {

    println("loading Mirai in console environments")
    println("正在控制台环境中启动Mirai ")
    println()
    println("Mirai-console is still in testing stage, some feature is not available")
    println("Mirai-console 还处于测试阶段, 部分功能不可用")
    println()
    println("Mirai-console now running on " + System.getProperty("user.dir"))
    println("Mirai-console 正在 " + System.getProperty("user.dir") + " 运行")
    println()
    println("\"login qqnumber qqpassword \" to login a bot")
    println("\"login qq号 qq密码 \" 来登陆一个BOT")

    thread { processNextCommandLine() }

    PluginManager.loadPlugins()

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        PluginManager.disableAllPlugins()
    })
}

tailrec fun processNextCommandLine() {
    val commandArgs = readLine()?.split(" ") ?: return
    when (commandArgs[0]) {
        "login" -> {
            if (commandArgs.size < 3) {
                println("\"login qqnumber qqpassword \" to login a bot")
                println("\"login qq号 qq密码 \" 来登录一个BOT")
                return processNextCommandLine()
            }
            val qqNumber = commandArgs[1].toLong()
            val qqPassword = commandArgs[2]
            println("login...")
            GlobalScope.launch {
                Bot(qqNumber, qqPassword).alsoLogin()
            }
        }
    }
    return processNextCommandLine()
}
