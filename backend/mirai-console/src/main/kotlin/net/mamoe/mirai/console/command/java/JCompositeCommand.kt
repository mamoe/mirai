/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.java

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.description.buildCommandArgumentContext
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

/**
 * 复合指令. 指令注册时候会通过反射构造指令解析器.
 *
 * 示例:
 * ```
 * public final class MyCompositeCommand extends CompositeCommand {
 *     public static final MyCompositeCommand INSTANCE = new MyCompositeCommand();
 *
 *     public MyCompositeCommand() {
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
 *         sender.sendMessage("结果: " + result)
 *     }
 *
 *     @SubCommand
 *     public void list(CommandSender sender) { // 执行 "/manage list" 时调用这个方法
 *         sender.sendMessage("/manage list 被调用了")
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
 * @see buildCommandArgumentContext
 */
@ConsoleExperimentalAPI
public abstract class JCompositeCommand(
    owner: CommandOwner,
    vararg names: String
) : CompositeCommand(owner, *names) {
    /** 指令描述, 用于显示在 [BuiltInCommands.Help] */
    public final override var description: String = "<no descriptions given>"
        protected set

    /** 指令权限 */
    public final override var permission: CommandPermission = CommandPermission.Default
        protected set

    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    public final override var prefixOptional: Boolean = false
        protected set

}