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

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage

/**
 * 指令管理器
 */
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
     *
     *
     * 注意: [内建指令][BuiltInCommands] 也可以被覆盖.
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
     * 执行一个指令
     *
     * @return 成功执行的指令, 在无匹配指令时返回 `null`
     * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
     * @see executeCommand
     */
    @JvmBlockingBridge
    @Throws(CommandExecutionException::class)
    public suspend fun Command.execute(sender: CommandSender, args: MessageChain, checkPermission: Boolean = true)

    /**
     * 执行一个指令
     *
     * @return 成功执行的指令, 在无匹配指令时返回 `null`
     * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
     * @see executeCommand
     */
    @JvmBlockingBridge
    @Throws(CommandExecutionException::class)
    public suspend fun Command.execute(sender: CommandSender, vararg args: Any, checkPermission: Boolean = true)

    /**
     * 解析并执行一个指令, 获取详细的指令参数等信息
     *
     * 执行过程中产生的异常将不会直接抛出, 而会包装为 [CommandExecuteResult.ExecutionFailed]
     *
     * ### 指令解析流程
     * 1. [messages] 的第一个消息元素的 [内容][Message.contentToString] 被作为指令名, 在已注册指令列表中搜索. (包含 [Command.prefixOptional] 相关的处理)
     * 2. 参数语法分析.
     *   在当前的实现下, [messages] 被以空格和 [SingleMessage] 分割.
     *   如 "MessageChain("foo bar", [Image], " test")" 被分割为 "foo", "bar", [Image], "test".
     *   注意: 字符串与消息元素之间不需要空格, 会被强制分割. 如 "bar[mirai:image:]" 会被分割为 "bar" 和 [Image] 类型的消息元素.
     * 3. 参数解析. 各类型指令实现不同. 详见 [RawCommand], [CompositeCommand], [SimpleCommand]
     *
     * @param messages 接受 [String] 或 [Message], 其他对象将会被 [Any.toString]
     *
     * @return 执行结果
     */
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommand(vararg messages: Any): CommandExecuteResult

    /**
     * 解析并执行一个指令, 获取详细的指令参数等信息
     *
     * 执行过程中产生的异常将不会直接抛出, 而会包装为 [CommandExecuteResult.ExecutionFailed]
     *
     * @return 执行结果
     */
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommand(messages: MessageChain): CommandExecuteResult

    public companion object INSTANCE : CommandManager by CommandManagerImpl {
        // TODO: 2020/8/20 https://youtrack.jetbrains.com/issue/KT-41191

        override val CommandOwner.registeredCommands: List<Command> get() = CommandManagerImpl.run { registeredCommands }
        override fun CommandOwner.unregisterAllCommands(): Unit = CommandManagerImpl.run { unregisterAllCommands() }
        override fun Command.register(override: Boolean): Boolean = CommandManagerImpl.run { register(override) }
        override fun Command.findDuplicate(): Command? = CommandManagerImpl.run { findDuplicate() }
        override fun Command.unregister(): Boolean = CommandManagerImpl.run { unregister() }
        override fun Command.isRegistered(): Boolean = CommandManagerImpl.run { isRegistered() }
        override val commandPrefix: String get() = CommandManagerImpl.commandPrefix
        override suspend fun Command.execute(
            sender: CommandSender,
            args: MessageChain,
            checkPermission: Boolean
        ): Unit = CommandManagerImpl.run { execute(sender, args = args, checkPermission = checkPermission) }

        override suspend fun Command.execute(sender: CommandSender, vararg args: Any, checkPermission: Boolean): Unit =
            CommandManagerImpl.run { execute(sender, args = args, checkPermission = checkPermission) }

        override suspend fun CommandSender.executeCommand(vararg messages: Any): CommandExecuteResult =
            CommandManagerImpl.run { executeCommand(*messages) }

        override suspend fun CommandSender.executeCommand(messages: MessageChain): CommandExecuteResult =
            CommandManagerImpl.run { executeCommand(messages) }
    }
}