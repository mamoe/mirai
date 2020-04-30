/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager.processCommandQueue
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.PluginManager
import java.util.concurrent.Executors

interface CommandOwner

class PluginCommandOwner(val pluginBase: PluginBase):CommandOwner
internal object ConsoleCommandOwner:CommandOwner
fun PluginBase.asCommandOwner() = PluginCommandOwner(this)


object CommandManager : Job by {
    GlobalScope.launch(start = CoroutineStart.LAZY) {
        processCommandQueue()
    }
}() {
    private val registeredCommand: MutableMap<String, Command> = mutableMapOf()
    val commands: Collection<Command> get() = registeredCommand.values
    private val pluginCommands:MutableMap<PluginBase,MutableCollection<Command>> = mutableMapOf()

    internal fun clearPluginsCommands(){
        pluginCommands.values.forEach {a ->
            a.forEach{
                unregister(it)
            }
        }
        pluginCommands.clear()
    }

    internal fun clearPluginCommands(
        pluginBase: PluginBase
    ){
        pluginCommands[pluginBase]?.run {
            this.forEach { unregister(it) }
            this.clear()
        }
    }

    /**
     * 注册这个指令.
     *
     * @throws IllegalStateException 当已注册的指令与 [command] 重名时
     */
    fun register(commandOwner: CommandOwner, command: Command) {
        val allNames = mutableListOf(command.name).also { it.addAll(command.alias) }
        allNames.forEach {
            if (registeredCommand.containsKey(it)) {
                error("Command Name(or Alias) $it is already registered, consider if same functional plugin was installed")
            }
        }
        allNames.forEach {
            registeredCommand[it] = command
        }
        if(commandOwner is PluginCommandOwner){
            pluginCommands.putIfAbsent(commandOwner.pluginBase, mutableSetOf())
            pluginCommands[commandOwner.pluginBase]!!.add(command)
        }
    }

    fun register(pluginBase:PluginBase, command: Command) = CommandManager.register(pluginBase.asCommandOwner(),command)

    fun unregister(command: Command) {
        command.alias.forEach {
            registeredCommand.remove(it)
        }
        registeredCommand.remove(command.name)
    }

    fun unregister(commandName: String): Boolean {
        return registeredCommand.remove(commandName) != null
    }


    /**
     * 最基础的执行指令的方式
     * 指令将会被加入队列，依次执行
     *
     * @param sender 指令执行者, 可为 [ConsoleCommandSender] 或 [ContactCommandSender]
     */
    fun runCommand(sender: CommandSender, command: String) {
        commandChannel.offer(
            FullCommand(sender, command)
        )
    }

    /**
     * 插队异步执行一个指令并返回 [Deferred]
     *
     * @param sender 指令执行者, 可为 [ConsoleCommandSender] 或 [ContactCommandSender]
     * @see PluginBase.runCommandAsync 扩展
     */
    fun runCommandAsync(pluginBase: PluginBase, sender: CommandSender, command: String): Deferred<Boolean> {
        return pluginBase.async {
            processCommand(sender, command)
        }
    }

    /**
     * 插队执行一个指令并返回 [Deferred]
     *
     * @param sender 指令执行者, 可为 [ConsoleCommandSender] 或 [ContactCommandSender]
     * @see PluginBase.runCommandAsync 扩展
     */
    @Suppress("KDocUnresolvedReference")
    suspend fun dispatchCommand(sender: CommandSender, command: String): Boolean {
        return processCommand(sender, command)
    }


    /**
     * 阻塞当前线程, 插队执行一个指令
     *
     * @param sender 指令执行者, 可为 [ConsoleCommandSender] 或 [ContactCommandSender]
     */
    // for java
    fun dispatchCommandBlocking(sender: CommandSender, command: String): Boolean =
        runBlocking { dispatchCommand(sender, command) }


    // internal

    /**
     * 单线程执行指令
     */
    private val commandDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    private suspend fun processCommand(sender: CommandSender, fullCommand: String): Boolean {
        return withContext(commandDispatcher) {
            processCommandImpl(sender, fullCommand)
        }
    }

    private suspend fun processCommandImpl(sender: CommandSender, fullCommand: String): Boolean {
        val blocks = fullCommand.split(" ")
        val commandHead = blocks[0] //.replace("/", "")
        val args = blocks.drop(1)
        return registeredCommand[commandHead]?.run {
            try {
                return onCommand(sender, ArrayList(args)).also {
                    if (it) {
                        PluginManager.onCommand(this, sender, args)
                    } else {
                        sender.sendMessage(this.usage)
                    }
                }
            } catch (e: Exception) {
                sender.sendMessage("在运行指令时出现了未知错误")
                MiraiConsole.logger(e)
                false
            } finally {
                (sender as AbstractCommandSender).flushMessage()
            }
        } ?: throw UnknownCommandException(commandHead)
    }

    internal class FullCommand(
        val sender: CommandSender,
        val commandStr: String
    )

    private val commandChannel: Channel<FullCommand> = Channel(Channel.UNLIMITED)

    private tailrec suspend fun processCommandQueue() {
        val command = commandChannel.receive()
        try {
            processCommand(command.sender, command.commandStr)
        } catch (e: UnknownCommandException) {
            command.sender.sendMessage("未知指令 " + command.commandStr)
        } catch (e: Throwable) {//should never happen
            MiraiConsole.logger(e)
        }
        if(isActive) {
            processCommandQueue()
        }
    }
}

/**
 * 插队异步执行一个指令并返回 [Deferred]
 *
 * @param sender 指令执行者, 可为 [ConsoleCommandSender] 或 [ContactCommandSender]
 * @see PluginBase.runCommandAsync 扩展
 */
fun PluginBase.runCommandAsync(sender: CommandSender, command: String): Deferred<Boolean> =
    CommandManager.runCommandAsync(this, sender, command)


class UnknownCommandException(command: String) : Exception("unknown command \"$command\"")