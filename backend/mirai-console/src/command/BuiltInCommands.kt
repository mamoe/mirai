/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser.Companion.map
import net.mamoe.mirai.console.command.descriptor.PermissionIdValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.PermitteeIdValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.buildCommandArgumentContext
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.CommandManagerImpl.allRegisteredCommands
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.*
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.PasswordKind.PLAIN
import net.mamoe.mirai.console.internal.permission.BuiltInPermissionService
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import net.mamoe.mirai.console.internal.util.runIgnoreException
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.Permission.Companion.parentsWithSelf
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.cancel
import net.mamoe.mirai.console.permission.PermissionService.Companion.findCorrespondingPermissionOrFail
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.message.nextMessageOrNull
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.secondsToMillis
import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.math.floor
import kotlin.system.exitProcess


@ConsoleExperimentalApi
@Suppress("EXPOSED_SUPER_INTERFACE")
public interface BuiltInCommand : Command

// for identification
internal interface BuiltInCommandInternal : Command, BuiltInCommand

/**
 * 内建指令列表
 *
 * [查看文档](https://github.com/mamoe/mirai-console/docs/BuiltInCommands.md)
 */
@Suppress("unused", "RESTRICTED_CONSOLE_COMMAND_OWNER")
public object BuiltInCommands {
    @ConsoleExperimentalApi
    public val parentPermission: Permission by lazy {
        PermissionService.INSTANCE.register(
            ConsoleCommandOwner.permissionId("*"),
            "The parent of any built-in commands"
        )
    }

    internal val all: Array<out Command> by lazy {
        this::class.nestedClasses.mapNotNull { it.objectInstance as? Command }.toTypedArray()
    }

    internal fun registerAll() {
        BuiltInCommands::class.nestedClasses.forEach {
            (it.objectInstance as? Command)?.register()
        }
    }

