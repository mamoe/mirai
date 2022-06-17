/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command.java

import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.descriptor.buildCommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.plus
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.permission.Permission

/**
 * 复合指令. 指令注册时候会通过反射构造指令解析器.
 *
 * 示例:
 * ```java
 * public final class MyCompositeCommand extends CompositeCommand {
 *     public static final MyCompositeCommand INSTANCE = new MyCompositeCommand();
 *
 *     private MyCompositeCommand() {
 *         super(MyPluginMain.INSTANCE, "manage") // "manage" 是主指令名
 *     }
 *
 *     // [参数智能解析]
 *     //
 *     //
 *     // 在控制台执行 "/manage <群号>.<群员> <持续时间>",
 *     // 或在聊天群内发送 "/manage <@一个群员> <持续时间>",
 *     // 或在聊天群内发送 "/manage <目标群员的群名> <持续时间>",
 *     // 或在聊天群内发送 "/manage <目标群员的账号> <持续时间>"
 *     // 时调用这个函数
 *     @SubCommand
 *     public void mute(CommandSender sender, Member target, int duration) { // 通过 /manage mute <target> <duration> 调用.
 *         sender.sendMessage("/manage mute 被调用了, 参数为: " + target + ", " + duration);
 *
 *
 *         String result;
 *         try {
 *             result = target.mute(duration).toString();
 *         } catch(Exception e) {
 *             result = ExceptionsKt.stackTraceToString(e);
 *         }
 *
 *         sender.sendMessage("结果: " + result);
 *     }
 *
 *     @SubCommand
 *     public void list(CommandSender sender) { // 执行 "/manage list" 时调用这个方法
 *         sender.sendMessage("/manage list 被调用了");
 *     }
 *
 *     @SubCommand
 *     public void repeat(CommandContext context) {
 *         // 使用 CommandContext 作为参数，可以获得触发指令的原消息链 originalMessage，其中包含 MessageMetadata。
 *         context.getSender().sendMessage(context.getOriginalMessage());
 *     }
 *
 *     // 支持 Image 类型, 需在聊天中执行此指令.
 *     @SubCommand
 *     public void test(CommandSender sender, Image image) { // 执行 "/manage test <一张图片>" 时调用这个方法
 *         sender.sendMessage("/manage image 被调用了, 图片是 " + image.imageId)
 *     }
 * }
 * ```
 *
 * Kotlin 示例查看 [CompositeCommand]
 *
 * @see buildCommandArgumentContext
 */
public abstract class JCompositeCommand
@JvmOverloads constructor(
    @ResolveContext(RESTRICTED_CONSOLE_COMMAND_OWNER) owner: CommandOwner,
    @ResolveContext(COMMAND_NAME) primaryName: String,
    @ResolveContext(COMMAND_NAME) vararg secondaryNames: String,
    parentPermission: Permission = owner.parentPermission,
) : CompositeCommand(owner, primaryName, secondaryNames = secondaryNames, parentPermission = parentPermission) {
    /** 指令描述, 用于显示在 [BuiltInCommands.HelpCommand] */
    public final override var description: String = super.description
        protected set

    public final override var permission: Permission = super.permission
        protected set

    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    @ExperimentalCommandDescriptors
    public final override var prefixOptional: Boolean = false
        protected set

    /**
     * 智能参数解析环境
     * @since 2.12
     */
    public final override var context: CommandArgumentContext = super.context
        private set

    /**
     * 增加智能参数解析环境
     * @since 2.12
     */
    protected fun addArgumentContext(context: CommandArgumentContext) {
        this.context += context
    }
}