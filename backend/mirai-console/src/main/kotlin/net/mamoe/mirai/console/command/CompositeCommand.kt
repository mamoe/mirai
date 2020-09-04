/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "EXPOSED_SUPER_CLASS",
    "NOTHING_TO_INLINE",
    "unused",
    "WRONG_MODIFIER_TARGET", "CANNOT_WEAKEN_ACCESS_PRIVILEGE",
    "WRONG_MODIFIER_CONTAINING_DECLARATION", "RedundantVisibilityModifier"
)

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.description.*
import net.mamoe.mirai.console.internal.command.AbstractReflectionCommand
import net.mamoe.mirai.console.internal.command.CompositeCommandSubCommandAnnotationResolver
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.message.data.MessageChain
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * 复合指令. 指令注册时候会通过反射构造指令解析器.
 *
 * 示例:
 * ```
 * @OptIn(ConsoleExperimentalAPI::class)
 * object MyCompositeCommand : CompositeCommand(
 *     MyPluginMain, "manage", // "manage" 是主指令名
 *     description = "示例指令", permission = MyCustomPermission,
 *     // prefixOptional = true // 还有更多参数可填, 此处忽略
 * ) {
 *
 *     // [参数智能解析]
 *     //
 *     // 在控制台执行 "/manage <群号>.<群员> <持续时间>",
 *     // 或在聊天群内发送 "/manage <@一个群员> <持续时间>",
 *     // 或在聊天群内发送 "/manage <目标群员的群名> <持续时间>",
 *     // 或在聊天群内发送 "/manage <目标群员的账号> <持续时间>"
 *     // 时调用这个函数
 *     @SubCommand
 *     suspend fun CommandSender.mute(target: Member, duration: Int) { // 通过 /manage mute <target> <duration> 调用
 *         sendMessage("/manage mute 被调用了, 参数为: $target, $duration")
 *
 *         val result = kotlin.runCatching {
 *             target.mute(duration).toString()
 *         }.getOrElse {
 *             it.stackTraceToString()
 *         } // 失败时返回堆栈信息
 *
 *         sendMessage("结果: $result")
 *     }
 *
 *     @SubCommand
 *     suspend fun CommandSender.list() { // 执行 "/manage list" 时调用这个函数
 *         sendMessage("/manage list 被调用了")
 *     }
 *
 *     // 支持 Image 类型, 需在聊天中执行此指令.
 *     @SubCommand
 *     suspend fun CommandSender.test(image: Image) { // 执行 "/manage test <一张图片>" 时调用这个函数
 *         sendMessage("/manage image 被调用了, 图片是 ${image.imageId}")
 *     }
 * }
 * ```
 *
 * @see buildCommandArgumentContext
 */
@ConsoleExperimentalAPI
public abstract class CompositeCommand(
    owner: CommandOwner,
    vararg names: String,
    description: String = "no description available",
    permission: CommandPermission = CommandPermission.Default,
    prefixOptional: Boolean = false,
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext
) : Command, AbstractReflectionCommand(owner, names, description, permission, prefixOptional),
    CommandArgumentContextAware {

    /**
     * 自动根据带有 [SubCommand] 注解的函数签名生成 [usage]. 也可以被覆盖.
     */
    public override val usage: String get() = super.usage

    /**
     * [CommandArgumentParser] 的环境
     */
    public final override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext

    /**
     * 标记一个函数为子指令, 当 [value] 为空时使用函数名.
     * @param value 子指令名
     */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class SubCommand(vararg val value: String)

    /** 指定子指令要求的权限 */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class Permission(val value: KClass<out CommandPermission>)

    /** 指令描述 */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class Description(val value: String)

    /** 参数名, 将参与构成 [usage] */
    @Retention(RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    protected annotation class Name(val value: String)

    public final override suspend fun CommandSender.onCommand(args: MessageChain) {
        matchSubCommand(args)?.parseAndExecute(this, args, true) ?: kotlin.run {
            defaultSubCommand.onCommand(this, args)
        }
    }


    protected override suspend fun CommandSender.onDefault(rawArgs: MessageChain) {
        sendMessage(usage)
    }

    internal final override val subCommandAnnotationResolver: SubCommandAnnotationResolver
        get() = CompositeCommandSubCommandAnnotationResolver
}


