/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.logging

import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.internal.logging.MiraiConsoleLogger
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger

/**
 * 日志控制系统
 *
 * @see [AbstractLoggerController]
 * @see [MiraiConsoleImplementation.loggerController]
 * @see [MiraiConsoleLogger]
 */
@ConsoleExperimentalApi
public interface LoggerController {
    /** 是否应该记录该等级的日志 */
    public fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean

    public fun getLoggerControlState(identity: String?): LoggerControlState {
        return object : LoggerControlState {
            override fun shouldLog(priority: SimpleLogger.LogPriority): Boolean {
                return shouldLog(identity, priority)
            }
        }
    }

    /**
     * 一个 [MiraiLogger] 的日志开关状态
     *
     * 这个类可以缓存 [LoggerController.shouldLog] 的结果, 避免多次查询
     *
     * 在需要的时候会自动更新, 所以不需要手动更换 [LoggerControlState]
     */
    @ConsoleExperimentalApi
    public interface LoggerControlState {
        public fun shouldLog(priority: SimpleLogger.LogPriority): Boolean
    }
}
