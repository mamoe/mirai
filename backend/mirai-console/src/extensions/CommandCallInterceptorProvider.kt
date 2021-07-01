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