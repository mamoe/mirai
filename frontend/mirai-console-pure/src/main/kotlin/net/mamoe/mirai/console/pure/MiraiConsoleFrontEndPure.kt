/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "INVISIBLE_SETTER",
    "INVISIBLE_GETTER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER_WARNING",
    "EXPOSED_SUPER_CLASS"
)

package net.mamoe.mirai.console.pure

//import net.mamoe.mirai.console.command.CommandManager
//import net.mamoe.mirai.console.utils.MiraiConsoleFrontEnd
import io.ktor.utils.io.concurrent.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsoleFrontEnd
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.DefaultLoginSolver
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import org.fusesource.jansi.Ansi
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val ANSI_RESET = Ansi().reset().toString()

internal val LoggerCreator: (identity: String?) -> MiraiLogger = {
    PlatformLogger(identity = it, output = { line ->
        ConsoleUtils.lineReader.printAbove(line + ANSI_RESET)
    })
}

/**
 * mirai-console-pure 前端实现
 *
 * @see MiraiConsoleImplementationPure 后端实现
 * @see MiraiConsolePureLoader CLI 入口点
 */
@ConsoleInternalAPI
@Suppress("unused")
object MiraiConsoleFrontEndPure : MiraiConsoleFrontEnd {
    private val globalLogger = LoggerCreator("Mirai")
    private val cachedLoggers = ConcurrentHashMap<String, MiraiLogger>()

    // companion object {
    // ANSI color codes
    const val COLOR_RED = "\u001b[38;5;196m"
    const val COLOR_CYAN = "\u001b[38;5;87m"
    const val COLOR_GREEN = "\u001b[38;5;82m"

    // use a dark yellow(more like orange) instead of light one to save Solarized-light users
    const val COLOR_YELLOW = "\u001b[38;5;220m"
    const val COLOR_GREY = "\u001b[38;5;244m"
    const val COLOR_BLUE = "\u001b[38;5;27m"
    const val COLOR_NAVY = "\u001b[38;5;24m" // navy uniform blue
    const val COLOR_PINK = "\u001b[38;5;207m"
    const val COLOR_RESET = "\u001b[39;49m"
    // }

    internal val sdf by ThreadLocal.withInitial {
        // SimpleDateFormat not thread safe.
        SimpleDateFormat("HH:mm:ss")
    }

    private operator fun <T> ThreadLocal<T>.getValue(thiz: Any, property: Any): T {
        return this.get()
    }

    override val name: String
        get() = "Pure"
    override val version: String
        get() = net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.version

    override fun loggerFor(identity: String?): MiraiLogger {
        identity?.apply {
            return cachedLoggers.computeIfAbsent(this, LoggerCreator)
        }
        return globalLogger
    }

    override fun pushBot(bot: Bot) {
    }

    override suspend fun requestInput(hint: String): String {
        return ConsoleUtils.miraiLineReader(hint)
    }

    override fun createLoginSolver(): LoginSolver {
        return DefaultLoginSolver(
            input = suspend {
                requestInput("LOGIN> ")
            }
        )
    }
}


/*
class MiraiConsoleFrontEndPure : MiraiConsoleFrontEnd {
    private var requesting = false
    private var requestStr = ""

    @Suppress("unused")
    companion object {
        // ANSI color codes
        const val COLOR_RED = "\u001b[38;5;196m"
        const val COLOR_CYAN = "\u001b[38;5;87m"
        const val COLOR_GREEN = "\u001b[38;5;82m"

        // use a dark yellow(more like orange) instead of light one to save Solarized-light users
        const val COLOR_YELLOW = "\u001b[38;5;220m"
        const val COLOR_GREY = "\u001b[38;5;244m"
        const val COLOR_BLUE = "\u001b[38;5;27m"
        const val COLOR_NAVY = "\u001b[38;5;24m" // navy uniform blue
        const val COLOR_PINK = "\u001b[38;5;207m"
        const val COLOR_RESET = "\u001b[39;49m"
    }

    init {
        thread(name = "Mirai Console Input Thread") {
            while (true) {
                val input = readLine() ?: return@thread
                if (requesting) {
                    requestStr = input
                    requesting = false
                } else {
                    CommandManager.runCommand(ConsoleCommandSender, input)
                }
            }
        }
    }

    val sdf by lazy {
        SimpleDateFormat("HH:mm:ss")
    }

    override val logger: MiraiLogger = DefaultLogger("Console") // CLI logger from mirai-core

    fun pushLog(identity: Long, message: String) {
        println("\u001b[0m " + sdf.format(Date()) + " $message")
    }

    override fun prePushBot(identity: Long) {

    }

    override fun pushBot(bot: Bot) {

    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {

    }

    override suspend fun requestInput(hint: String): String {
        if (hint.isNotEmpty()) {
            println("\u001b[0m " + sdf.format(Date()) + COLOR_PINK + " $hint")
        }
        requesting = true
        while (true) {
            delay(50)
            if (!requesting) {
                return requestStr
            }
        }
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) {

    }

    override fun createLoginSolver(): LoginSolver {
        return DefaultLoginSolver(
            input = suspend {
                requestInput("")
            }
        )
    }

}

*/
