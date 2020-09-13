/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "NOTHING_TO_INLINE", "unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE",
    "MemberVisibilityCanBePrivate", "INAPPLICABLE_JVM_NAME"
)
@file:JvmName("CommandManagerKt")

package net.mamoe.mirai.console.command

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.CommandManagerImpl.executeCommand
import net.mamoe.mirai.message.data.*

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
    @JvmName("registerCommand")
    public fun Command.register(override: Boolean = false): Boolean

    /**
     * 查找并返回重名的指令. 返回重名指令.
     */
    @JvmName("findCommandDuplicate")
    public fun Command.findDuplicate(): Command?

    /**
     * 取消注册这个指令. 若指令未注册, 返回 `false`.
     */
    @JvmName("unregisterCommand")
    public fun Command.unregister(): Boolean

    /**
     * 当 [this] 已经 [注册][register] 后返回 `true`
     */
    @JvmName("isCommandRegistered")
    public fun Command.isRegistered(): Boolean

    /**
     * 解析并执行一个指令
     *
     * ### 指令解析流程
     * 1. [message] 的第一个消息元素的 [内容][Message.contentToString] 被作为指令名, 在已注册指令列表中搜索. (包含 [Command.prefixOptional] 相关的处理)
     * 2. 参数语法分析.
     *   在当前的实现下, [message] 被以空格和 [SingleMessage] 分割.
     *   如 "MessageChain("foo bar", [Image], " test")" 被分割为 "foo", "bar", [Image], "test".
     *   注意: 字符串与消息元素之间不需要空格, 会被强制分割. 如 "bar[mirai:image:]" 会被分割为 "bar" 和 [Image] 类型的消息元素.
     * 3. 参数解析. 各类型指令实现不同. 详见 [RawCommand], [CompositeCommand], [SimpleCommand]
     *
     * ### 未来的扩展
     * 在将来, 参数语法分析过程可能会被扩展, 允许插件自定义处理方式, 因此可能不会简单地使用空格分隔.
     *
     * @param message 一条完整的指令. 如 "/managers add 123456.123456"
     * @param checkPermission 为 `true` 时检查权限
     *
     * @return 执行结果
     */
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommand(
        message: Message,
        checkPermission: Boolean = true,
    ): CommandExecuteResult

    /**
     * 解析并执行一个指令
     *
     * @param message 一条完整的指令. 如 "/managers add 123456.123456"
     * @param checkPermission 为 `true` 时检查权限
     *
     * @return 执行结果
     * @see executeCommand
     */
    @JvmDefault
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommand(
        message: String,
        checkPermission: Boolean = true,
    ): CommandExecuteResult = executeCommand(PlainText(message).asMessageChain(), checkPermission)

    /**
     * 执行一个确切的指令
     * @see executeCommand 获取更多信息
     */
    @JvmBlockingBridge
    @JvmName("executeCommand")
    public suspend fun Command.execute(
        sender: CommandSender,
        arguments: Message = EmptyMessageChain,
        checkPermission: Boolean = true,
    ): CommandExecuteResult

    /**
     * 执行一个确切的指令
     * @see executeCommand 获取更多信息
     */
    @JvmDefault
    @JvmBlockingBridge
    @JvmName("executeCommand")
    public suspend fun Command.execute(
        sender: CommandSender,
        arguments: String = "",
        checkPermission: Boolean = true,
    ): CommandExecuteResult = execute(sender, PlainText(arguments).asMessageChain(), checkPermission)

    public companion object INSTANCE : CommandManager by CommandManagerImpl {
        // TODO: 2020/8/20 https://youtrack.jetbrains.com/issue/KT-41191

        override val CommandOwner.registeredCommands: List<Command> get() = CommandManagerImpl.run { registeredCommands }
        override fun CommandOwner.unregisterAllCommands(): Unit = CommandManagerImpl.run { unregisterAllCommands() }
        override fun Command.register(override: Boolean): Boolean = CommandManagerImpl.run { register(override) }
        override fun Command.findDuplicate(): Command? = CommandManagerImpl.run { findDuplicate() }
        override fun Command.unregister(): Boolean = CommandManagerImpl.run { unregister() }
        override fun Command.isRegistered(): Boolean = CommandManagerImpl.run { isRegistered() }
        override val commandPrefix: String get() = CommandManagerImpl.commandPrefix
        override val allRegisteredCommands: List<Command>
            get() = CommandManagerImpl.allRegisteredCommands


        override suspend fun Command.execute(
            sender: CommandSender,
            arguments: Message,
            checkPermission: Boolean,
        ): CommandExecuteResult =
            CommandManagerImpl.run { execute(sender, arguments = arguments, checkPermission = checkPermission) }

        override suspend fun CommandSender.executeCommand(
            message: String,
            checkPermission: Boolean,
        ): CommandExecuteResult = CommandManagerImpl.run { executeCommand(message, checkPermission) }

        override suspend fun Command.execute(
            sender: CommandSender,
            arguments: String,
            checkPermission: Boolean,
        ): CommandExecuteResult = CommandManagerImpl.run { execute(sender, arguments, checkPermission) }

        override suspend fun CommandSender.executeCommand(
            message: Message,
            checkPermission: Boolean,
        ): CommandExecuteResult = CommandManagerImpl.run { executeCommand(message, checkPermission) }

        /**
         * 执行一个确切的指令
         * @see execute 获取更多信息
         */
        public suspend fun CommandSender.execute(
            command: Command,
            arguments: Message,
            checkPermission: Boolean = true,
        ): CommandExecuteResult {
            return command.execute(this, arguments, checkPermission)
        }

        /**
         * 执行一个确切的指令
         * @see execute 获取更多信息
         */
        public suspend fun CommandSender.execute(
            command: Command,
            arguments: String,
            checkPermission: Boolean = true,
        ): CommandExecuteResult {
            return command.execute(this, arguments, checkPermission)
        }
    }
}