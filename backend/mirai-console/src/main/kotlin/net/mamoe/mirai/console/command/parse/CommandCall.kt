/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(ExperimentalStdlibApi::class)

package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.descriptor.UnresolvedCommandCallException
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.extensions.CommandCallResolverProvider
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage

@ExperimentalCommandDescriptors
public interface CommandCall {
    public val caller: CommandSender

    public val calleeName: String
    public val valueArguments: List<CommandValueArgument>

    public companion object {
        @JvmStatic
        public fun CommandCall.resolveOrNull(): ResolvedCommandCall? {
            GlobalComponentStorage.run {
                CommandCallResolverProvider.useExtensions { provider ->
                    provider.instance.resolve(this@resolveOrNull)?.let { return it }
                }
            }
            return null
        }

        @JvmStatic
        public fun CommandCall.resolve(): ResolvedCommandCall {
            return resolveOrNull() ?: throw UnresolvedCommandCallException(this)
        }
    }
}