    public object HelpCommand : SimpleCommand(
        ConsoleCommandOwner, "help",
        description = "查看指令帮助",
    ), BuiltInCommandInternal {
        @Handler
        public suspend fun CommandSender.handle() {
            sendMessage(
                allRegisteredCommands
                    .filter { hasPermission(it.permission) }
                    .joinToString("\n\n") { command ->
                        val lines = command.usage.lines()
                        if (lines.isEmpty()) "/${command.primaryName} ${command.description}"
                        else
                            "◆ " + lines.first() + "\n" + lines.drop(1).joinToString("\n") { "  $it" }
                    }.lines().filterNot(String::isBlank).joinToString("\n")
            )
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(thread(false) {
            MiraiConsole.cancel()
        })
    }

    public object StopCommand : SimpleCommand(
        ConsoleCommandOwner, "stop", "shutdown", "exit",
        description = "关闭 Mirai Console",
    ), BuiltInCommandInternal {

        private val closingLock = Mutex()

        @OptIn(DelicateCoroutinesApi::class)
        @Handler
        public suspend fun CommandSender.handle() {
            GlobalScope.launch {
                kotlin.runCatching {
                    closingLock.withLock {
                        if (!MiraiConsole.isActive) return@withLock
                        sendMessage("Stopping mirai-console")
                        kotlin.runCatching {
                            Bot.instances.forEach { bot ->
                                lateinit var logger: MiraiLogger
                                kotlin.runCatching {
                                    logger = bot.logger
                                    bot.closeAndJoin()
                                }.onFailure { t ->
                                    kotlin.runCatching { logger.error("Error in closing bot", t) }
                                }
                            }
                            MiraiConsole.job.cancelAndJoin()
                        }.fold(
                            onSuccess = {
                                runIgnoreException<EventCancelledException> { sendMessage("mirai-console stopped successfully.") }
                            },
                            onFailure = {
                                @OptIn(ConsoleInternalApi::class)
                                MiraiConsole.mainLogger.error("Exception in stop", it)
                                runIgnoreException<EventCancelledException> {
                                    sendMessage(
                                        it.localizedMessage ?: it.message ?: it.toString()
                                    )
                                }
                            }
                        )
                    }
                }.exceptionOrNull()?.let(MiraiConsole.mainLogger::error)
                exitProcess(0)
            }
        }
    }

    public object LoginCommand : SimpleCommand(
        ConsoleCommandOwner, "login", "登录",
        description = "登录一个账号",
    ), BuiltInCommandInternal {
        private suspend fun Bot.doLogin() = kotlin.runCatching {
            login(); this
        }.onFailure { close() }.getOrThrow()

        @Handler
        @JvmOverloads
        public suspend fun CommandSender.handle(
            @Name("qq") id: Long,
            password: String,
            protocol: BotConfiguration.MiraiProtocol? = null,
        ) {
            kotlin.runCatching {
                MiraiConsole.addBot(id, password) {
                    if (protocol != null) {
                        this.protocol = protocol
                    }
                }.doLogin()
            }.fold(
                onSuccess = { scopeWith(ConsoleCommandSender).sendMessage("${it.nick} ($id) Login successful") },
                onFailure = { throwable ->
                    scopeWith(ConsoleCommandSender).sendMessage(
                        "Login failed: ${throwable.localizedMessage ?: throwable.message ?: throwable.toString()}" +
                            if (this is CommandSenderOnMessage<*>) {
                                CommandManagerImpl.launch(CoroutineName("stacktrace delayer from Login")) {
                                    fromEvent.nextMessageOrNull(60.secondsToMillis) { it.message.contentEquals("stacktrace") }
                                }
                                "\n 1 分钟内发送 stacktrace 以获取堆栈信息"
                            } else ""
                    )

                    throw throwable
                }
            )
        }
    }

    public object PermissionCommand : CompositeCommand(
        ConsoleCommandOwner, "permission", "权限", "perm",
        description = "管理权限",
        overrideContext = buildCommandArgumentContext {
            PermitteeId::class with PermitteeIdValueArgumentParser
            Permission::class with PermissionIdValueArgumentParser.map { id ->
                kotlin.runCatching {
                    id.findCorrespondingPermissionOrFail()
                }.getOrElse { throw CommandArgumentParserException("指令不存在: $id", it) }
            }
        },
    ), BuiltInCommandInternal {
        // TODO: 2020/9/10 improve Permission command

        @Description("授权一个权限")
        @SubCommand("permit", "grant", "add")
        public suspend fun CommandSender.permit(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("权限 ID") permission: Permission,
        ) {
            target.permit(permission)
            sendMessage("OK")
        }

        @Description("撤销一个权限")
        @SubCommand("cancel", "deny", "remove")
        public suspend fun CommandSender.cancel(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("权限 ID") permission: Permission,
        ) {
            target.cancel(permission, false)
            sendMessage("OK")
        }

        @Description("撤销一个权限及其所有子权限")
        @SubCommand("cancelAll", "denyAll", "removeAll")
        public suspend fun CommandSender.cancelAll(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("权限 ID") permission: Permission,
        ) {
            target.cancel(permission, true)
            sendMessage("OK")
        }

        @Description("查看被授权权限列表")
        @SubCommand("permittedPermissions", "pp", "grantedPermissions", "gp")
        public suspend fun CommandSender.permittedPermissions(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("包括重复") all: Boolean = false,
        ) {
            var grantedPermissions = target.getPermittedPermissions().toList()
            if (!all) {
                grantedPermissions = grantedPermissions.filter { thisPerm ->
                    grantedPermissions.none { other -> thisPerm.parentsWithSelf.drop(1).any { it == other } }
                }
            }
            if (grantedPermissions.isEmpty()) {
                sendMessage("${target.asString()} 未被授予任何权限. 使用 `${CommandManager.commandPrefix}permission grant` 给予权限.")
            } else {
                sendMessage(grantedPermissions.joinToString("\n") { it.id.toString() })
            }
        }

        @Description("查看所有权限列表")
        @SubCommand("listPermissions", "lp")
        public suspend fun CommandSender.listPermissions() {
            sendMessage(PermissionService.INSTANCE.getRegisteredPermissions().joinToString("\n") { it.id.toString() + "    " + it.description })
        }
    }


    public object AutoLoginCommand : CompositeCommand(
        ConsoleCommandOwner, "autoLogin", "自动登录",
        description = "自动登录设置",
        overrideContext = buildCommandArgumentContext {
            ConfigurationKey::class with ConfigurationKey.Parser
        }
    ), BuiltInCommandInternal {
        @Description("查看自动登录账号列表")
        @SubCommand
        public suspend fun CommandSender.list() {
            sendMessage(buildString {
                for (account in AutoLoginConfig.accounts) {
                    if (account.account == "123456") continue
                    append("- ")
                    append("账号: ")
                    append(account.account)
                    appendLine()
                    append("  密码: ")
                    append(account.password.value)
                    appendLine()

                    if (account.configuration.isNotEmpty()) {
                        appendLine("  配置:")
                        for ((key, value) in account.configuration) {
                            append("    $key = $value")
                        }
                        appendLine()
                    }
                }
            })
        }

        @Description("添加自动登录")
        @SubCommand
        public suspend fun CommandSender.add(account: Long, password: String, passwordKind: PasswordKind = PLAIN) {
            val accountStr = account.toString()
            if (AutoLoginConfig.accounts.any { it.account == accountStr }) {
                sendMessage("已有相同账号在自动登录配置中. 请先删除该账号.")
                return
            }
            AutoLoginConfig.accounts.add(AutoLoginConfig.Account(accountStr, Password(passwordKind, password)))
            sendMessage("已成功添加 '$account'.")
        }

        @Description("清除所有配置")
        @SubCommand
        public suspend fun CommandSender.clear() {
            AutoLoginConfig.accounts.clear()
            sendMessage("已清除所有自动登录配置.")
        }

        @Description("删除一个账号")
        @SubCommand
        public suspend fun CommandSender.remove(account: Long) {
            val accountStr = account.toString()
            if (AutoLoginConfig.accounts.removeIf { it.account == accountStr }) {
                sendMessage("已成功删除 '$account'.")
                return
            }
            sendMessage("账号 '$account' 未配置自动登录.")
        }

        @Description("设置一个账号的一个配置项")
        @SubCommand
        public suspend fun CommandSender.setConfig(account: Long, configKey: ConfigurationKey, value: String) {
            val accountStr = account.toString()

            val oldAccount = AutoLoginConfig.accounts.find { it.account == accountStr } ?: kotlin.run {
                sendMessage("未找到账号 $account.")
                return
            }

            if (value.isEmpty()) return removeConfig(account, configKey)

            val newAccount = oldAccount.copy(configuration = oldAccount.configuration.toMutableMap().apply {
                put(configKey, value)
            })

            AutoLoginConfig.accounts.remove(oldAccount)
            AutoLoginConfig.accounts.add(newAccount)

            sendMessage("成功修改 '$account' 的配置 '$configKey' 为 '$value'")
        }

        @Description("删除一个账号的一个配置项")
        @SubCommand
        public suspend fun CommandSender.removeConfig(account: Long, configKey: ConfigurationKey) {
            val accountStr = account.toString()

            val oldAccount = AutoLoginConfig.accounts.find { it.account == accountStr } ?: kotlin.run {
                sendMessage("未找到账号 $account.")
                return
            }

            val newAccount = oldAccount.copy(configuration = oldAccount.configuration.toMutableMap().apply {
                remove(configKey)
            })

            AutoLoginConfig.accounts.remove(oldAccount)
            AutoLoginConfig.accounts.add(newAccount)

            sendMessage("成功删除 '$account' 的配置 '$configKey'.")
        }
    }

    public object StatusCommand : SimpleCommand(
        ConsoleCommandOwner, "status", "states", "状态",
        description = "获取 Mirai Console 运行状态"
    ), BuiltInCommandInternal {

        internal interface MemoryUsageGet {
            val heapMemoryUsage: MUsage
            val nonHeapMemoryUsage: MUsage
            val objectPendingFinalizationCount: Int
        }

        internal val memoryUsageGet: MemoryUsageGet = kotlin.runCatching {
            ByMemoryMXBean
        }.getOrElse { ByRuntime }

        internal object ByMemoryMXBean : MemoryUsageGet {
            val memoryMXBean = ManagementFactory.getMemoryMXBean()
            val MemoryUsage.m: MUsage
                get() = MUsage(
                    committed, init, used, max
                )
            override val heapMemoryUsage: MUsage
                get() = memoryMXBean.heapMemoryUsage.m
            override val nonHeapMemoryUsage: MUsage
                get() = memoryMXBean.nonHeapMemoryUsage.m
            override val objectPendingFinalizationCount: Int
                get() = memoryMXBean.objectPendingFinalizationCount
        }

        internal object ByRuntime : MemoryUsageGet {
            override val heapMemoryUsage: MUsage
                get() {
                    val runtime = Runtime.getRuntime()
                    return MUsage(
                        committed = 0,
                        init = 0,
                        used = runtime.maxMemory() - runtime.freeMemory(),
                        max = runtime.maxMemory()
                    )
                }
            override val nonHeapMemoryUsage: MUsage
                get() = MUsage(-1, -1, -1, -1)
            override val objectPendingFinalizationCount: Int
                get() = -1
        }

        internal data class MUsage(
            val committed: Long,
            val init: Long,
            val used: Long,
            val max: Long,
        )

        @Handler
        public suspend fun CommandSender.handle() {
            sendAnsiMessage {
                val buildDateFormatted =
                    MiraiConsoleBuildConstants.buildDate.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                append("Running MiraiConsole v")
                gold().append(MiraiConsoleBuildConstants.versionConst)
                reset().append(", built on ")
                lightBlue().append(buildDateFormatted).reset().append(".\n")
                append(MiraiConsoleImplementationBridge.frontEndDescription.render()).append("\n\n")
                append("Permission Service: ").append(
                    if (PermissionService.INSTANCE is BuiltInPermissionService) {
                        lightYellow()
                        "Built In Permission Service"
                    } else {
                        val plugin = PermissionServiceProvider.providerPlugin
                        if (plugin == null) {
                            PermissionService.INSTANCE.toString()
                        } else {
                            green().append(plugin.name).reset().append(" v").gold()
                            plugin.version.toString()
                        }
                    }
                )
                reset().append("\n\n")

                append("Plugins: ")
                if (PluginManagerImpl.resolvedPlugins.isEmpty()) {
                    gray().append("<none>")
                } else {
                    PluginManagerImpl.resolvedPlugins.joinTo(this) { plugin ->
                        green().append(plugin.name).reset().append(" v").gold()
                        plugin.version.toString()
                    }
                }
                reset().append("\n\n")

                append("Object Pending Finalization Count: ")
                    .emeraldGreen()
                    .append(memoryUsageGet.objectPendingFinalizationCount)
                    .reset()
                    .append("\n")
                val l1 = arrayOf("committed", "init", "used", "max")
                val l2 = renderMemoryUsage(memoryUsageGet.heapMemoryUsage)
                val l3 = renderMemoryUsage(memoryUsageGet.nonHeapMemoryUsage)
                val lmax = calculateMax(l1, l2.first, l3.first)

                append("                 ")
                l1.forEachIndexed { index, s ->
                    if (index != 0) append(" | ")
                    renderMUNum(lmax[index], s.length) { append(s); reset() }
                }
                reset()
                append("\n")

                fun rendMU(l: Pair<Array<String>, LongArray>) {
                    val max = l.second[3]
                    val e50 = max / 2
                    val e90 = max * 90 / 100
                    l.first.forEachIndexed { index, s ->
                        if (index != 0) append(" | ")
                        renderMUNum(lmax[index], s.length) {
                            if (index == 3) {
                                // MAX
                                append(s)
                            } else {
                                if (max < 0L) {
                                    append(s)
                                } else {
                                    val v = l.second[index]
                                    when {
                                        v < e50 -> {
                                            green()
                                        }
                                        v < e90 -> {
                                            lightRed()
                                        }
                                        else -> {
                                            red()
                                        }
                                    }
                                    append(s)
                                    reset()
                                }
                            }
                        }
                    }
                }

                append("    Heap Memory: ")
                rendMU(l2)
                append("\nNon-Heap Memory: ")
                rendMU(l3)
            }
        }

        private const val MEM_B = 1024L
        private const val MEM_KB = 1024L shl 10
        private const val MEM_MB = 1024L shl 20
        private const val MEM_GB = 1024L shl 30

        @Suppress("NOTHING_TO_INLINE")
        private inline fun StringBuilder.appendDouble(number: Double): StringBuilder =
            append(floor(number * 100) / 100)

        private fun renderMemoryUsageNumber(num: Long) = buildString {
            when {
                num == -1L -> {
                    append(num)
                }
                num < MEM_B -> {
                    append(num).append("B")
                }
                num < MEM_KB -> {
                    appendDouble(num / 1024.0).append("KB")
                }
                num < MEM_MB -> {
                    appendDouble((num ushr 10) / 1024.0).append("MB")
                }
                else -> {
                    appendDouble((num ushr 20) / 1024.0).append("GB")
                }
            }
        }

        private fun AnsiMessageBuilder.renderMemoryUsage(usage: MUsage) = arrayOf(
            renderMemoryUsageNumber(usage.committed),
            renderMemoryUsageNumber(usage.init),
            renderMemoryUsageNumber(usage.used),
            renderMemoryUsageNumber(usage.max),
        ) to longArrayOf(
            usage.committed,
            usage.init,
            usage.used,
            usage.max,
        )

        private var emptyLine = "    ".repeat(10)
        private fun Appendable.emptyLine(size: Int) {
            if (emptyLine.length <= size) {
                emptyLine = String(CharArray(size) { ' ' })
            }
            append(emptyLine, 0, size)
        }

        private inline fun AnsiMessageBuilder.renderMUNum(size: Int, contentLength: Int, code: () -> Unit) {
            val s = size - contentLength
            val left = s / 2
            val right = s - left
            emptyLine(left)
            code()
            emptyLine(right)
        }

        private fun calculateMax(
            vararg lines: Array<String>
        ): IntArray = IntArray(lines[0].size) { r ->
            lines.maxOf { it[r].length }
        }
    }
}