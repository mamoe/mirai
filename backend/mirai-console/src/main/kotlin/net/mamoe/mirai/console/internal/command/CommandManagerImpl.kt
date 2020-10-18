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
import net.mamoe.mirai.console.command.Command.Companion.allNames
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import java.util.concurrent.locks.ReentrantLock

internal object CommandManagerImpl : CommandManager, CoroutineScope by MiraiConsole.childScope("CommandManagerImpl") {
    private val logger: MiraiLogger by lazy {
        MiraiConsole.createLogger("command")
    }

    @Suppress("ObjectPropertyName")
    @JvmField
    internal val _registeredCommands: MutableList<Command> = mutableListOf()

    @JvmField
    internal val requiredPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val optionalPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val modifyLock = ReentrantLock()


    /**
     * 从原始的 command 中解析出 Command 对象
     */
    override fun matchCommand(commandName: String): Command? {
        if (commandName.startsWith(commandPrefix)) {
            return requiredPrefixCommandMap[commandName.substringAfter(commandPrefix).toLowerCase()]
        }
        return optionalPrefixCommandMap[commandName.toLowerCase()]
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

            when (val result = executeCommand(sender, message)) {
                is CommandExecuteResult.PermissionDenied -> {
                    if (!result.command.prefixOptional || message.content.startsWith(CommandManager.commandPrefix)) {
                        sender.sendMessage("权限不足")
                        intercept()
                    }
                }
                is CommandExecuteResult.IllegalArgument -> {
                    result.exception.message?.let { sender.sendMessage(it) }
                    intercept()
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


    override val CommandOwner.registeredCommands: List<Command> get() = _registeredCommands.filter { it.owner == this }
    override val allRegisteredCommands: List<Command> get() = _registeredCommands.toList() // copy
    override val commandPrefix: String get() = "/"
    override fun CommandOwner.unregisterAllCommands() {
        for (registeredCommand in registeredCommands) {
            registeredCommand.unregister()
        }
    }

    override fun Command.register(override: Boolean): Boolean {
        if (this is CompositeCommand) this.subCommands // init lazy
        kotlin.runCatching {
            this.permission // init lazy
            this.secondaryNames // init lazy
            this.description // init lazy
            this.usage // init lazy
        }.onFailure {
            throw IllegalStateException("Failed to init command ${this@register}.", it)
        }

        modifyLock.withLock {
            if (!override) {
                if (findDuplicate() != null) return false
            }
            _registeredCommands.add(this@register)
            if (this.prefixOptional) {
                for (name in this.allNames) {
                    val lowerCaseName = name.toLowerCase()
                    optionalPrefixCommandMap[lowerCaseName] = this
                    requiredPrefixCommandMap[lowerCaseName] = this
                }
            } else {
                for (name in this.allNames) {
                    val lowerCaseName = name.toLowerCase()
                    optionalPrefixCommandMap.remove(lowerCaseName) // ensure resolution consistency
                    requiredPrefixCommandMap[lowerCaseName] = this
                }
            }
            return true
        }
    }

    override fun Command.findDuplicate(): Command? =
        _registeredCommands.firstOrNull { it.allNames intersectsIgnoringCase this.allNames }

    override fun Command.unregister(): Boolean = modifyLock.withLock {
        if (this.prefixOptional) {
            this.allNames.forEach {
                optionalPrefixCommandMap.remove(it)
            }
        }
        this.allNames.forEach {
            requiredPrefixCommandMap.remove(it)
        }
        _registeredCommands.remove(this)
    }

    override fun Command.isRegistered(): Boolean = this in _registeredCommands
}