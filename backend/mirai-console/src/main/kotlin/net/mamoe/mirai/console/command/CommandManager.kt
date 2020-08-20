/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "NOTHING_TO_INLINE", "unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE",
    "MemberVisibilityCanBePrivate"
)
@file:JvmName("CommandManagerKt")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.command.CommandManagerImpl.unregisterAllCommands
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain

public interface CommandManager {
    /**
     * 获取已经注册了的属于这个 [CommandOwner] 的指令列表.
     */
    public val CommandOwner.registeredCommands: List<Command>

    /**
     * 获取所有已经注册了指令列表.
     */
    public val allRegisteredCommands: List<Command>

    /**
     * 指令前缀, 如 '/'
     */
    public val commandPrefix: String

    /**
     * 取消注册所有属于 [this] 的指令
     */
    public fun CommandOwner.unregisterAllCommands()

    /**
     * 注册一个指令.
     *
     * @param override 是否覆盖重名指令.
     *
     * 若原有指令 P, 其 [Command.names] 为 'a', 'b', 'c'.
     * 新指令 Q, 其 [Command.names] 为 'b', 将会覆盖原指令 A 注册的 'b'.
     *
     * 即注册完成后, 'a' 和 'c' 将会解析到指令 P, 而 'b' 会解析到指令 Q.
     *
     * @return
     * 若已有重名指令, 且 [override] 为 `false`, 返回 `false`;
     * 若已有重名指令, 但 [override] 为 `true`, 覆盖原有指令并返回 `true`.
     */
    public fun Command.register(override: Boolean = false): Boolean

    /**
     * 查找并返回重名的指令. 返回重名指令.
     */
    public fun Command.findDuplicate(): Command?

    /**
     * 取消注册这个指令. 若指令未注册, 返回 `false`.
     */
    public fun Command.unregister(): Boolean

    /**
     * 当 [this] 已经 [注册][register] 后返回 `true`
     */
    public fun Command.isRegistered(): Boolean

    /**
     * 解析并执行一个指令. 将会检查指令权限, 在无权限时抛出
     *
     * @param messages 接受 [String] 或 [Message], 其他对象将会被 [Any.toString]
     *
     * @return 成功执行的指令, 在无匹配指令时返回 `null`
     * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
     */
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommand(vararg messages: Any): Command?

    /**
     * 解析并执行一个指令
     *
     * @return 成功执行的指令, 在无匹配指令时返回 `null`
     * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
     */
    @Throws(CommandExecutionException::class)
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommand(message: MessageChain): Command?

    /**
     * 执行一个指令
     *
     * @return 成功执行的指令, 在无匹配指令时返回 `null`
     * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
     */
    @JvmBlockingBridge
    @Throws(CommandExecutionException::class)
    public suspend fun Command.execute(sender: CommandSender, args: MessageChain, checkPermission: Boolean = true)

    /**
     * 执行一个指令
     *
     * @return 成功执行的指令, 在无匹配指令时返回 `null`
     * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
     */
    @JvmBlockingBridge
    @Throws(CommandExecutionException::class)
    public suspend fun Command.execute(sender: CommandSender, vararg args: Any, checkPermission: Boolean = true)

    /**
     * 解析并执行一个指令, 获取详细的指令参数等信息
     *
     * @param messages 接受 [String] 或 [Message], 其他对象将会被 [Any.toString]
     *
     * @return 执行结果
     */
    @ConsoleExperimentalAPI
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommandDetailed(vararg messages: Any): CommandExecuteResult

    /**
     * 解析并执行一个指令, 获取详细的指令参数等信息
     *
     * 执行过程中产生的异常将不会直接抛出, 而会包装为 [CommandExecuteResult.ExecutionException]
     *
     * @return 执行结果
     */
    @ConsoleExperimentalAPI
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommandDetailed(messages: MessageChain): CommandExecuteResult

    public companion object INSTANCE : CommandManager by CommandManagerImpl {
        override val CommandOwner.registeredCommands: List<Command> get() = CommandManagerImpl.run { registeredCommands }
        override fun CommandOwner.unregisterAllCommands(): Unit = CommandManagerImpl.run { unregisterAllCommands() }
        override fun Command.register(override: Boolean): Boolean = CommandManagerImpl.run { register(override) }
        override fun Command.findDuplicate(): Command? = CommandManagerImpl.run { findDuplicate() }
        override fun Command.unregister(): Boolean = CommandManagerImpl.run { unregister() }
        override fun Command.isRegistered(): Boolean = CommandManagerImpl.run { isRegistered() }
        override suspend fun CommandSender.executeCommand(vararg messages: Any): Command? =
            CommandManagerImpl.run { executeCommand(*messages) }

        override suspend fun CommandSender.executeCommand(message: MessageChain): Command? =
            CommandManagerImpl.run { executeCommand(message) }

        override val commandPrefix: String
            get() = CommandManagerImpl.commandPrefix

        override suspend fun Command.execute(
            sender: CommandSender,
            args: MessageChain,
            checkPermission: Boolean
        ): Unit =
            CommandManagerImpl.run { execute(sender, args = args, checkPermission = checkPermission) }

        override suspend fun Command.execute(sender: CommandSender, vararg args: Any, checkPermission: Boolean): Unit =
            CommandManagerImpl.run { execute(sender, args = args, checkPermission = checkPermission) }

        @ConsoleExperimentalAPI
        override suspend fun CommandSender.executeCommandDetailed(vararg messages: Any): CommandExecuteResult =
            CommandManagerImpl.run { executeCommandDetailed(*messages) }

        @ConsoleExperimentalAPI
        override suspend fun CommandSender.executeCommandDetailed(messages: MessageChain): CommandExecuteResult =
            CommandManagerImpl.run { executeCommandDetailed(messages) }
    }
}

/**
 * 指令的所有者.
 * @see PluginCommandOwner
 */
public sealed class CommandOwner

/**
 * 插件指令所有者. 插件只能通过 [PluginCommandOwner] 管理指令.
 */
public abstract class PluginCommandOwner(
    public val plugin: Plugin
) : CommandOwner() {
    init {
        if (plugin is CoroutineScope) { // JVM Plugin
            plugin.coroutineContext[Job]?.invokeOnCompletion {
                this.unregisterAllCommands()
            }
        }
    }
}

/**
 * 代表控制台所有者. 所有的 mirai-console 内建的指令都属于 [ConsoleCommandOwner].
 */
public object ConsoleCommandOwner : CommandOwner()
