/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.logging

import net.mamoe.mirai.console.internal.logging.LoggerControllerImpl
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.SimpleLogger

/**
 * 日志控制系统
 *
 * @see [LoggerControllerImpl]
 */
@ConsoleExperimentalApi
public interface LoggerController {
    /** 是否应该记录该等级的日志 */
    public fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean

}
