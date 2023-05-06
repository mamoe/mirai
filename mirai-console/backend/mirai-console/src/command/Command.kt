/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.CommandArgumentContextAware
import net.mamoe.mirai.console.command.descriptor.CommandSignature
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId

/**
 * 指令
 *
 * ### 权限
 * 每个指令都会被分配一个权限 [permission]. 默认没有人拥有这个权限. 请通过 [BuiltInCommands.PermissionCommand] 赋予权限.
 *
 * @see CommandManager.registerCommand 注册这个指令
 *
 * @see RawCommand 无参数解析, 接收原生参数的指令
 * @see CompositeCommand 复合指令
 * @see SimpleCommand 简单的, 支持参数自动解析的指令
 *
 * @see CommandArgumentContextAware
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
     * 指令可能的参数列表.
     */
    @ExperimentalCommandDescriptors
    public val overloads: List<@JvmWildcard CommandSignature>

    /**
     * 用法说明, 用于发送给用户. [usage] 一般包含 [description].
     */
    public val usage: String

    /**
     * 描述, 用于显示在 [BuiltInCommands.HelpCommand]
     */
    public val description: String

    /**
     * 为此指令分配的权限.
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
     *
     * #### 实验性 API
     * 由于指令解析允许被扩展, 此属性可能不适用所有解析器, 因此还未决定是否保留.
     */
    @ExperimentalCommandDescriptors
    public val prefixOptional: Boolean

    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    @ResolveContext(RESTRICTED_CONSOLE_COMMAND_OWNER)
    public val owner: CommandOwner

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
                name.any { it.isWhitespace() } -> throw IllegalArgumentException("Spaces are not yet allowed in command name.")
                name.contains(':') -> throw IllegalArgumentException("':' is forbidden in command name.")
                name.contains('.') -> throw IllegalArgumentException("'.' is forbidden in command name.")
            }
        }
    }
}
