/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.internal.command.createOrFindCommandPermission
import net.mamoe.mirai.console.permission.Permission

/**
 * [Command] 的基础实现
 *
 * @see SimpleCommand
 * @see CompositeCommand
 * @see RawCommand
 */
public abstract class AbstractCommand
@JvmOverloads constructor(
    public final override val owner: CommandOwner,
    public final override val primaryName: String,
    public final override val secondaryNames: Array<out String>,
    public override val description: String = "<no description available>",
    parentPermission: Permission = owner.parentPermission,
    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    public override val prefixOptional: Boolean = false,
) : Command {
    init {
        Command.checkCommandName(primaryName)
        secondaryNames.forEach(Command.Companion::checkCommandName)
    }

    public override val usage: String get() = description
    public override val permission: Permission by lazy { createOrFindCommandPermission(parentPermission) }
}