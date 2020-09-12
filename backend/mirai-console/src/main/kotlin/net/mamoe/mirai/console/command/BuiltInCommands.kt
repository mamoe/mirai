/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
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
import net.mamoe.mirai.console.command.description.*
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.CommandManagerImpl.allRegisteredCommands
import net.mamoe.mirai.console.internal.util.runIgnoreException
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.denyPermission
import net.mamoe.mirai.console.permission.PermissionService.Companion.findCorrespondingPermissionOrFail
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.permission.PermissionService.Companion.grantPermission
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
@OptIn(ExperimentalPermission::class)
public object BuiltInCommands {
    @ConsoleExperimentalApi
    public val parentPermission: Permission by lazy {
        PermissionService.INSTANCE.register(
            PermissionId("console", "*"),
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
        description = "Command list",
    ), BuiltInCommandInternal {
        @Handler
        public suspend fun CommandSender.handle() {
            sendMessage(
                allRegisteredCommands.joinToString("\n\n") { "◆ ${it.usage}" }.lines().filterNot(String::isBlank)
                    .joinToString("\n")
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
        description = "Stop the whole world.",
    ), BuiltInCommandInternal {

        private val closingLock = Mutex()

        @Handler
        public suspend fun CommandSender.handle() {
            kotlin.runCatching {
                closingLock.withLock {
                    sendMessage("Stopping mirai-console")
                    kotlin.runCatching {
                        runIgnoreException<CancellationException> { MiraiConsole.job.cancelAndJoin() }
                    }.fold(
                        onSuccess = {
                            runIgnoreException<EventCancelledException> { sendMessage("mirai-console stopped successfully.") }
                        },
                        onFailure = {
                            if (it is CancellationException) return@fold
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

    public object LoginCommand : SimpleCommand(
        ConsoleCommandOwner, "login", "登录",
        description = "Log in a bot account.",
    ), BuiltInCommandInternal {
        @Handler
        public suspend fun CommandSender.handle(id: Long, password: String) {
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

    @OptIn(ExperimentalPermission::class)
    public object PermissionCommand : CompositeCommand(
        ConsoleCommandOwner, "permission", "权限", "perm",
        description = "Manage permissions",
        overrideContext = buildCommandArgumentContext {
            PermitteeId::class with PermissibleIdentifierArgumentParser
            Permission::class with PermissionIdArgumentParser.map { id ->
                kotlin.runCatching {
                    id.findCorrespondingPermissionOrFail()
                }.getOrElse { illegalArgument("指令不存在: $id", it) }
            }
        },
    ), BuiltInCommandInternal {
        // TODO: 2020/9/10 improve Permission command
        @SubCommand("permit", "grant", "add")
        public suspend fun CommandSender.permit(target: PermitteeId, permission: Permission) {
            target.grantPermission(permission)
            sendMessage("OK")
        }

        @SubCommand("cancel", "deny", "remove")
        public suspend fun CommandSender.cancel(target: PermitteeId, permission: Permission) {
            target.denyPermission(permission, false)
            sendMessage("OK")
        }

        @SubCommand("cancelAll", "denyAll", "removeAll")
        public suspend fun CommandSender.cancelAll(target: PermitteeId, permission: Permission) {
            target.denyPermission(permission, true)
            sendMessage("OK")
        }

        @SubCommand("permittedPermissions", "pp", "grantedPermissions", "gp")
        public suspend fun CommandSender.permittedPermissions(target: PermitteeId) {
            val grantedPermissions = target.getPermittedPermissions()
            sendMessage(grantedPermissions.joinToString("\n") { it.id.toString() })
        }

        @SubCommand("listPermissions", "lp")
        public suspend fun CommandSender.listPermissions() {
            sendMessage(PermissionService.INSTANCE.getRegisteredPermissions().joinToString("\n") { it.id.toString() })
        }
    }
}