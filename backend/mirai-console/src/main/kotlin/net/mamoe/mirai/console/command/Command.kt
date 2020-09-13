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
import net.mamoe.mirai.console.internal.command.createOrFindCommandPermission
import net.mamoe.mirai.console.internal.command.isValidSubName
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage

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
     * 指令名. 需要至少有一个元素. 所有元素都不能带有空格
     *
     * @see Command.primaryName 获取主要指令名
     */
    public val names: Array<out String>

    /**
     * 用法说明, 用于发送给用户. [usage] 一般包含 [description].
     */
    public val usage: String

    /**
     * 指令描述, 用于显示在 [BuiltInCommands.HelpCommand]
     */
    public val description: String

    /**
     * 指令权限
     */
    public val permission: Permission

    /**
     * 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选
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
     * @param args 指令参数. 数组元素类型可能是 [SingleMessage] 或 [String]. 且已经以 ' ' 分割.
     *
     * @see CommandManager.executeCommand 查看更多信息
     */
    @JvmBlockingBridge
    public suspend fun CommandSender.onCommand(args: MessageChain)

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
public suspend inline fun Command.onCommand(sender: CommandSender, args: MessageChain): Unit =
    sender.onCommand(args)

/**
 * [Command] 的基础实现
 *
 * @see SimpleCommand
 * @see CompositeCommand
 * @see RawCommand
 */
public abstract class AbstractCommand
@JvmOverloads constructor(
    /** 指令拥有者. */
    public override val owner: CommandOwner,
    vararg names: String,
    description: String = "<no description available>",
    parentPermission: Permission = owner.parentPermission,
    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    public override val prefixOptional: Boolean = false,
) : Command {
    public override val description: String = description.trimIndent()
    public override val names: Array<out String> =
        names.map(String::trim).filterNot(String::isEmpty).map(String::toLowerCase).also { list ->
            list.firstOrNull { !it.isValidSubName() }?.let { error("Invalid name: $it") }
        }.toTypedArray()

    public override val permission: Permission by lazy { createOrFindCommandPermission(parentPermission) }
}