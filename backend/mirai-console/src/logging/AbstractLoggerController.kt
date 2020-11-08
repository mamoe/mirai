/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.logging

import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.SimpleLogger
import java.util.*

@ConsoleExperimentalApi
public abstract class AbstractLoggerController : LoggerController {

    protected open fun shouldLog(
        priority: LogPriority,
        settings: LogPriority
    ): Boolean = settings <= priority

    protected abstract fun getPriority(identity: String?): LogPriority

    override fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean =
        shouldLog(LogPriority.by(priority), getPriority(identity))

    @Suppress("unused")
    @ConsoleExperimentalApi
    public enum class LogPriority {
        ALL(null),
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        NONE(null);

        private var mapped: SimpleLogger.LogPriority? = null

        public companion object {
            private val mapping = EnumMap<SimpleLogger.LogPriority, LogPriority>(SimpleLogger.LogPriority::class.java)

            public fun by(priority: SimpleLogger.LogPriority): LogPriority = mapping[priority]!!

            init {
                values().forEach { priority ->
                    mapping[priority.mapped ?: return@forEach] = priority
                }
            }
        }

        @Suppress("UNUSED_PARAMETER")
        constructor(void: Nothing?)
        constructor() {
            mapped = SimpleLogger.LogPriority.valueOf(name)
        }

    }

}
