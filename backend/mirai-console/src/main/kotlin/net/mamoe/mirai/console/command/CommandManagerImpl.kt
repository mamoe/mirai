/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.internal.command.*
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import java.util.concurrent.locks.ReentrantLock

internal object CommandManagerImpl : CommandManager, CoroutineScope by CoroutineScope(MiraiConsole.job) {
    @JvmField
    internal val registeredCommands: MutableList<Command> = mutableListOf()

    /**
     * 全部注册的指令
     * /mute -> MuteCommand
     * /jinyan -> MuteCommand
     */
    @JvmField
    internal val requiredPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    /**
     * Command name of commands that are prefix optional
     * mute -> MuteCommand
     */
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
        @Suppress("RemoveExplicitTypeArguments")
        subscribeAlways<MessageEvent>(
            concurrency = Listener.ConcurrencyKind.CONCURRENT,
            priority = Listener.EventPriority.HIGH
        ) {
            if (this.sender.asCommandSender().executeCommand(message) != null) {
                intercept()
            }
        }
    }


    ///// IMPL


    override val CommandOwner.registeredCommands: List<Command> get() = this@CommandManagerImpl.registeredCommands.filter { it.owner == this }
    override val allRegisteredCommands: List<Command> get() = registeredCommands.toList() // copy
    override val commandPrefix: String get() = "/"
    override fun CommandOwner.unregisterAllCommands() {
        for (registeredCommand in registeredCommands) {
            registeredCommand.unregister()
        }
    }

    override fun Command.register(override: Boolean): Boolean {
        if (this is CompositeCommand) this.subCommands // init

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

    //// executing without detailed result (faster)
    override suspend fun CommandSender.executeCommand(vararg messages: Any): Command? {
        if (messages.isEmpty()) return null
        return matchAndExecuteCommandInternal(messages, messages[0].toString().substringBefore(' '))
    }

    override suspend fun CommandSender.executeCommand(message: MessageChain): Command? {
        if (message.isEmpty()) return null
        return matchAndExecuteCommandInternal(message, message[0].toString().substringBefore(' '))
    }

    override suspend fun Command.execute(sender: CommandSender, args: MessageChain, checkPermission: Boolean) {
        sender.executeCommandInternal(
            this,
            args.flattenCommandComponents().toTypedArray(),
            this.primaryName,
            checkPermission
        )
    }

    override suspend fun Command.execute(sender: CommandSender, vararg args: Any, checkPermission: Boolean) {
        sender.executeCommandInternal(
            this,
            args.flattenCommandComponents().toTypedArray(),
            this.primaryName,
            checkPermission
        )
    }

    //// execution with detailed result
    @ConsoleExperimentalAPI
    override suspend fun CommandSender.executeCommandDetailed(vararg messages: Any): CommandExecuteResult {
        if (messages.isEmpty()) return CommandExecuteResult.CommandNotFound("")
        return executeCommandDetailedInternal(messages, messages[0].toString().substringBefore(' '))
    }

    @ConsoleExperimentalAPI
    override suspend fun CommandSender.executeCommandDetailed(messages: MessageChain): CommandExecuteResult {
        if (messages.isEmpty()) return CommandExecuteResult.CommandNotFound("")
        return executeCommandDetailedInternal(messages, messages[0].toString())
    }
}