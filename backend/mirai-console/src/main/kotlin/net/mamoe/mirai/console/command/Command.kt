/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.java.JCommand
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.message.data.MessageChain

/**
 * 指令
 *
 * @see CommandManager.register 注册这个指令
 *
 * @see RawCommand 无参数解析, 接收原生参数的指令
 * @see CompositeCommand 复合指令
 * @see SimpleCommand 简单的, 支持参数自动解析的指令
 *
 * @see JCommand 为 Java 用户添加协程帮助的 [Command]
 */
public interface Command {
    /**
     * 主指令名. 将会参与构成 [Permission.id].
     *
     * 不允许包含 [空格][Char.isWhitespace], '.', ':'.
     */
    @ResolveContext(COMMAND_NAME)
    public val primaryName: String

    /**
     * 次要指令名
     * @see Command.primaryName 获取主指令名
     */
    @ResolveContext(COMMAND_NAME)
    public val secondaryNames: Array<out String>

    /**
     * 用法说明, 用于发送给用户. [usage] 一般包含 [description].
     */
    public val usage: String

    /**
     * 指令描述, 用于显示在 [BuiltInCommands.HelpCommand]
     */
    public val description: String

    /**
     * 此指令所分配的权限.
     *
     * ### 实现约束
     * - [Permission.id] 应由 [CommandOwner.permissionId] 创建. 因此保证相同的 [PermissionId.namespace]
     * - [PermissionId.name] 应为 [主指令名][primaryName]
     */
    public val permission: Permission

    /**
     * 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选.
     *
     * 会影响聊天语境中的解析.
     */
    public val prefixOptional: Boolean

    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    public val owner: CommandOwner

    /**
     * 在指令被执行时调用.
     *
     * @param args 精确的指令参数. [MessageChain] 每个元素代表一个精确的参数.
     *
     * @see CommandManager.executeCommand 查看更多信息
     */
    @JvmBlockingBridge
    public suspend fun CommandSender.onCommand(args: MessageChain)

    public companion object {

        /**
         * 获取所有指令名称 (包含 [primaryName] 和 [secondaryNames]).
         *
         * @return 数组大小至少为 1. 第一个元素总是 [primaryName]. 随后是保持原顺序的 [secondaryNames]
         */
        @JvmStatic
        public val Command.allNames: Array<String>
            get() = arrayOf(primaryName, *secondaryNames)

        /**
         * 检查指令名的合法性. 在非法时抛出 [IllegalArgumentException]
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun checkCommandName(@ResolveContext(COMMAND_NAME) name: String) {
            when {
                name.isBlank() -> throw IllegalArgumentException("Command name should not be blank.")
                name.any { it.isWhitespace() } -> throw IllegalArgumentException("Spaces is not yet allowed in command name.")
                name.contains(':') -> throw IllegalArgumentException("':' is forbidden in command name.")
                name.contains('.') -> throw IllegalArgumentException("'.' is forbidden in command name.")
            }
        }
    }
}

/**
 * 调用 [Command.onCommand]
 * @see Command.onCommand
 */
@JvmSynthetic
public suspend inline fun Command.onCommand(sender: CommandSender, args: MessageChain): Unit =
    sender.onCommand(args)

