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
import net.mamoe.mirai.console.internal.command.GroupedCommandSubCommandAnnotationResolver
import net.mamoe.mirai.console.internal.command.SubCommandReflector

public abstract class AbstractSubCommandGroup(
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext,
) : CommandArgumentContextAware, SubCommandGroup {

    private val reflector by lazy { SubCommandReflector(this, GroupedCommandSubCommandAnnotationResolver) }

    @ExperimentalCommandDescriptors
    public final override val overloads: List<CommandSignatureFromKFunction> by lazy {
        reflector.findSubCommands().also {
            reflector.validate(it)
        }
    }

    /**
     * 智能参数解析环境
     */ // open since 2.12
    public override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext

}


