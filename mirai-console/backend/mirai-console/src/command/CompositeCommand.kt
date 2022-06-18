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
import net.mamoe.mirai.console.command.java.JCompositeCommand
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.internal.command.CommandReflector
import net.mamoe.mirai.console.internal.command.CompositeCommandSubCommandAnnotationResolver
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION


/**
 * 复合指令. 指令注册时候会通过反射构造指令解析器.
 *
 * Java 示例查看 [JCompositeCommand].
 *
 * Kotlin 示例:
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
 *     @SubCommand // 表示这是一个子指令，使用函数名作为子指令名称
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
 *     suspend fun SystemCommandSender.foo() {
 *         // 使用 SystemCommandSender 作为接收者，表示指令只能由系统(控制台或其他插件)执行。
 *         // 当用户尝试在聊天环境执行时将会收到错误提示。
 *     }
 *
 *     @SubCommand("list", "查看列表") // 可以设置多个子指令名。此时函数名会被忽略。
 *     suspend fun CommandSender.list() { // 执行 "/manage list" 时调用这个函数
 *         sendMessage("/manage list 被调用了")
 *     }
 *
 *     @SubCommand
 *     suspend fun CommandContext.repeat() {
 *         // 使用 CommandContext 作为参数，可以获得触发指令的原消息链 originalMessage，其中包含 MessageMetadata。
 *         sender.sendMessage(originalMessage)
 *     }
 *
 *     // 支持 Image 类型, 需在聊天中执行此指令.
 *     @SubCommand
 *     suspend fun UserCommandSender.test(image: Image) { // 执行 "/manage test <一张图片>" 时调用这个函数
 *         // 由于 Image 类型消息只可能在聊天环境，可以直接使用 UserCommandSender。
 *         sendMessage("/manage image 被调用了, 图片是 ${image.imageId}")
 *     }
 * }
 * ```
 *
 * @see buildCommandArgumentContext
 */
public abstract class CompositeCommand(
    @ResolveContext(RESTRICTED_CONSOLE_COMMAND_OWNER) owner: CommandOwner,
    @ResolveContext(COMMAND_NAME) primaryName: String,
    @ResolveContext(COMMAND_NAME) vararg secondaryNames: String,
    description: String = "no description available",
    parentPermission: Permission = owner.parentPermission,
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext,
) : Command, AbstractCommand(owner, primaryName, secondaryNames = secondaryNames, description, parentPermission),
    CommandArgumentContextAware {

    private val reflector by lazy { CommandReflector(this, CompositeCommandSubCommandAnnotationResolver) }

    @ExperimentalCommandDescriptors
    public final override val overloads: List<@JvmWildcard CommandSignatureFromKFunction> by lazy {
        reflector.findSubCommands().also {
            reflector.validate(it)
        }
    }

    /**
     * 自动根据带有 [SubCommand] 注解的函数签名生成 [usage]. 也可以被覆盖.
     */
    public override val usage: String by lazy {
        @OptIn(ExperimentalCommandDescriptors::class)
        reflector.generateUsage(overloads)
    }

    /**
     * 智能参数解析环境
     */ // open since 2.12
    public override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext

    /**
     * 标记一个函数为子指令, 当 [value] 为空时使用函数名.
     * @param value 子指令名
     */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class SubCommand(
        @ResolveContext(COMMAND_NAME) vararg val value: String = [],
    )

    /** 指令描述 */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class Description(val value: String)

    /** 参数名, 将参与构成 [usage] */
    @ConsoleExperimentalApi("Classname might change")
    @Retention(RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    protected annotation class Name(val value: String)
}


