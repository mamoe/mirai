/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.resolve.BuiltInCommandCallResolver
import net.mamoe.mirai.console.command.resolve.CommandCallResolver
import net.mamoe.mirai.console.extension.AbstractSingletonExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension

@ExperimentalCommandDescriptors
public interface CommandCallResolverProvider : SingletonExtension<CommandCallResolver> {
    public companion object ExtensionPoint :
        AbstractSingletonExtensionPoint<CommandCallResolverProvider, CommandCallResolver>(
            CommandCallResolverProvider::class,
            BuiltInCommandCallResolver
        )
}

@ExperimentalCommandDescriptors
public class CommandCallResolverProviderImpl(override val instance: CommandCallResolver) : CommandCallResolverProvider

@ExperimentalCommandDescriptors
public class CommandCallResolverProviderImplLazy(initializer: () -> CommandCallResolver) : CommandCallResolverProvider {
    override val instance: CommandCallResolver by lazy(initializer)
}