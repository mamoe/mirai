/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleFrontEndImplementation::class)

package net.mamoe.mirai.console.command

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.ConsoleDataScope.Companion.get
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.allRegisteredCommands
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser.Companion.map
import net.mamoe.mirai.console.command.descriptor.PermissionIdValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.PermitteeIdValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.buildCommandArgumentContext
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants
import net.mamoe.mirai.console.internal.command.builtin.LoginCommandImpl
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.*
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.PasswordKind.PLAIN
import net.mamoe.mirai.console.internal.data.builtins.DataScope
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.permission.BuiltInPermissionService
import net.mamoe.mirai.console.internal.permission.getPermittedPermissionsAndSource
import net.mamoe.mirai.console.internal.plugin.JvmPluginInternal
import net.mamoe.mirai.console.internal.plugin.MiraiConsoleAsPlugin
import net.mamoe.mirai.console.internal.pluginManagerImpl
import net.mamoe.mirai.console.internal.util.runIgnoreException
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.cancel
import net.mamoe.mirai.console.permission.PermissionService.Companion.findCorrespondingPermissionOrFail
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.MemoryUsage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess


@ConsoleExperimentalApi
@Suppress("EXPOSED_SUPER_INTERFACE")
public interface BuiltInCommand : Command

// for identification
@OptIn(ConsoleExperimentalApi::class)
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

        /**
         * @since 2.8.0
         */
        // for https://github.com/mamoe/mirai-console/issues/416
        @JvmStatic
        public fun generateDefaultHelp(permitteeId: PermitteeId): String {
            return allRegisteredCommands
                .asSequence()
                .filter { permitteeId.hasPermission(it.permission) }
                .joinToString("\n\n") { command ->
                    val lines = command.usage.lines()
                    if (lines.isEmpty()) "/${command.primaryName} ${command.description}"
                    else
                        "◆ " + lines.first() + "\n" + lines.drop(1).joinToString("\n") { "  $it" }
                }.lines().filterNot(String::isBlank).joinToString("\n")
        }

        @Handler
        public suspend fun CommandSender.handle() {
            sendMessage(generateDefaultHelp(this.permitteeId))
        }
    }

    public object StopCommand : SimpleCommand(
        ConsoleCommandOwner, "stop", "shutdown", "exit",
        description = "关闭 Mirai Console",
    ), BuiltInCommandInternal {

        private val closingLock = Mutex()

        @OptIn(
            DelicateCoroutinesApi::class, ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class,
            ConsoleInternalApi::class
        )
        @Handler
        public suspend fun CommandSender.handle() {
            GlobalScope.launch {
                kotlin.runCatching {
                    closingLock.withLock {
                        if (!MiraiConsole.isActive) return@withLock
                        sendMessage("Stopping mirai-console")
                        kotlin.runCatching {
                            MiraiConsoleImplementation.shutdown()
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

    public object LogoutCommand : SimpleCommand(
        ConsoleCommandOwner, "logout", "登出",
        description = "登出一个账号",
    ), BuiltInCommandInternal {

        @OptIn(ConsoleExperimentalApi::class)
        @Handler
        public suspend fun CommandSender.handle(
            @Name("qq") id: Long
        ) {
            if (Bot.getInstanceOrNull(id)?.close() == null) {
                sendMessage("$id 未登录")
            } else {
                sendMessage("$id 已登出")
            }
        }
    }

    private val loginCommandInstance = LoginCommandImpl()

    public object LoginCommand : SimpleCommand(
        ConsoleCommandOwner, loginCommandInstance.primaryName, * loginCommandInstance.secondaryNames,
        description = loginCommandInstance.description,
    ), BuiltInCommandInternal {

        @OptIn(ConsoleExperimentalApi::class)
        @Handler
        @JvmOverloads
        public suspend fun CommandSender.handle(
            @Name("qq") id: Long,
            password: String? = null,
            protocol: BotConfiguration.MiraiProtocol? = null,
        ) {
            loginCommandInstance.run {
                handle(id, password, protocol)
            }
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

        /* 用于解析权限继承关系 */
        private class PermTree(
            val perm: Permission,
            val sub: MutableList<PermissionId> = mutableListOf(),
            var linked: Boolean = false,
            var implicit: Boolean = false,
            val source: MutableList<PermitteeId> = mutableListOf(),
        ) {
            companion object {
                fun sortView(view: PermTree) {
                    view.sub.sortWith { p1, p2 ->
                        val namespaceCompare = p1.namespace compareTo p2.namespace
                        if (namespaceCompare != 0) return@sortWith namespaceCompare

                        if (p1.name == p2.name) return@sortWith 0 // ?
                        if (p1.name == "*") return@sortWith -1
                        if (p2.name == "*") return@sortWith 1

                        return@sortWith p1.name compareTo p2.name
                    }
                }
            }
        }

        private fun renderDepth(depth: Int, sb: AnsiMessageBuilder) {
            repeat(depth) { sb.append(" | ") }
        }


        @OptIn(ConsoleExperimentalApi::class)
        @Description("授权一个权限")
        @SubCommand("permit", "grant", "add")
        public suspend fun CommandSender.permit(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("权限 ID") permission: Permission,
        ) {
            target.permit(permission)
            sendMessage("OK")
        }

        @OptIn(ConsoleExperimentalApi::class)
        @Description("撤销一个权限")
        @SubCommand("cancel", "deny", "remove")
        public suspend fun CommandSender.cancel(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("权限 ID") permission: Permission,
        ) {
            target.cancel(permission, false)
            sendMessage("OK")
        }

        @OptIn(ConsoleExperimentalApi::class)
        @Description("撤销一个权限及其所有子权限")
        @SubCommand("cancelAll", "denyAll", "removeAll")
        public suspend fun CommandSender.cancelAll(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("权限 ID") permission: Permission,
        ) {
            target.cancel(permission, true)
            sendMessage("OK")
        }

        @OptIn(ConsoleExperimentalApi::class)
        @Description("查看被授权权限列表")
        @SubCommand("permittedPermissions", "pp", "grantedPermissions", "gp")
        public suspend fun CommandSender.permittedPermissions(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("显示全部") all: Boolean = true,
        ) {
            val grantedPermissions = target.getPermittedPermissionsAndSource().toList()
            if (grantedPermissions.isEmpty()) {
                sendMessage("${target.asString()} 未被授予任何权限. 使用 `${CommandManager.commandPrefix}permission grant` 给予权限.")
            } else if (all) {
                val allPermissions = PermissionService.INSTANCE.getRegisteredPermissions().toList()
                val permMapping = mutableMapOf<PermissionId, PermTree>()
                grantedPermissions.forEach { (source, granted) ->
                    val m = permMapping[granted.id] ?: kotlin.run {
                        PermTree(granted).also { it.implicit = false; permMapping[granted.id] = it }
                    }
                    m.source.add(source)
                }
                val root = PermissionService.INSTANCE.rootPermission
                fun linkPmTree(permTree: PermTree) {
                    allPermissions.forEach { perm ->
                        if (perm.id == root.id) return@forEach
                        if (perm.parent.id == permTree.perm.id) {
                            permTree.sub.add(perm.id)
                            val subp = permMapping[perm.id] ?: kotlin.run {
                                val p = PermTree(perm)
                                p.implicit = true
                                permMapping[perm.id] = p
                                linkPmTree(p)
                                p
                            }
                            subp.linked = true
                        }
                    }
                }
                permMapping.values.toList().forEach { linkPmTree(it) }
                permMapping.values.forEach { PermTree.sortView(it) }

                @Suppress("LocalVariableName")
                val BG_BLACK = "\u001B[40m"
                fun render(depth: Int, view: PermTree, sb: AnsiMessageBuilder) {
                    if (view.implicit) {
                        sb.gray()
                        sb.append(view.perm.id)
                        sb.append(" (implicit)\n")
                        sb.reset().white().ansi(BG_BLACK)
                    } else {
                        sb.append(view.perm.id)
                        if (view.source.isNotEmpty()) {
                            sb.append(' ').gray().append('(')
                            sb.append("from ")
                            view.source.joinTo(sb)
                            sb.append(')').reset().white().ansi(BG_BLACK)
                        }
                        sb.append('\n')
                    }
                    view.sub.forEach { sub ->
                        val subView = permMapping[sub] ?: error("Error in resolving $sub")
                        renderDepth(depth, sb)
                        sb.append(" |- ")
                        render(depth + 1, subView, sb)
                    }
                }
                sendAnsiMessage {
                    ansi(BG_BLACK).white()
                    permMapping.forEach { (_, tree) ->
                        if (!tree.linked) {
                            render(0, tree, this)
                        }
                    }
                }
            } else {
                sendMessage(grantedPermissions.map { it.second.id }.toSet().joinToString("\n"))
            }
        }

        @Description("查看所有权限列表")
        @SubCommand("listPermissions", "lp")
        public suspend fun CommandSender.listPermissions() {

            val rootView = PermTree(PermissionService.INSTANCE.rootPermission)
            val mappings = mutableMapOf<PermissionId, PermTree>()
            val permListSnapshot = PermissionService.INSTANCE.getRegisteredPermissions().toList()

            permListSnapshot.forEach { perm ->
                mappings[perm.id] = PermTree(perm)
            }
            mappings[rootView.perm.id] = rootView

            permListSnapshot.forEach { perm ->
                if (perm.id == rootView.perm.id) return@forEach

                val parentView = mappings[perm.parent.id] ?: error("Can't find parent of ${perm.id}: ${perm.parent.id}")
                parentView.sub.add(perm.id)
            }

            mappings.values.forEach { PermTree.sortView(it) }

            //*:*
            // |  `-
            // |- .....
            // |  |  `-
            // |  |- ......
            // |  |  |  `-
            // |  |  |-
            val prefixed = 50

            fun render(depth: Int, view: PermTree, sb: AnsiMessageBuilder) {
                kotlin.run { // render perm id
                    var doReset = false
                    val permId = view.perm.id
                    if (permId == rootView.perm.id || permId.name.endsWith("*")) {
                        doReset = true
                        sb.red()
                    }
                    sb.append(permId)
                    if (doReset) {
                        sb.reset()
                    }
                }

                val linePrefixLen =
                    (depth * 3) + 1 + view.perm.id.let { it.name.length + it.namespace.length } + (if (depth == 0) 0 else 1)
                val descFlatten = view.perm.description.replace("\r\n", "\n").replace("\r", "\n")
                if (!descFlatten.contains('\n') && linePrefixLen < prefixed) {
                    if (descFlatten.isNotBlank()) {
                        repeat(prefixed - linePrefixLen) { sb.append(' ') }
                        sb.append("    ").append(descFlatten)
                    }
                } else {
                    descFlatten.splitToSequence('\n').forEach { line ->
                        sb.append('\n')
                        renderDepth(depth, sb)
                        sb.append(" |  `- ").append(line)
                    }
                }
                sb.append('\n')

                view.sub.forEach { sub ->
                    val subView = mappings[sub] ?: return@forEach
                    renderDepth(depth, sb)
                    sb.append(" |- ")
                    render(depth + 1, subView, sb)
                }
            }
            sendAnsiMessage {
                render(0, rootView, this)
            }
        }
    }


    public object AutoLoginCommand : CompositeCommand(
        ConsoleCommandOwner, "autoLogin", "自动登录",
        description = "自动登录设置",
        overrideContext = buildCommandArgumentContext {
            @OptIn(ConsoleExperimentalApi::class)
            ConfigurationKey::class with ConfigurationKey.Parser
        }
    ), BuiltInCommandInternal {
        @Description("查看自动登录账号列表")
        @SubCommand
        @OptIn(ConsoleExperimentalApi::class)
        public suspend fun CommandSender.list() {
            val config = DataScope.get<AutoLoginConfig>()
            sendMessage(buildString {
                for (account in config.accounts) {
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

        @Description("添加自动登录, passwordKind 可选 PLAIN 或 MD5")
        @SubCommand
        @ConsoleExperimentalApi
        public suspend fun CommandSender.add(account: Long, password: String, passwordKind: PasswordKind = PLAIN) {
            val config = DataScope.get<AutoLoginConfig>()
            val accountStr = account.toString()
            if (config.accounts.any { it.account == accountStr }) {
                sendMessage("已有相同账号在自动登录配置中. 请先删除该账号.")
                return
            }
            config.accounts.add(AutoLoginConfig.Account(accountStr, Password(passwordKind, password)))
            sendMessage("已成功添加 '$account'.")
        }

        @OptIn(ConsoleExperimentalApi::class)
        @Description("清除所有配置")
        @SubCommand
        public suspend fun CommandSender.clear() {
            val config = DataScope.get<AutoLoginConfig>()
            config.accounts.clear()
            sendMessage("已清除所有自动登录配置.")
        }

        @OptIn(ConsoleExperimentalApi::class)
        @Description("删除一个账号")
        @SubCommand
        public suspend fun CommandSender.remove(account: Long) {
            val config = DataScope.get<AutoLoginConfig>()
            val accountStr = account.toString()
            if (config.accounts.removeIf { it.account == accountStr }) {
                sendMessage("已成功删除 '$account'.")
                return
            }
            sendMessage("账号 '$account' 未配置自动登录.")
        }

        @ConsoleExperimentalApi
        @Description("设置一个账号的一个配置项")
        @SubCommand
        public suspend fun CommandSender.setConfig(account: Long, configKey: ConfigurationKey, value: String) {
            val config = DataScope.get<AutoLoginConfig>()
            val accountStr = account.toString()

            val oldAccount = config.accounts.find { it.account == accountStr } ?: kotlin.run {
                sendMessage("未找到账号 $account.")
                return
            }

            if (value.isEmpty()) return removeConfig(account, configKey)

            val newAccount = oldAccount.copy(configuration = oldAccount.configuration.toMutableMap().apply {
                put(configKey, value)
            })

            config.accounts.remove(oldAccount)
            config.accounts.add(newAccount)

            sendMessage("成功修改 '$account' 的配置 '$configKey' 为 '$value'")
        }

        @Description("删除一个账号的一个配置项")
        @ConsoleExperimentalApi
        @SubCommand
        public suspend fun CommandSender.removeConfig(account: Long, configKey: ConfigurationKey) {
            val config = DataScope.get<AutoLoginConfig>()
            val accountStr = account.toString()

            val oldAccount = config.accounts.find { it.account == accountStr } ?: kotlin.run {
                sendMessage("未找到账号 $account.")
                return
            }

            val newAccount = oldAccount.copy(configuration = oldAccount.configuration.toMutableMap().apply {
                remove(configKey)
            })

            config.accounts.remove(oldAccount)
            config.accounts.add(newAccount)

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

        private val memoryUsageGet: MemoryUsageGet = kotlin.runCatching {
            ByMemoryMXBean
        }.getOrElse { ByRuntime }

        internal object ByMemoryMXBean : MemoryUsageGet {
            private val memoryMXBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()
            private val MemoryUsage.m: MUsage
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

        @OptIn(MiraiExperimentalApi::class)
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
                append(MiraiConsoleImplementation.getInstance().frontEndDescription.render()).append("\n\n")
                append("Permission Service: ").append(
                    if (PermissionService.INSTANCE is BuiltInPermissionService) {
                        lightYellow()
                        "Built In Permission Service"
                    } else {
                        val plugin = GlobalComponentStorage.getPreferredExtension(PermissionServiceProvider).plugin
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

                val resolvedPlugins = MiraiConsole.pluginManagerImpl.resolvedPlugins.asSequence()
                    .filter { it !is MiraiConsoleAsPlugin } // skip mirai-console in status
                    .toList()

                if (resolvedPlugins.isEmpty()) {
                    gray().append("<none>")
                } else {
                    resolvedPlugins.joinTo(this) { plugin ->
                        if (plugin.isEnabled) {
                            green().append(plugin.name).reset().append(" v").gold()
                        } else {
                            red().append(plugin.name)
                            if (plugin is JvmPluginInternal) {
                                append("(").append(plugin.currentPluginStatus.name.lowercase()).append(")")
                            } else {
                                append("(disabled)")
                            }
                            reset().append(" v").gold()
                        }
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

        private fun calculateMax(
            vararg lines: Array<String>
        ): IntArray = IntArray(lines[0].size) { r ->
            lines.maxOf { it[r].length }
        }
    }
}
