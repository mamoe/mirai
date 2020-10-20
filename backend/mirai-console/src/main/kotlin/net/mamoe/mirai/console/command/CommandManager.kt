/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
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
import net.mamoe.mirai.console.command.parse.CommandCallParser.Companion.parseCommandCall
import net.mamoe.mirai.console.command.resolve.CommandCallResolver
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.extensions.CommandCallResolverProvider
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.CommandManagerImpl.executeCommand
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.message.data.*

/**
 * 指令管理器
 */
public interface CommandManager {
    /**
     * 获取已经注册了的属于这个 [CommandOwner] 的指令列表.
     *
     * @return 这一时刻的浅拷贝.
     */
    public val CommandOwner.registeredCommands: List<Command>

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
     * 取消注册所有属于 [this] 的指令
     */
    public fun CommandOwner.unregisterAllCommands()

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
    @JvmName("registerCommand")
    public fun Command.register(override: Boolean = false): Boolean

    /**
     * 查找并返回重名的指令. 返回重名指令.
     */
    @JvmName("findCommandDuplicate")
    public fun Command.findDuplicate(): Command?

    /**
     * 取消注册这个指令.
     *
     * 若指令未注册, 返回 `false`.
     */
    @JvmName("unregisterCommand")
    public fun Command.unregister(): Boolean

    /**
     * 当 [this] 已经 [注册][register] 时返回 `true`
     */
    @JvmName("isCommandRegistered")
    public fun Command.isRegistered(): Boolean

    /**
     * 解析并执行一个指令.
     *
     * 如要避免参数解析, 请使用 [Command.onCommand]
     *
     * ### 指令解析流程
     * 1. [message] 的第一个消息元素的 [内容][Message.contentToString] 被作为指令名, 在已注册指令列表中搜索. (包含 [Command.prefixOptional] 相关的处理)
     * 2. 参数语法分析.
     *   在当前的实现下, [message] 被以空格和 [SingleMessage] 分割.
     *   如 "MessageChain("foo bar", [Image], " test")" 被分割为 "foo", "bar", [Image], "test".
     *   注意: 字符串与消息元素之间不需要空格, 会被强制分割. 如 "bar[mirai:image:]" 会被分割为 "bar" 和 [Image] 类型的消息元素.
     * 3. 参数解析. 各类型指令实现不同. 详见 [RawCommand], [CompositeCommand], [SimpleCommand]
     *
     * ### 扩展
     * 参数语法分析过程可能会被扩展, 插件可以自定义处理方式, 因此可能不会简单地使用空格分隔.
     *
     * @param message 一条完整的指令. 如 "/managers add 123456.123456"
     * @param checkPermission 为 `true` 时检查权限
     *
     * @see CommandCallParser
     * @see CommandCallResolver
     *
     * @return 执行结果
     */
    @JvmBlockingBridge
    @OptIn(ExperimentalCommandDescriptors::class)
    public suspend fun executeCommand(
        caller: CommandSender,
        message: Message,
        checkPermission: Boolean = true,
    ): CommandExecuteResult {
        return executeCommandImpl(this, message, caller, checkPermission)
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
    @JvmBlockingBridge
    public suspend fun CommandSender.executeCommand(
        message: String,
        checkPermission: Boolean = true,
    ): CommandExecuteResult = executeCommand(this, PlainText(message).asMessageChain(), checkPermission)

    @JvmName("resolveCall")
    @ExperimentalCommandDescriptors
    public fun CommandCall.resolve(): ResolvedCommandCall? {
        GlobalComponentStorage.run {
            CommandCallResolverProvider.useExtensions { provider ->
                provider.instance.resolve(this@resolve)?.let { return it }
            }
        }
        return null
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
        // TODO: 2020/8/20 https://youtrack.jetbrains.com/issue/KT-41191


        override val CommandOwner.registeredCommands: List<Command> get() = CommandManagerImpl.run { this@registeredCommands.registeredCommands }
        override fun CommandOwner.unregisterAllCommands(): Unit = CommandManagerImpl.run { unregisterAllCommands() }
        override fun Command.register(override: Boolean): Boolean = CommandManagerImpl.run { register(override) }
        override fun Command.findDuplicate(): Command? = CommandManagerImpl.run { findDuplicate() }
        override fun Command.unregister(): Boolean = CommandManagerImpl.run { unregister() }
        override fun Command.isRegistered(): Boolean = CommandManagerImpl.run { isRegistered() }
        override val commandPrefix: String get() = CommandManagerImpl.commandPrefix
        override val allRegisteredCommands: List<Command>
            get() = CommandManagerImpl.allRegisteredCommands

    }
}

/**
 * 执行一个确切的指令
 * @see executeCommand 获取更多信息
 */
// @JvmBlockingBridge
// @JvmName("executeCommand")
public suspend fun Command.execute(
    sender: CommandSender,
    arguments: String = "",
    checkPermission: Boolean = true,
): CommandExecuteResult = execute(sender, PlainText(arguments).asMessageChain(), checkPermission)

/**
 * 执行一个确切的指令
 * @see executeCommand 获取更多信息
 */
// @JvmBlockingBridge
// @JvmName("executeCommand")
public suspend fun Command.execute(
    sender: CommandSender,
    arguments: Message = EmptyMessageChain,
    checkPermission: Boolean = true,
): CommandExecuteResult {
    // TODO: 2020/10/18  net.mamoe.mirai.console.command.CommandManager.execute
    val chain = buildMessageChain {
        append(CommandManager.commandPrefix)
        append(this@execute.primaryName)
        append(' ')
        append(arguments)
    }
    return CommandManager.executeCommand(sender, chain, checkPermission)
}


// Don't move into CommandManager, compilation error / VerifyError
@OptIn(ExperimentalCommandDescriptors::class)
internal suspend fun executeCommandImpl(
    receiver: CommandManager,
    message: Message,
    caller: CommandSender,
    checkPermission: Boolean,
): CommandExecuteResult = with(receiver) {
    val call = message.asMessageChain().parseCommandCall(caller) ?: return CommandExecuteResult.CommandNotFound("")
    val resolved = call.resolve() ?: return CommandExecuteResult.CommandNotFound(call.calleeName)

    val command = resolved.callee

    if (checkPermission && !command.permission.testPermission(caller)) {
        return CommandExecuteResult.PermissionDenied(command, call.calleeName)
    }

    return try {
        resolved.calleeSignature.call(resolved)
        CommandExecuteResult.Success(resolved.callee, call.calleeName, EmptyMessageChain)
    } catch (e: Throwable) {
        CommandExecuteResult.ExecutionFailed(e, resolved.callee, call.calleeName, EmptyMessageChain)
    }
}

