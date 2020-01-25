import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.plugin.Command
import net.mamoe.mirai.plugin.CommandManager
import net.mamoe.mirai.plugin.PluginManager
import kotlin.concurrent.thread

val bots = mutableMapOf<Long, Bot>()

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
    println("\"/login qqnumber qqpassword \" to login a bot")
    println("\"/login qq号 qq密码 \" 来登陆一个BOT")

    thread { processNextCommandLine() }

    PluginManager.loadPlugins()
    defaultCommands()

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        PluginManager.disableAllPlugins()
    })
}


fun defaultCommands() {
    class LoginCommand : Command(
        "login"
    ) {
        override fun onCommand(args: List<String>): Boolean {
            if (args.size < 2) {
                println("\"/login qqnumber qqpassword \" to login a bot")
                println("\"/login qq号 qq密码 \" 来登录一个BOT")
                return false
            }
            val qqNumber = args[0].toLong()
            val qqPassword = args[1]
            println("login...")
            runBlocking {
                try {
                    Bot(qqNumber, qqPassword).also {
                        it.login()
                        bots[qqNumber] = it
                    }
                } catch (e: Exception) {
                    println("$qqNumber login failed")
                }
            }
            return true
        }
    }
    CommandManager.register(LoginCommand())
}

tailrec fun processNextCommandLine() {
    val fullCommand = readLine()
    if (fullCommand != null && fullCommand.startsWith("/")) {
        if (!CommandManager.runCommand(fullCommand)) {
            println("unknown command $fullCommand")
            println("未知指令 $fullCommand")
        }
    }
    processNextCommandLine();
}
