/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.command.java.JRawCommand
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.internal.command.findOrCreateCommandPermission
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

/**
 * 无参数解析, 只会接收原消息链的指令. Java 查看 [JRawCommand].
 *
 * ```kotlin
 * object MyCommand : RawCommand(
 *     MyPluginMain, "name", // 使用插件主类对象作为指令拥有者；设置主指令名为 "name"
 *     // 可选：
 *     "name2", "name3", // 增加两个次要名称
 *     usage = "/name arg1 arg2", // 设置用法，将会在 /help 展示
 *     description = "这是一个测试指令", // 设置描述，将会在 /help 展示
 *     prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
 * ) {
 *     override suspend fun CommandContext.onCommand(args: MessageChain) {
 *     }
 * }
 * ```
 *
 * @see JRawCommand 供 Java 用户继承.
 *
 * @see SimpleCommand 简单指令
 * @see CompositeCommand 复合指令
 */
public abstract class RawCommand(
    /**
     * 指令拥有者. 通常建议使用插件主类.
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
            receiverParameter = CommandReceiverParameter.Context(false),
            valueParameters = listOf(
                AbstractCommandValueParameter.UserDefinedType.createRequired<Array<out Message>>(
                    "args",
                    true
                )
            )
        ) { call ->
            val sender = call.caller
            val arguments = call.rawValueArguments
            val context = CommandContextImpl(sender, call.originalMessage)
            context.onCommand(buildMessageChain { arguments.forEach { +it.value } })
        }
    )

    /**
     * 在指令被执行时调用.
     *
     * @param args 指令参数.
     *
     * @see CommandManager.executeCommand 查看更多信息
     */
    public open suspend fun CommandSender.onCommand(args: MessageChain) {

    }

    /**
     * 在指令被执行时调用.
     *
     * @param args 指令参数.
     * @see CommandManager.executeCommand 查看更多信息
     *
     * @since 2.12
     */
    public open suspend fun CommandContext.onCommand(args: MessageChain) {
        return sender.onCommand(args)
    }
}


