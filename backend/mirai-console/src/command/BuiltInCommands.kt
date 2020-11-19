/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser.Companion.map
import net.mamoe.mirai.console.command.descriptor.PermissionIdValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.PermitteeIdValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.buildCommandArgumentContext
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.CommandManagerImpl.allRegisteredCommands
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.*
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.PasswordKind.PLAIN
import net.mamoe.mirai.console.internal.util.runIgnoreException
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.cancel
import net.mamoe.mirai.console.permission.PermissionService.Companion.findCorrespondingPermissionOrFail
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.message.nextMessageOrNull
import net.mamoe.mirai.utils.secondsToMillis
import kotlin.concurrent.thread
import kotlin.system.exitProcess


@ConsoleExperimentalApi
@Suppress("EXPOSED_SUPER_INTERFACE")
public interface BuiltInCommand : Command

// for identification
internal interface BuiltInCommandInternal : Command, BuiltInCommand

/**
 * 内建指令列表
 */
@Suppress("unused")
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

        @Handler
        public suspend fun CommandSender.handle() {
            GlobalScope.launch {
                kotlin.runCatching {
                    closingLock.withLock {
                        if (!MiraiConsole.isActive) return@withLock
                        sendMessage("Stopping mirai-console")
                        kotlin.runCatching {
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
        @Handler
        public suspend fun CommandSender.handle(@Name("qq") id: Long, password: String) {
            kotlin.runCatching {
                MiraiConsole.addBot(id, password).alsoLogin()
            }.fold(
                onSuccess = { sendMessage("${it.nick} ($id) Login successful") },
                onFailure = { throwable ->
                    sendMessage(
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

        @Description("取消授权一个权限")
        @SubCommand("cancel", "deny", "remove")
        public suspend fun CommandSender.cancel(
            @Name("被许可人 ID") target: PermitteeId,
            @Name("权限 ID") permission: Permission,
        ) {
            target.cancel(permission, false)
            sendMessage("OK")
        }

        @Description("取消授权一个权限及其所有子权限")
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
        ) {
            val grantedPermissions = target.getPermittedPermissions()
            sendMessage(grantedPermissions.joinToString("\n") { it.id.toString() })
        }

        @Description("查看所有权限列表")
        @SubCommand("listPermissions", "lp")
        public suspend fun CommandSender.listPermissions() {
            sendMessage(PermissionService.INSTANCE.getRegisteredPermissions().joinToString("\n") { it.id.toString() })
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
}