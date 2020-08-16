/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

/**
 * 无参数解析, 接收原生参数的指令.
 */
public abstract class RawCommand(
    public override val owner: CommandOwner,
    public override vararg val names: String,
    public override val usage: String = "<no usages given>",
    public override val description: String = "<no descriptions given>",
    public override val permission: CommandPermission = CommandPermission.Default,
    public override val prefixOptional: Boolean = false
) : Command {
    public abstract override suspend fun CommandSender.onCommand(args: Array<out Any>)
}
