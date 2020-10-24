/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

package net.mamoe.mirai.console.logging

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.SimpleLogger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@Suppress("MemberVisibilityCanBePrivate")
@ConsoleExperimentalApi
@ConsoleFrontEndImplementation
public abstract class MiraiConsoleLoggerControllerPlatformBase : MiraiConsoleLoggerController {

    override fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean = true
    override val cacheLoggers: Boolean get() = true
    protected val registrations: ConcurrentHashMap<Any, AtomicReference<Any>> = ConcurrentHashMap()

    protected object NilIdentityPlaceholder

    override fun getLoggerRegistration(identity: String?): AtomicReference<Any> =
        registrations.computeIfAbsent(identity ?: NilIdentityPlaceholder) { AtomicReference() }

}