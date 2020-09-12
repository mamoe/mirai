/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.command

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import java.util.concurrent.locks.ReentrantLock

internal object CommandManagerImpl : CommandManager, CoroutineScope by CoroutineScope(MiraiConsole.job) {
    private val logger: MiraiLogger by lazy {
        MiraiConsole.createLogger("command")
    }

    @JvmField
    internal val registeredCommands: MutableList<Command> = mutableListOf()

    @JvmField
    internal val requiredPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val optionalPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val modifyLock = ReentrantLock()


    /**
     * 从原始的 command 中解析出 Command 对象
     */
    internal fun matchCommand(rawCommand: String): Command? {
        if (rawCommand.startsWith(commandPrefix)) {
            return requiredPrefixCommandMap[rawCommand.substringAfter(commandPrefix).toLowerCase()]
        }
        return optionalPrefixCommandMap[rawCommand.toLowerCase()]
    }

    internal val commandListener: Listener<MessageEvent> by lazy {
        subscribeAlways(
            coroutineContext = CoroutineExceptionHandler { _, throwable ->
                logger.error(throwable)
            },
            concurrency = Listener.ConcurrencyKind.CONCURRENT,
            priority = Listener.EventPriority.HIGH
        ) {
            val sender = this.toCommandSender()

            when (val result = sender.executeCommand(message)) {
                is CommandExecuteResult.PermissionDenied -> {
                    if (!result.command.prefixOptional || message.content.startsWith(CommandManager.commandPrefix)) {
                        sender.sendMessage("权限不足")
                        intercept()
                    }
                }
                is CommandExecuteResult.Success -> {
                    intercept()
                }
                is CommandExecuteResult.ExecutionFailed -> {
                    sender.catchExecutionException(result.exception)
                    intercept()
                }
                is CommandExecuteResult.CommandNotFound -> {
                    // noop
                }
            }
        }
    }


    ///// IMPL


    override val CommandOwner.registeredCommands: List<Command> get() = CommandManagerImpl.registeredCommands.filter { it.owner == this }
    override val allRegisteredCommands: List<Command> get() = registeredCommands.toList() // copy
    override val commandPrefix: String get() = "/"
    override fun CommandOwner.unregisterAllCommands() {
        for (registeredCommand in registeredCommands) {
            registeredCommand.unregister()
        }
    }

    override fun Command.register(override: Boolean): Boolean {
        if (this is CompositeCommand) this.subCommands // init lazy
        this.permission // init lazy
        this.names // init lazy
        this.description // init lazy
        this.usage // init lazy

        modifyLock.withLock {
            if (!override) {
                if (findDuplicate() != null) return false
            }
            registeredCommands.add(this@register)
            if (this.prefixOptional) {
                for (name in this.names) {
                    val lowerCaseName = name.toLowerCase()
                    optionalPrefixCommandMap[lowerCaseName] = this
                    requiredPrefixCommandMap[lowerCaseName] = this
                }
            } else {
                for (name in this.names) {
                    val lowerCaseName = name.toLowerCase()
                    optionalPrefixCommandMap.remove(lowerCaseName) // ensure resolution consistency
                    requiredPrefixCommandMap[lowerCaseName] = this
                }
            }
            return true
        }
    }

    override fun Command.findDuplicate(): Command? =
        registeredCommands.firstOrNull { it.names intersectsIgnoringCase this.names }

    override fun Command.unregister(): Boolean = modifyLock.withLock {
        if (this.prefixOptional) {
            this.names.forEach {
                optionalPrefixCommandMap.remove(it)
            }
        }
        this.names.forEach {
            requiredPrefixCommandMap.remove(it)
        }
        registeredCommands.remove(this)
    }

    override fun Command.isRegistered(): Boolean = this in registeredCommands

    override suspend fun Command.execute(
        sender: CommandSender,
        arguments: Message,
        checkPermission: Boolean
    ): CommandExecuteResult {
        return sender.executeCommandInternal(
            this,
            arguments.flattenCommandComponents(),
            primaryName,
            checkPermission
        )
    }

    override suspend fun Command.execute(
        sender: CommandSender,
        arguments: String,
        checkPermission: Boolean
    ): CommandExecuteResult {
        return sender.executeCommandInternal(
            this,
            arguments.flattenCommandComponents(),
            primaryName,
            checkPermission
        )
    }

    override suspend fun CommandSender.executeCommand(
        message: Message,
        checkPermission: Boolean
    ): CommandExecuteResult {
        val msg = message.asMessageChain().filterIsInstance<MessageContent>()
        if (msg.isEmpty()) return CommandExecuteResult.CommandNotFound("")
        return executeCommandInternal(msg, msg[0].content.substringBefore(' '), checkPermission)
    }

    override suspend fun CommandSender.executeCommand(message: String, checkPermission: Boolean): CommandExecuteResult {
        if (message.isBlank()) return CommandExecuteResult.CommandNotFound("")
        return executeCommandInternal(message, message.substringBefore(' '), checkPermission)
    }
}