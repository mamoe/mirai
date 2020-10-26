/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractSingletonExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.logging.LoggerControllerImpl
import net.mamoe.mirai.console.logging.LoggerController
import net.mamoe.mirai.console.permission.PermissionService

public interface LoggerControllerProvider : SingletonExtension<LoggerController> {
    public companion object ExtensionPoint :
        AbstractSingletonExtensionPoint<LoggerControllerProvider, LoggerController>(LoggerControllerProvider::class, MiraiConsoleImplementationBridge.frontendLoggerController)

}

/**
 * @see LoggerControllerProvider
 */
public class LoggerControllerProviderImpl(override val instance: LoggerController) : LoggerControllerProvider

/**
 * @see LoggerControllerProvider
 */
public class LoggerControllerProviderLazy(initializer: () -> LoggerController) : LoggerControllerProvider {
    override val instance: LoggerController by lazy(initializer)
}
