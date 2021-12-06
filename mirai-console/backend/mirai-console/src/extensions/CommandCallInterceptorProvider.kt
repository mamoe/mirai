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
import net.mamoe.mirai.console.command.resolve.CommandCallInterceptor
import net.mamoe.mirai.console.extension.AbstractInstanceExtensionPoint
import net.mamoe.mirai.console.extension.InstanceExtension

@ExperimentalCommandDescriptors
public interface CommandCallInterceptorProvider : InstanceExtension<CommandCallInterceptor> {
    public companion object ExtensionPoint :
        AbstractInstanceExtensionPoint<CommandCallInterceptorProvider, CommandCallInterceptor>(
            CommandCallInterceptorProvider::class
        )
}

@ExperimentalCommandDescriptors
public class CommandCallInterceptorProviderImpl(override val instance: CommandCallInterceptor) :
    CommandCallInterceptorProvider

@ExperimentalCommandDescriptors
public class CommandCallInterceptorProviderImplLazy(initializer: () -> CommandCallInterceptor) :
    CommandCallInterceptorProvider {
    override val instance: CommandCallInterceptor by lazy(initializer)
}