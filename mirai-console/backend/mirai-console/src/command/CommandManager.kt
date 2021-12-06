/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress(
    "NOTHING_TO_INLINE", "unused",
    "MemberVisibilityCanBePrivate", "INAPPLICABLE_JVM_NAME"
)
@file:JvmName("CommandManagerKt")

package net.mamoe.mirai.console.command

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandCallParser
import net.mamoe.mirai.console.command.resolve.CommandCallResolver
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.CommandManagerImpl.executeCommand
import net.mamoe.mirai.console.internal.command.executeCommandImpl
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.*

/**
 * 指令管理器
 */
public interface CommandManager {
    /**
     * 获取所有已经注册了指令列表.
     *
     * @return 这一时刻的浅拷贝.
     */
    public val allRegisteredCommands: List<Command>

    /**
     * 指令前缀, 如 '/'
     */
    public val commandPrefix: String

    /**
     * 获取已经注册了的属于这个 [CommandOwner] 的指令列表.
     *
     * @return 这一时刻的浅拷贝.
     */
    public fun getRegisteredCommands(owner: CommandOwner): List<Command>


    /**
     * 取消注册所有属于 [owner] 的指令
     */
    public fun unregisterAllCommands(owner: CommandOwner)

    /**
     * 注册一个指令.
     *
     * @param override 是否覆盖重名指令.
     *
     * 若原有指令 P, 其 [Command.secondaryNames] 为 'a', 'b', 'c'.
     * 新指令 Q, 其 [Command.secondaryNames] 为 'b', 将会覆盖原指令 A 注册的 'b'.
     *
     * 即注册完成后, 'a' 和 'c' 将会解析到指令 P, 而 'b' 会解析到指令 Q.
     *
     * @return
     * 若已有重名指令, 且 [override] 为 `false`, 返回 `false`;
     * 若已有重名指令, 但 [override] 为 `true`, 覆盖原有指令并返回 `true`.
     *
     *
     * 注意: [内建指令][BuiltInCommands] 也可以被覆盖.
     */
    public fun registerCommand(command: Command, override: Boolean = false): Boolean

    /**
     * 查找并返回重名的指令. 返回重名指令.
     */
    public fun findDuplicateCommand(command: Command): Command?

    /**
     * 取消注册这个指令.
     *
     * 若指令未注册, 返回 `false`.
     */
    public fun unregisterCommand(command: Command): Boolean

    /**
     * 当 [command] 已经 [注册][registerCommand] 时返回 `true`
     */
    public fun isCommandRegistered(command: Command): Boolean

    /**
     * 解析并执行一个指令.
     *
     * ### 指令解析流程
     * 1. [CommandCallParser] 将 [MessageChain] 解析为 [CommandCall]
     * 2. [CommandCallResolver] 将 [CommandCall] 解析为 [ResolvedCommandCall]
     * 1. [message] 的第一个消息元素的 [内容][Message.contentToString] 被作为指令名, 在已注册指令列表中搜索. (包含 [Command.prefixOptional] 相关的处理)
     * 2. 参数语法分析.
     *   在当前的实现下, [message] 被以空格和 [SingleMessage] 分割.
     *   如 "MessageChain("foo bar", [Image], " test")" 被分割为 "foo", "bar", [Image], "test".
     *   注意: 字符串与消息元素之间不需要空格, 会被强制分割. 如 "bar[mirai:image:]" 会被分割为 "bar" 和 [Image] 类型的消息元素.
     * 3. 参数解析. 各类型指令实现不同. 详见 [RawCommand], [CompositeCommand], [SimpleCommand]
     *
     * ### 扩展
     * 参数语法分析过程可能会被扩展, 插件可以自定义处理方式 ([CommandCallParser]), 因此可能不会简单地使用空格分隔.
     *
     * @param message 一条完整的指令. 如 "/managers add 123456.123456"
     * @param checkPermission 为 `true` 时检查权限
     *
     * @see CommandCallParser
     * @see CommandCallResolver
     *
     * @see CommandSender.executeCommand
     * @see Command.execute
     *
     * @return 执行结果
     */
    @ExperimentalCommandDescriptors
    @JvmBlockingBridge
    public suspend fun executeCommand(
        caller: CommandSender,
        message: Message,
        checkPermission: Boolean = true,
    ): CommandExecuteResult {
        return executeCommandImpl(message, caller, checkPermission)
    }

