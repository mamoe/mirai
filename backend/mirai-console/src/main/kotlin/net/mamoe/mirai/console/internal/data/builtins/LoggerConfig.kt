/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.utils.SimpleLogger
import java.util.*

internal object LoggerConfig : AutoSavePluginConfig("Logger") {
    @ValueDescription("""
        日志输出等级 可选值: ALL, VERBOSE, DEBUG, INFO, WARNING, ERROR, NONE
    """)
    val defaultPriority by value(LogPriority.INFO)

    @ValueDescription("""
        特定日志记录器输出等级
    """)
    val loggers: Map<String, LogPriority> by value(
        mapOf("example.logger" to LogPriority.NONE)
    )

    enum class LogPriority {
        ALL(null),
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        NONE(null);

        var mapped: SimpleLogger.LogPriority? = null

        // resolve <clinit> NullPointerException
        private object Holder {
            @JvmField
            val mapping = EnumMap<SimpleLogger.LogPriority, LogPriority>(SimpleLogger.LogPriority::class.java)
        }
        companion object {
            fun by(priority: SimpleLogger.LogPriority): LogPriority = Holder.mapping[priority]!!
        }
        constructor(void: Nothing?)
        constructor() {
            mapped = SimpleLogger.LogPriority.valueOf(name)
            Holder.mapping[mapped] = this
        }

    }
}
