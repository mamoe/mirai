/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.internal.command.findOrCreateCommandPermission
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
) : Command {

    @ExperimentalCommandDescriptors
    override val prefixOptional: Boolean
        get() = false

    init {
        Command.checkCommandName(primaryName)
        secondaryNames.forEach(Command.Companion::checkCommandName)
    }

    public override val usage: String get() = description
    public override val permission: Permission by lazy { findOrCreateCommandPermission(parentPermission) }
}