    /**
     * 执行一个确切的指令
     *
     * @param command 目标指令
     * @param arguments 参数列表
     *
     * @see executeCommand 获取更多信息
     * @see Command.execute
     */
    @ConsoleExperimentalApi
    @JvmName("executeCommand")
    @ExperimentalCommandDescriptors
    @JvmSynthetic
    public suspend fun executeCommand(
        sender: CommandSender,
        command: Command,
        arguments: Message = EmptyMessageChain,
        checkPermission: Boolean = true,
    ): CommandExecuteResult {
        // TODO: 2020/10/18  net.mamoe.mirai.console.command.CommandManager.execute
        val chain = buildMessageChain {
            append(CommandManager.commandPrefix)
            append(command.primaryName)
            append(' ')
            append(arguments)
        }
        return CommandManager.executeCommand(sender, chain, checkPermission)
    }

    /**
     * 从 [指令名称][commandName] 匹配对应的 [Command].
     *
     * #### 实现细节
     * - [commandName] 带有 [commandPrefix] 时可以匹配到所有指令
     * - [commandName] 不带有 [commandPrefix] 时只能匹配到 [Command.prefixOptional] 的指令
     *
     * @param commandName 可能带有或不带有 [commandPrefix].
     */
    public fun matchCommand(commandName: String): Command?

    public companion object INSTANCE : CommandManager by CommandManagerImpl {

        /**
         * @see CommandManager.getRegisteredCommands
         */
        @get:JvmName("registeredCommands0")
        @get:JvmSynthetic
        public inline val CommandOwner.registeredCommands: List<Command>
            get() = getRegisteredCommands(this)

        /**
         * @see CommandManager.registerCommand
         */
        @JvmSynthetic
        public inline fun Command.register(override: Boolean = false): Boolean = registerCommand(this, override)

        /**
         * @see CommandManager.unregisterCommand
         */
        @JvmSynthetic
        public inline fun Command.unregister(): Boolean = unregisterCommand(this)

        /**
         * @see CommandManager.isCommandRegistered
         */
        @get:JvmSynthetic
        public inline val Command.isRegistered: Boolean
            get() = isCommandRegistered(this)

        /**
         * @see CommandManager.unregisterAll
         */
        @JvmSynthetic
        public inline fun CommandOwner.unregisterAll(): Unit = unregisterAllCommands(this)

        /**
         * @see CommandManager.findDuplicate
         */
        @JvmSynthetic
        public inline fun Command.findDuplicate(): Command? = findDuplicateCommand(this)

    }
}

/**
 * 解析并执行一个指令
 *
 * @param message 一条完整的指令. 如 "/managers add 123456.123456"
 * @param checkPermission 为 `true` 时检查权限
 *
 * @return 执行结果
 * @see executeCommand
 */
@JvmName("execute0")
@ExperimentalCommandDescriptors
@JvmSynthetic
public suspend inline fun CommandSender.executeCommand(
    message: String,
    checkPermission: Boolean = true,
): CommandExecuteResult = CommandManager.executeCommand(this, PlainText(message).toMessageChain(), checkPermission)


/**
 * 执行一个确切的指令
 * @see executeCommand 获取更多信息
 */
@JvmName("execute0")
@ExperimentalCommandDescriptors
@JvmSynthetic
public suspend inline fun Command.execute(
    sender: CommandSender,
    vararg arguments: Message = emptyArray(),
    checkPermission: Boolean = true,
): CommandExecuteResult = CommandManager.executeCommand(sender, this, arguments.toMessageChain(), checkPermission)

/**
 * 执行一个确切的指令
 * @see executeCommand 获取更多信息
 */
@JvmName("execute0")
@ExperimentalCommandDescriptors
@JvmSynthetic
public suspend inline fun Command.execute(
    sender: CommandSender,
    arguments: String = "",
    checkPermission: Boolean = true,
): CommandExecuteResult = execute(sender, PlainText(arguments), checkPermission = checkPermission)
