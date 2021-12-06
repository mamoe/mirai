/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.command.java.JRawCommand
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.internal.command.findOrCreateCommandPermission
import net.mamoe.mirai.console.internal.data.typeOf0
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

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
    @ResolveContext(RESTRICTED_CONSOLE_COMMAND_OWNER)
    public override val owner: CommandOwner,
    /** 主指令名. */
    @ResolveContext(COMMAND_NAME)
    public override val primaryName: String,
    /** 次要指令名. */
    @ResolveContext(COMMAND_NAME)
    public override vararg val secondaryNames: String,
    /** 用法说明, 用于发送给用户 */
    public override val usage: String = "<no usages given>",
    /** 指令描述, 用于显示在 [BuiltInCommands.HelpCommand] */
    public override val description: String = "<no descriptions given>",
    /** 指令父权限 */
    parentPermission: Permission = owner.parentPermission,
    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    @OptIn(ExperimentalCommandDescriptors::class)
    public override val prefixOptional: Boolean = false,
) : Command {
    public override val permission: Permission by lazy { findOrCreateCommandPermission(parentPermission) }

    @ExperimentalCommandDescriptors
    override val overloads: List<@JvmWildcard CommandSignature> = listOf(
        CommandSignatureImpl(
            receiverParameter = CommandReceiverParameter(false, typeOf0<CommandSender>()),
            valueParameters = listOf(
                AbstractCommandValueParameter.UserDefinedType.createRequired<Array<out Message>>(
                    "args",
                    true
                )
            )
        ) { call ->
            val sender = call.caller
            val arguments = call.rawValueArguments
            sender.onCommand(buildMessageChain { arguments.forEach { +it.value } })
        }
    )

    /**
     * 在指令被执行时调用.
     *
     * @param args 指令参数.
     *
     * @see CommandManager.executeCommand 查看更多信息
     */
    public abstract suspend fun CommandSender.onCommand(args: MessageChain)
}


