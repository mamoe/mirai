/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.resolve

import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.extensions.CommandCallResolverProvider
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * The resolver converting a [CommandCall] into [ResolvedCommandCall] based on registered []
 *
 * @see CommandCallResolverProvider The provider to instances of this class
 * @see BuiltInCommandCallResolver The builtin implementation
 */
@ExperimentalCommandDescriptors
public interface CommandCallResolver {
    public fun resolve(call: CommandCall): ResolvedCommandCall?

    public companion object {
        @JvmName("resolveCall")
        @ConsoleExperimentalApi
        @ExperimentalCommandDescriptors
        public fun CommandCall.resolve(): ResolvedCommandCall? {
            GlobalComponentStorage.run {
                CommandCallResolverProvider.useExtensions { provider ->
                    provider.instance.resolve(this@resolve)?.let { return it }
                }
            }
            return null
        }
    }
}