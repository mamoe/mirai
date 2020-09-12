/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.execute
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.java.JRawCommand
import net.mamoe.mirai.console.internal.command.createOrFindCommandPermission
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.message.data.MessageChain

/**
 * 无参数解析, 接收原生参数的指令.
 *
 * ### 指令执行流程
 * 继 [CommandManager.executeCommand] 所述第 3 步, [RawCommand] 不会对参数做任何解析.
 *
 * @see JRawCommand 供 Java 用户继承.
 *
 * @see SimpleCommand 简单指令
 * @see CompositeCommand 复合指令
 */
public abstract class RawCommand(
    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    public override val owner: CommandOwner,
    /** 指令名. 需要至少有一个元素. 所有元素都不能带有空格 */
    public override vararg val names: String,
    /** 用法说明, 用于发送给用户 */
    public override val usage: String = "<no usages given>",
    /** 指令描述, 用于显示在 [BuiltInCommands.Help] */
    public override val description: String = "<no descriptions given>",
    /** 指令父权限 */
    parentPermission: Permission = owner.parentPermission,
    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    public override val prefixOptional: Boolean = false,
) : Command {
    public override val permission: Permission by lazy { createOrFindCommandPermission(parentPermission) }

    /**
     * 在指令被执行时调用.
     *
     * @param args 指令参数.
     *
     * @see CommandManager.execute 查看更多信息
     */
    public abstract override suspend fun CommandSender.onCommand(args: MessageChain)
}


