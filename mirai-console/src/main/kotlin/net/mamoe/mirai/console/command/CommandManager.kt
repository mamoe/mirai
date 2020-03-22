/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.console.command.CommandManager.processCommandQueue
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.PluginManager
import java.util.concurrent.Executors


object CommandManager : Job by {
    GlobalScope.launch(start = CoroutineStart.LAZY) {
        processCommandQueue()
    }
}() {
    private val registeredCommand: MutableMap<String, Command> = mutableMapOf()

    val commands: Collection<Command> get() = registeredCommand.values

    fun register(command: Command) {
        val allNames = mutableListOf(command.name).also { it.addAll(command.alias) }
        allNames.forEach {
            if (registeredCommand.containsKey(it)) {
                error("Command Name(or Alias) $it is already registered, consider if same functional plugin was installed")
            }
        }
        allNames.forEach {
            registeredCommand[it] = command
        }
    }

    fun unregister(command: Command) {
        (command.alias.asSequence() + command.name).forEach {
            registeredCommand.remove(it)
        }
    }

    fun unregister(commandName: String): Boolean {
        return registeredCommand.remove(commandName) != null
    }


    /**
     * 指令是单线程运行的
     */
    private val commandDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    /**
     * 执行一个指令, 但是如果你想模拟一个指令的执行
     * 请向下看
     *
     * 返回一个指令是否执行成功
     */
    private suspend fun processCommand(sender: CommandSender, fullCommand: String):Boolean {
        return withContext(commandDispatcher) {
            _processCommand(sender, fullCommand)
        }
    }

    private suspend fun _processCommand(sender: CommandSender, fullCommand: String): Boolean {
        val blocks = fullCommand.split(" ")
        val commandHead = blocks[0].replace("/", "")
        val args = blocks.drop(1)
        return registeredCommand[commandHead]?.run {
            try {
                return onCommand(sender, blocks.drop(1)).also {
                    if (it) {
                        PluginManager.onCommand(this, sender, args)
                    } else {
                        sender.sendMessage(this.usage)
                    }
                }
            }catch (e: Exception){
                sender.sendMessage("在运行指令时出现了未知错误")
                e.printStackTrace()
                false
            }finally {
                (sender as CommandSenderImpl).flushMessage()
            }
        }?: throw UnknownCommandException(commandHead)
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
        }catch (e:UnknownCommandException){
            command.sender.sendMessage("未知指令 " + command.commandStr)
        }catch (e:Throwable){//should never happen
            e.printStackTrace()
        }
        processCommandQueue()
    }

    /**
     * runCommand()是最基础的执行指令的方式
     * 指令将会被加入队列，依次执行
     * 方法由0.27.0的阻塞模式改为不阻塞(鉴于commandChannel大小无限)
     */
    fun runCommand(sender: CommandSender, command: String) {
        runBlocking {//it wont be blocking
            commandChannel.send(
                 FullCommand(sender, command)
            )
        }
    }

    @Suppress("unused")
    fun runConsoleCommand(command: String) = runCommand(ConsoleCommandSender,command)

    /**
     * runCommandAnsyc()执行一个指令并返回deferred
     * 为插队执行
     */
    fun runCommandAsync(pluginBase: PluginBase, sender: CommandSender, command: String):Deferred<Boolean>{
        return pluginBase.async{
            processCommand(sender,command)
        }
    }

    fun runConsoleCommandAsync(pluginBase: PluginBase, command: String):Deferred<Boolean> = runCommandAsync(pluginBase,ConsoleCommandSender,command)

    /**
     * dispatchCommand()执行一个指令并等到完成
     * 为插队执行
     */
    suspend fun dispatchCommand(sender: CommandSender,command: String):Boolean{
        return processCommand(sender,command)
    }

    suspend fun dispatchConsoleCommand(command: String):Boolean = dispatchCommand(ConsoleCommandSender,command)

    fun dispatchCommandBlocking(sender: CommandSender,command: String):Boolean = runBlocking { dispatchCommand(sender, command) }

    fun dispatchConsoleCommandBlocking(command: String):Boolean = runBlocking { dispatchConsoleCommandBlocking(command) }
}

fun PluginBase.runCommandAsnyc(sender: CommandSender, command: String):Deferred<Boolean> = CommandManager.runCommandAsync(this,sender,command)

fun PluginBase.runConsoleCommandAsync(command: String):Deferred<Boolean> = CommandManager.runConsoleCommandAsync(this,command)



class UnknownCommandException(command: String):Exception("unknown command \"$command\"")