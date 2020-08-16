/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.execute
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.internal.command.isValidSubName
import net.mamoe.mirai.message.data.SingleMessage

/**
 * 指令
 *
 * @see CommandManager.register 注册这个指令
 *
 * @see RawCommand
 * @see CompositeCommand
 * @see SimpleCommand
 */
public interface Command {
    /**
     * 指令名. 需要至少有一个元素. 所有元素都不能带有空格
     */
    public val names: Array<out String>

    /**
     * 用法说明, 用于发送给用户
     */
    public val usage: String

    /**
     * 指令描述, 用于显示在 [BuiltInCommands.Help]
     */
    public val description: String

    /**
     * 指令权限
     */
    public val permission: CommandPermission

    /**
     * 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选
     */
    public val prefixOptional: Boolean

    /**
     * 指令拥有者, 对于插件的指令通常是 [PluginCommandOwner]
     */
    public val owner: CommandOwner

    /**
     * @param args 指令参数. 数组元素类型可能是 [SingleMessage] 或 [String]. 且已经以 ' ' 分割.
     *
     * @see CommandManager.execute
     */
    @JvmBlockingBridge
    public suspend fun CommandSender.onCommand(args: Array<out Any>)

    public companion object {
        /**
         * 主要指令名. 为 [Command.names] 的第一个元素.
         */
        @JvmStatic
        public val Command.primaryName: String
            get() = names[0]
    }
}

@JvmSynthetic
public suspend inline fun Command.onCommand(sender: CommandSender, args: Array<out Any>): Unit =
    sender.run { onCommand(args) }

/**
 * [Command] 的基础实现
 */
public abstract class AbstractCommand @JvmOverloads constructor(
    public final override val owner: CommandOwner,
    vararg names: String,
    description: String = "<no description available>",
    public final override val permission: CommandPermission = CommandPermission.Default,
    public final override val prefixOptional: Boolean = false
) : Command {
    public final override val description: String = description.trimIndent()
    public final override val names: Array<out String> =
        names.map(String::trim).filterNot(String::isEmpty).map(String::toLowerCase).also { list ->
            list.firstOrNull { !it.isValidSubName() }?.let { error("Invalid name: $it") }
        }.toTypedArray()

}