/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.logging

import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.SimpleLogger
import java.util.*

/**
 * 日志控制器的基本实现
 */
@ConsoleExperimentalApi
public abstract class AbstractLoggerController : LoggerController {

    /**
     * @param priority 尝试判断的日志等级
     * @param settings 配置中的日志等级 (see [getPriority])
     */
    protected open fun shouldLog(
        priority: LogPriority,
        settings: LogPriority,
    ): Boolean = settings <= priority

    /**
     * 获取配置中与 [identity] 对应的 [LogPriority]
     */
    protected abstract fun getPriority(identity: String?): LogPriority

    override fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean =
        shouldLog(LogPriority.by(priority), getPriority(identity))

    /**
     * 便于进行配置存储的 [LogPriority],
     * 等级优先级与 [SimpleLogger.LogPriority] 对应
     */
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

    /**
     * 路径形式实现的基本日志控制器
     *
     * Example:
     * 配置文件:
     * ```
     * defaultPriority: ALL
     * loggers:
     *  t: NONE
     *  t.sub: VERBOSE
     *  t.sub.1: NONE
     * ```
     *
     * ```
     * "logger.1"
     *      -> "logger.1" << null
     *      -> "logger" << null
     *      -> defaultPriority << ALL
     *
     * "t.sub.1"
     *      -> "t.sub.1" << NONE
     *
     * "t.sub.2"
     *      -> "t.sub.2" << null
     *      -> "t.sub" << VERBOSE
     *
     * ......
     * ```
     */
    @ConsoleExperimentalApi
    public abstract class PathBased
    @JvmOverloads public constructor(
        protected open val spliterator: Char = '.'
    ) : AbstractLoggerController() {
        protected abstract val defaultPriority: LogPriority
        protected abstract fun findPriority(identity: String?): LogPriority?

        /**
         * 从 [path] 析出下一次应该进行搜索的二次 path (@see [getPriority])
         *
         * @return 如果返回了 `null`, 会令 [getPriority] 返回 `findPriority(null) ?: defaultPriority`)
         */
        protected open fun nextPath(path: String): String? {
            val lastIndex = path.lastIndexOf(spliterator)
            if (lastIndex == -1) return null
            return path.substring(0, lastIndex)
        }

        override fun getPriority(identity: String?): LogPriority {
            if (identity == null) {
                return findPriority(null) ?: defaultPriority
            } else {
                var path: String = identity
                while (true) {
                    findPriority(path)?.let { return it }
                    path = nextPath(path) ?: return (findPriority(null) ?: defaultPriority)
                }
            }
        }
    }
}
